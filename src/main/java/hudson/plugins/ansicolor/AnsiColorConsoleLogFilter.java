package hudson.plugins.ansicolor;

import hudson.console.ConsoleLogFilter;
import hudson.console.LineTransformationOutputStream;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ConsoleLogFilter} that adds a {@link SimpleHtmlNote} to each line.
 */
public final class AnsiColorConsoleLogFilter extends ConsoleLogFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(AnsiColorConsoleLogFilter.class.getName());

    private AnsiColorMap colorMap;

    public AnsiColorConsoleLogFilter(AnsiColorMap colorMap) {
        super();
        this.colorMap = colorMap;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OutputStream decorateLogger(AbstractBuild build, final OutputStream logger)
            throws IOException, InterruptedException {
        if (logger == null) {
            return null;
        }

        return new LineTransformationOutputStream() {
            AnsiHtmlOutputStream ansi = new AnsiHtmlOutputStream(logger, colorMap, new AnsiAttributeElement.Emitter() {
                @Override
                public void emitHtml(String html) {
                    try {
                        new SimpleHtmlNote(html).encodeTo(logger);
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "Failed to add HTML markup '" + html + "'", e);
                    }
                }
            });

            @Override
            protected void eol(byte[] b, int len) throws IOException {
                ansi.write(b, 0, len);
                ansi.flush();
                logger.flush();
            }

            @Override
            public void close() throws IOException {
                ansi.close();
                logger.close();
                super.close();
            }
        };
    }
}
