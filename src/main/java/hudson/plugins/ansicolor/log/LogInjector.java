package hudson.plugins.ansicolor.log;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.plugins.ansicolor.action.ActionNote;
import hudson.plugins.ansicolor.action.ColorizedAction;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.*;
import java.util.Optional;
import java.util.logging.Logger;

public class LogInjector {
    private static final Logger LOGGER = Logger.getLogger(LogInjector.class.getName());
    private static final int CONSOLE_TAIL_DEFAULT = 150;
    private static final int BUFFER_SIZE = 64 * 1024;

    public void injectShortlogAction(File logFile, int injectionFromEnd, ActionNote actionNote) {
        final File logTmp = new File(String.format("%s.ansicolor.tmp", logFile));
        final File logFileBkp = new File(String.format("%s.bkp", logFile));
        try {
            try (
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(logFile));
                FileOutputStream outputStream = new FileOutputStream(logTmp);
            ) {
                final long injectionPoint = logFile.length() - injectionFromEnd * 1024;
                final byte[] buf = new byte[BUFFER_SIZE];
                int read;
                int totalRead = 0;
                int i = 0;
                boolean injected = false;
                do {
                    read = inputStream.read(buf);
                    if (read != -1) {
                        i++;
                        if (!injected && totalRead + read >= injectionPoint) {
                            final BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
                            final CountingOutputStream countingStream = new CountingOutputStream(bufferedStream);
                            actionNote.encodeTo(countingStream);
                            final long actionNoteLength = countingStream.getByteCount();
                            // todo When (if doable) in the middle of another ActionNote render afterwards in order to preserve it
                            final int spot = (int) (injectionPoint - totalRead + actionNoteLength);
                            outputStream.write(buf, 0, spot);
                            bufferedStream.flush();
                            outputStream.write(buf, spot, read - spot);
                            injected = true;
                        } else {
                            outputStream.write(buf, 0, read);
                        }
                        totalRead += read;
                    }
                } while (read != -1);
            }

            if (logFile.renameTo(logFileBkp) && logTmp.renameTo(logFile) && logFileBkp.delete()) {
                LOGGER.fine("Substituted original log with the injected one.");
            } else {
                LOGGER.warning("Error while substituting log file");
            }
        } catch (IOException e) {
            if (logTmp.exists() && !logTmp.getPath().equals(logFile.getPath())) {
                if (!logTmp.delete()) {
                    LOGGER.fine("Error while deleting tmp file");
                }
            }
            if (logFileBkp.exists()) {
                if (!logFileBkp.delete()) {
                    LOGGER.fine("Error while deleting backup file");
                }
            }
            LOGGER.warning("Error while injecting shortlog action: " + e.getMessage());
        }
    }

    @Extension
    public static class InjectorRunListener extends RunListener<Run<?, ?>> {
        @Override
        public void onFinalized(Run<?, ?> run) {
            super.onFinalized(run);
            // todo Find the actual last start action before tail
            final Optional<ColorizedAction> startAction = run.getActions(ColorizedAction.class).stream().filter(a -> a.getCommand().equals(ColorizedAction.Command.START)).findAny();
            if (startAction.isPresent()) {
                final File logFile = new File(run.getRootDir(), "log");
                if (logFile.isFile()) {
                    final LogInjector logInjector = new LogInjector();
                    final String consoleTail = System.getProperty("hudson.consoleTailKB");
                    logInjector.injectShortlogAction(logFile, consoleTail != null ? Integer.parseInt(consoleTail) : CONSOLE_TAIL_DEFAULT, new ActionNote(startAction.get()));
                }
            }
        }
    }
}
