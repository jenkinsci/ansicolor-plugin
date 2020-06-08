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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.ansicolor.action.ActionNote;
import hudson.plugins.ansicolor.action.ColorizedAction;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Build wrapper that decorates the build's logger to filter output with {@link AnsiHtmlOutputStream}.
 *
 * @author Daniel Doubrovkine
 */
@SuppressWarnings("unused")
public final class AnsiColorBuildWrapper extends SimpleBuildWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        final ColorizedAction action = new ColorizedAction(colorMapName, ColorizedAction.Command.START);
        build.replaceAction(action);
        listener.annotate(new ActionNote(action));
    }

    /**
     * Registers {@link AnsiColorBuildWrapper} as a {@link BuildWrapper}.
     */
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        private AnsiColorMap[] colorMaps = new AnsiColorMap[0];

        private String globalColorMapName;

        public DescriptorImpl() {
            super(AnsiColorBuildWrapper.class);
            load();
        }

        private AnsiColorMap[] withDefaults(AnsiColorMap[] colorMaps) {
            Map<String, AnsiColorMap> maps = new LinkedHashMap<>();
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
        public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
            try {
                final List<AnsiColorMap> colorMaps = req.bindJSONToList(AnsiColorMap.class, req.getSubmittedForm().get("colorMap"));
                for (AnsiColorMap colorMap : colorMaps) {
                    validateFieldName(colorMap.getName());
                    final HashMap<String, Supplier<String>> validatedFields = new HashMap<>();
                    validatedFields.put("black", colorMap::getBlack);
                    validatedFields.put("blackB", colorMap::getBlackB);
                    validatedFields.put("red", colorMap::getRed);
                    validatedFields.put("redB", colorMap::getRedB);
                    validatedFields.put("green", colorMap::getGreen);
                    validatedFields.put("greenB", colorMap::getGreenB);
                    validatedFields.put("yellow", colorMap::getYellow);
                    validatedFields.put("yellowB", colorMap::getYellowB);
                    validatedFields.put("blue", colorMap::getBlue);
                    validatedFields.put("blueB", colorMap::getBlueB);
                    validatedFields.put("magenta", colorMap::getMagenta);
                    validatedFields.put("magentaB", colorMap::getMagentaB);
                    validatedFields.put("cyan", colorMap::getCyan);
                    validatedFields.put("cyanB", colorMap::getCyanB);
                    validatedFields.put("white", colorMap::getWhite);
                    validatedFields.put("whiteB", colorMap::getWhiteB);
                    for (Map.Entry<String, Supplier<String>> e : validatedFields.entrySet()) {
                        validateFieldColorLiteral(e.getKey(), e.getValue().get());
                    }
                }
                final String globalColorMapName = req.getSubmittedForm().getString("globalColorMapName").trim();
                final FormValidation validation = doCheckGlobalColorMapName(globalColorMapName);
                if (validation.kind != FormValidation.Kind.OK) {
                    throw new FormException(validation.getMessage(), "globalColorMapName");
                }

                if (!globalColorMapName.isEmpty() && colorMaps.stream().noneMatch(cm -> cm.getName().equals(globalColorMapName))) {
                    throw new FormException("Global color map name must match one of the color maps", "globalColorMapName");
                }
                setColorMaps(colorMaps.toArray(new AnsiColorMap[0]));
                setGlobalColorMapName(globalColorMapName.isEmpty() ? null : globalColorMapName);
                save();
                return true;
            } catch (ServletException e) {
                throw new FormException(e, "");
            }
        }

        private void validateFieldName(String fieldValue) throws FormException {
            final FormValidation validation = doCheckName(fieldValue);
            if (validation.kind != FormValidation.Kind.OK) {
                throw new FormException(validation.getMessage(), "name");
            }
        }

        private void validateFieldColorLiteral(String fieldName, String fieldValue) throws FormException {
            final FormValidation globalColorMapNameValidation = validateColorLiteral(fieldValue);
            if (globalColorMapNameValidation.kind != FormValidation.Kind.OK) {
                throw new FormException(globalColorMapNameValidation.getMessage(), fieldName);
            }
        }

        public FormValidation doCheckGlobalColorMapName(@QueryParameter String value) {
            return value.isEmpty() ? FormValidation.ok() : validateColorMapName(value);
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckName(@QueryParameter final String value) {
            return validateColorMapName(value);
        }

        private FormValidation validateColorMapName(String name) {
            final int nameLength = name.trim().length();
            return (nameLength < 1 || nameLength > 256) ? FormValidation.error("Color map name length must be between 1 and 256 chars.") : FormValidation.ok();
        }

        public FormValidation doCheckBlack(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckBlackB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckRed(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckRedB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckGreen(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckGreenB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckYellow(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckYellowB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckBlue(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckBlueB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckMagenta(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckMagentaB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckCyan(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckCyanB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckWhite(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        public FormValidation doCheckWhiteB(@QueryParameter String value) {
            return validateColorLiteral(value);
        }

        private FormValidation validateColorLiteral(String name) {
            final int nameLength = name.trim().length();
            return (nameLength < 1 || nameLength > 64) ? FormValidation.error("Color literal length must be between 1 and 64 chars.") : FormValidation.ok();
        }

        public String getGlobalColorMapName() {
            return globalColorMapName;
        }

        public void setGlobalColorMapName(String colorMapName) {
            globalColorMapName = colorMapName;
        }

        public AnsiColorMap[] getColorMaps() {
            return withDefaults(colorMaps);
        }

        public void setColorMaps(AnsiColorMap[] maps) {
            colorMaps = maps.clone();
        }

        public AnsiColorMap getColorMap(final String name) {
            for (AnsiColorMap colorMap : getColorMaps()) {
                if (colorMap.getName().equals(name)) {
                    return colorMap;
                }
            }
            return AnsiColorMap.Default;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillColorMapNameItems() {
            ListBoxModel m = new ListBoxModel();
            for (AnsiColorMap colorMap : getColorMaps()) {
                String name = colorMap.getName().trim();
                if (name.length() > 0) {
                    m.add(name);
                }
            }
            return m;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillDefaultForegroundItems() {
            ListBoxModel m = new ListBoxModel();

            m.add("Jenkins Default", "");
            for (AnsiColorMap.Color color : AnsiColorMap.Color.values()) {
                m.add(color.toString(), String.valueOf(color.ordinal()));
            }

            return m;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillDefaultBackgroundItems() {
            return doFillDefaultForegroundItems();
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
