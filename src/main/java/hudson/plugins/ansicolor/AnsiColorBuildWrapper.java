/*
 * The MIT License
 * 
 * Copyright (c) 2011 Daniel Doubrovkine
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.ansicolor;

import hudson.Extension;
import hudson.Launcher;
import hudson.console.LineTransformationOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Build wrapper that decorates the build's logger to filter output with {@link AnsiHtmlOutputStream}.
 * 
 * @author Daniel Doubrovkine
 */
public final class AnsiColorBuildWrapper extends BuildWrapper {

	private final String colorMapName;

	/**
	 * Create a new {@link AnsiColorBuildWrapper}.
	 */
	@DataBoundConstructor
	public AnsiColorBuildWrapper(String colorMapName) {
		this.colorMapName = colorMapName;
	}
	
	public String getColorMapName() {
		return colorMapName == null ? AnsiColorMap.DefaultName : colorMapName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		return new Environment() {
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream decorateLogger(AbstractBuild build, final OutputStream logger) {
		final AnsiColorMap colorMap = getDescriptor().getColorMap(getColorMapName());

        return new LineTransformationOutputStream() {
            AnsiHtmlOutputStream ansi = new AnsiHtmlOutputStream(logger, colorMap, new AnsiAttributeElement.Emitter() {
                public void emitHtml(String html) throws IOException {
                    new SimpleHtmlNote(html).encodeTo(logger);
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

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

	/**
	 * Registers {@link AnsiColorBuildWrapper} as a {@link BuildWrapper}.
	 */
	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		private AnsiColorMap[] colorMaps = new AnsiColorMap[0];

		public DescriptorImpl() {
			super(AnsiColorBuildWrapper.class);
			load();
		}

		private AnsiColorMap[] withDefaults(AnsiColorMap[] colorMaps) {
			Map<String,AnsiColorMap> maps = new LinkedHashMap<String,AnsiColorMap>();
			addAll(AnsiColorMap.defaultColorMaps(), maps);
			addAll(colorMaps, maps);
			return maps.values().toArray(new AnsiColorMap[1]);
		}
		
		private void addAll(AnsiColorMap[] maps, Map<String,AnsiColorMap> to) {
			for(AnsiColorMap map : maps)
				to.put(map.getName(), map);
		}

		@Override
		public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
			try {
				setColorMaps(req.bindJSONToList(AnsiColorMap.class,
					req.getSubmittedForm().get("colorMap")).toArray(new AnsiColorMap[1]));
				return true;
			} catch (ServletException e) {
				throw new FormException(e, "");
			}
		}
		
		public AnsiColorMap[] getColorMaps() {
			return withDefaults(colorMaps);
		}
		
		public void setColorMaps(AnsiColorMap[] maps) {
			colorMaps = maps;
			save();
		}
		
		public AnsiColorMap getColorMap(final String name) {
			for (AnsiColorMap colorMap: getColorMaps()) {
				if (colorMap.getName().equals(name)) {
					return colorMap;
				}
			}
			return AnsiColorMap.Default;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getDisplayName() {
			return Messages.DisplayName();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}
	}
}
