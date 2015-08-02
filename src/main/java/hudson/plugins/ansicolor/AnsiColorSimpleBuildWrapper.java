package hudson.plugins.ansicolor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.console.LineTransformationOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

public class AnsiColorSimpleBuildWrapper extends SimpleBuildWrapper implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final String colorMapName;

    private static final Logger LOG = Logger.getLogger(AnsiColorSimpleBuildWrapper.class.getName());

    @DataBoundConstructor
    public AnsiColorSimpleBuildWrapper(String colorMapName) {
        this.colorMapName = colorMapName;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
    }

    @Override
    public ConsoleLogFilter createLoggerDecorator(Run<?, ?> build) {     
        return new ConsoleLogFilterImpl(colorMapName);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 81).append(colorMapName).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnsiColorSimpleBuildWrapper other = (AnsiColorSimpleBuildWrapper) obj;
        if ((this.colorMapName == null) ? (other.colorMapName != null) : !this.colorMapName.equals(other.colorMapName)) {
            return false;
        }
        return true;
    }

    protected static final class ConsoleLogFilterImpl extends ConsoleLogFilter implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String colorMapName;

        private final AnsiColorMap[] colorMaps = new AnsiColorMap[0];

        private ConsoleLogFilterImpl(String colorMapName) {
            this.colorMapName = colorMapName;
        }

        public AnsiColorMap getColorMap(final String name) {
            for (AnsiColorMap colorMap : getColorMaps()) {
                if (colorMap.getName().equals(name)) {
                    return colorMap;
                }
            }
            return AnsiColorMap.Default;
        }
        
        public AnsiColorMap[] getColorMaps() {
            return withDefaults(colorMaps);
        }

        private AnsiColorMap[] withDefaults(AnsiColorMap[] colorMaps) {
            Map<String, AnsiColorMap> maps = new LinkedHashMap<String, AnsiColorMap>();
            addAll(AnsiColorMap.defaultColorMaps(), maps);
            addAll(colorMaps, maps);
            return maps.values().toArray(new AnsiColorMap[1]);
        }

        private void addAll(AnsiColorMap[] maps, Map<String, AnsiColorMap> to) {
            for (AnsiColorMap map : maps) {
                to.put(map.getName(), map);
            }
        }

        @Override
        public OutputStream decorateLogger(AbstractBuild build, final OutputStream logger) throws IOException, InterruptedException {
            final AnsiColorMap colorMap = getColorMap(colorMapName);

            if (logger == null) {
                return null;
            }

            return new LineTransformationOutputStream() {
                AnsiHtmlOutputStream ansi = new AnsiHtmlOutputStream(logger, colorMap, new AnsiAttributeElement.Emitter() {
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

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        public DescriptorImpl() {
            super(AnsiColorSimpleBuildWrapper.class);
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }
    }
}
