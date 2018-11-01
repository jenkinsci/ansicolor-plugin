package hudson.plugins.ansicolor;

import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.JenkinsJVM;

/**
 * {@link ConsoleLogFilter} that adds a {@link SimpleHtmlNote} to each line.
 */
public final class AnsiColorConsoleLogFilter extends ConsoleLogFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(AnsiColorConsoleLogFilter.class.getName());

    private AnsiColorMap colorMap;
    private final Map<String, byte[]> notes;

    public AnsiColorConsoleLogFilter(AnsiColorMap colorMap) {
        super();
        this.colorMap = colorMap;
        this.notes = new HashMap<>();
        // some cases of AnsiHtmlOutputStream.setForegroundColor:
        for (AnsiColorMap.Color color : AnsiColorMap.Color.values()) {
            pregenerateNote(new AnsiAttributeElement(AnsiAttributeElement.AnsiAttrType.FG, "span", "style=\"color: " + colorMap.getNormal(color.ordinal()) + ";\""));
            pregenerateNote(new AnsiAttributeElement(AnsiAttributeElement.AnsiAttrType.FG, "span", "style=\"color: " + colorMap.getBright(color.ordinal()) + ";\""));
        }
        pregenerateNote(AnsiAttributeElement.bold());
        pregenerateNote(AnsiAttributeElement.italic());
        pregenerateNote(AnsiAttributeElement.underline());
        pregenerateNote(AnsiAttributeElement.underlineDouble());
        pregenerateNote(AnsiAttributeElement.strikeout());
        pregenerateNote(AnsiAttributeElement.framed());
        pregenerateNote(AnsiAttributeElement.overline());
        // TODO other cases, and other methods
        LOG.log(Level.FINE, "Notes pregenerated for {0}", notes.keySet());
    }
    
    private void pregenerateNote(AnsiAttributeElement element) {
        element.emitOpen(html -> pregenerateNote(html));
        element.emitClose(html -> pregenerateNote(html));
    }
    
    private void pregenerateNote(String html) {
        if (!notes.containsKey(html)) {
            JenkinsJVM.checkJenkinsJVM();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                new SimpleHtmlNote(html).encodeTo(baos);
            } catch (IOException x) { // should be impossible
                throw new RuntimeException(x);
            }
            notes.put(html, baos.toByteArray());
        }
    }

    private Object readResolve() { // handle old program.dat
        return notes == null ? new AnsiColorConsoleLogFilter(colorMap) : this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public OutputStream decorateLogger(AbstractBuild build, final OutputStream logger)
            throws IOException, InterruptedException {
        if (logger == null) {
            return null;
        }

        return new AnsiHtmlOutputStream(logger, colorMap, new AnsiAttributeElement.Emitter() {
            @Override
            public void emitHtml(String html) {
                try {
                    byte[] pregenerated = notes.get(html);
                    if (pregenerated != null) {
                        logger.write(pregenerated);
                    } else {
                        // TODO decline to use pregenerated form of end tag if start tag could not be pregenerated
                        new SimpleHtmlNote(html).encodeTo(logger);
                    }
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to add HTML markup '" + html + "'", e);
                }
            }
        });
    }
}
