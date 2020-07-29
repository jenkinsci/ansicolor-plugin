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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ShortlogActionCreator {
    private static final Logger LOGGER = Logger.getLogger(ShortlogActionCreator.class.getName());
    private static final int CONSOLE_TAIL_DEFAULT = 150;
    private static final int BUFFER_SIZE = 16 * 1024;

    private final LineIdentifier lineIdentifier;
    private final byte[] eol;

    public ShortlogActionCreator(LineIdentifier lineIdentifier, String eol) {
        this.lineIdentifier = lineIdentifier;
        this.eol = eol.getBytes(UTF_8);
    }

    public ColorizedAction createActionForShortlog(File logFile, Map<String, ColorizedAction> actions, int shortlogLimit) {
        final ActionContext lastAction = findLastActionBefore(logFile, actions.keySet(), shortlogLimit);
        if (!lastAction.isEmpty()) {
            final ColorizedAction colorizedAction = actions.get(lastAction.serializedAction);
            if (ColorizedAction.Command.START.equals(colorizedAction.getCommand())) {
                return new ColorizedAction(lineIdentifier.hash(ConsoleNote.removeNotes(lastAction.line), 1), colorizedAction);
            }
        }
        return null;
    }

    private ActionContext findLastActionBefore(File logFile, Collection<String> serializedActions, int shortlogLimit) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(logFile))) {
            final long shortlogStart = logFile.length() - shortlogLimit * 1024L;
            if (shortlogStart > 0) {
                final byte[] buf = new byte[BUFFER_SIZE];
                int read;
                int totalRead = 0;
                String lastAction = "";
                String partialLine = "";
                while ((read = inputStream.read(buf)) != -1) {
                    final int startInBuff = shortlogStart > totalRead ? (int) (shortlogStart - totalRead) : 0;
                    final String action = findLastAction(serializedActions, buf, startInBuff);
                    if (!action.isEmpty()) {
                        lastAction = action;
                    }
                    if (totalRead + read >= shortlogStart) {
                        final int eolPos = indexOfEol(buf, startInBuff);
                        if (eolPos != -1 && !lastAction.isEmpty()) {
                            return new ActionContext(lastAction, partialLine + new String(buf, startInBuff, eolPos - startInBuff + eol.length, UTF_8));
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

    private String findLastAction(Collection<String> serializedActions, byte[] buf, int maxPos) {
        String lastAction = "";
        int preamblePos = 0;
        while (preamblePos < maxPos && (preamblePos = ConsoleNote.findPreamble(buf, preamblePos, buf.length - preamblePos)) != -1) {
            final int begin = preamblePos;
            lastAction = serializedActions.stream().filter(sa -> buf.length - begin > sa.length() && sa.equals(new String(buf, begin, sa.length(), UTF_8))).findFirst().orElse(lastAction);
            preamblePos++;
        }
        return lastAction;
    }

    private int indexOfEol(byte[] buf, int after) {
        for (int i = after; i < buf.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(buf, i, i + eol.length), eol)) {
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
            final List<ColorizedAction.Command> commands = Arrays.asList(ColorizedAction.Command.START, ColorizedAction.Command.STOP);
            Map<String, ColorizedAction> actions = run.getActions(ColorizedAction.class).stream()
                .filter(a -> commands.contains(a.getCommand()))
                .collect(Collectors.toMap(a -> {
                    try {
                        return new ActionNote(a).encode();
                    } catch (IOException e) {
                        LOGGER.warning("Will not be able to identify all ColorizedActions: " + e.getMessage());
                    }
                    return "";
                }, Function.identity()));
            if (!actions.isEmpty()) {
                final File logFile = new File(run.getRootDir(), "log");
                if (logFile.isFile()) {
                    final ShortlogActionCreator shortlogActionCreator = new ShortlogActionCreator(new LineIdentifier(), System.lineSeparator());
                    final String consoleTail = System.getProperty("hudson.consoleTailKB");
                    final ColorizedAction action = shortlogActionCreator.createActionForShortlog(logFile, actions, consoleTail != null ? Integer.parseInt(consoleTail) : CONSOLE_TAIL_DEFAULT);
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
