package hudson.plugins.ansicolor.action;

import hudson.Extension;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ShortlogActionCreator {
    private static final Logger LOGGER = Logger.getLogger(ShortlogActionCreator.class.getName());
    private static final int CONSOLE_TAIL_DEFAULT = 150;
    private static final int BUFFER_SIZE = 16 * 1024;
    private static final byte[] EOL = System.lineSeparator().getBytes(UTF_8);

    private final LineIdentifier lineIdentifier;

    public ShortlogActionCreator(LineIdentifier lineIdentifier) {
        this.lineIdentifier = lineIdentifier;
    }

    public ColorizedAction createActionForShortlog(File logFile, Map<String, ColorizedAction> startActions, int beginFromEnd) {
        final ActionContext shortlogStartAction = findStartActionAt(logFile, startActions.keySet(), beginFromEnd);
        if (!shortlogStartAction.isEmpty()) {
            return new ColorizedAction(lineIdentifier.hash(ConsoleNote.removeNotes(shortlogStartAction.line), 1), startActions.get(shortlogStartAction.serializedAction));
        }
        return null;
    }

    private ActionContext findStartActionAt(File logFile, Collection<String> serializedActions, int bytesFromEnd) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(logFile))) {
            final long shortlogStart = logFile.length() - bytesFromEnd * 1024L;
            if (shortlogStart > 0) {
                final byte[] buf = new byte[BUFFER_SIZE];
                int read;
                int totalRead = 0;
                String currentStartAction = "";
                String partialLine = "";
                while ((read = inputStream.read(buf)) != -1) {
                    final String newAction = findActionInBuffer(serializedActions, buf);
                    if (!newAction.isEmpty()) {
                        currentStartAction = newAction;
                    }
                    if (totalRead + read >= shortlogStart) {
                        final int startInBuff = shortlogStart > totalRead ? (int) (shortlogStart - totalRead) : 0;
                        final int eolPos = indexOfEol(buf, startInBuff);
                        if (eolPos != -1) {
                            return new ActionContext(currentStartAction, partialLine + new String(buf, startInBuff, eolPos - startInBuff + EOL.length, UTF_8));
                        } else {
                            // line extends to the next buffer
                            partialLine = new String(Arrays.copyOfRange(buf, startInBuff, buf.length - 1), UTF_8);
                        }
                    }
                    totalRead += read;
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Cannot search log for actions: " + e.getMessage());
        }
        return new ActionContext();
    }

    private String findActionInBuffer(Collection<String> serializedActions, byte[] buf) {
        int preamblePos = 0;
        while (preamblePos < buf.length && (preamblePos = ConsoleNote.findPreamble(buf, preamblePos, buf.length - preamblePos)) != -1) {
            final int begin = preamblePos;
            final Optional<String> startAction = serializedActions.stream().filter(sa -> buf.length - begin > sa.length() && sa.equals(new String(buf, begin, sa.length(), UTF_8))).findFirst();
            if (startAction.isPresent()) {
                return startAction.get();
            }
            preamblePos++;
        }
        return "";
    }

    private int indexOfEol(byte[] buf, int after) {
        for (int i = after; i < buf.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(buf, i, i + EOL.length), EOL)) {
                return i;
            }
        }
        return -1;
    }

    @Extension
    public static class Listener extends RunListener<Run<?, ?>> {
        @Override
        public void onFinalized(Run<?, ?> run) {
            super.onFinalized(run);
            Map<String, ColorizedAction> startActions = run.getActions(ColorizedAction.class).stream()
                .filter(a -> a.getCommand().equals(ColorizedAction.Command.START))
                .collect(Collectors.toMap(a -> {
                    try {
                        return new ActionNote(a).encode();
                    } catch (IOException e) {
                        LOGGER.warning("Will not be able to identify all ColorizedActions: " + e.getMessage());
                    }
                    return "";
                }, Function.identity()));
            if (!startActions.isEmpty()) {
                final File logFile = new File(run.getRootDir(), "log");
                if (logFile.isFile()) {
                    final ShortlogActionCreator shortlogActionCreator = new ShortlogActionCreator(new LineIdentifier());
                    final String consoleTail = System.getProperty("hudson.consoleTailKB");
                    final ColorizedAction action = shortlogActionCreator.createActionForShortlog(logFile, startActions, consoleTail != null ? Integer.parseInt(consoleTail) : CONSOLE_TAIL_DEFAULT);
                    if (action != null) {
                        run.addAction(action);
                    }
                }
            }
        }
    }

    private static class ActionContext {
        private final String serializedAction;
        private final String line;

        public ActionContext() {
            this(null, null);
        }

        public ActionContext(String serializedAction, String line) {
            this.serializedAction = serializedAction;
            this.line = line;
        }

        public boolean isEmpty() {
            return serializedAction == null && line == null;
        }
    }
}
