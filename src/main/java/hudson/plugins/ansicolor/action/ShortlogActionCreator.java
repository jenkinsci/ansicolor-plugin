package hudson.plugins.ansicolor.action;

import hudson.Extension;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
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

    public ColorizedAction createActionForShortlog(File logFile, Map<String, ColorizedAction> actions, int shortlogLimit, boolean keepLinesWhole) {
        final ActionContext lastAction = findLastActionBefore(logFile, actions.keySet(), shortlogLimit, keepLinesWhole);
        if (!lastAction.isEmpty()) {
            final ColorizedAction colorizedAction = actions.get(lastAction.serializedAction);
            if (ColorizedAction.Command.START.equals(colorizedAction.getCommand())) {
                return new ColorizedAction(lineIdentifier.hash(ConsoleNote.removeNotes(lastAction.line), 1), colorizedAction);
            }
        }
        return null;
    }

    private ActionContext findLastActionBefore(File logFile, Collection<String> serializedActions, int shortlogLimit, boolean keepLinesWhole) {
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
                        final int[] beginLength = calculateBeginLength(buf, startInBuff, eolPos, partialLine.isEmpty() && keepLinesWhole);
                        final int begin = beginLength[0];
                        final int length = beginLength[1];
                        if (length != -1 && !lastAction.isEmpty()) {
                            return new ActionContext(lastAction, partialLine + new String(buf, begin, length, UTF_8));
                        } else {
                            // line extends to the next buffer
                            partialLine = new String(Arrays.copyOfRange(buf, begin, buf.length), UTF_8);
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
        for (int i = after + 1; i < buf.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(buf, i, i + eol.length), eol)) {
                return i;
            }
        }
        return -1;
    }

    private int[] calculateBeginLength(byte[] buf, int startInBuff, int eolPos, boolean keepLinesWhole) {
        if (keepLinesWhole) {
            final int begin = eolPos != -1? eolPos + eol.length: startInBuff;
            return new int[]{begin, eolPos != -1 ? indexOfEol(buf, eolPos) - begin + eol.length : -1};
        }
        return new int[]{startInBuff, eolPos != -1 ? eolPos - startInBuff + eol.length : -1};
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
                    final VersionNumber keepLinesWholeVersion = new VersionNumber("2.260");
                    final String consoleTail = System.getProperty("hudson.consoleTailKB");
                    final ColorizedAction action = shortlogActionCreator.createActionForShortlog(
                        logFile,
                        actions,
                        consoleTail != null ? Integer.parseInt(consoleTail) : CONSOLE_TAIL_DEFAULT,
                        Optional.ofNullable(Jenkins.getVersion()).orElse(keepLinesWholeVersion).isNewerThan(keepLinesWholeVersion)
                    );
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
