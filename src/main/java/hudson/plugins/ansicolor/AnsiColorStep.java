package hudson.plugins.ansicolor;

import hudson.Extension;
import hudson.plugins.ansicolor.AnsiColorBuildWrapper.DescriptorImpl;
import hudson.util.ListBoxModel;

import java.util.Collections;


import jenkins.model.Jenkins;

import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.model.Run;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

/**
 * Custom pipeline step that can be used without a node and build wrapper.
 */
public class AnsiColorStep extends Step {

    private final String colorMapName;

    /**
     * Create a new {@link AnsiColorStep}.
     */
    @DataBoundConstructor
    public AnsiColorStep(final String colorMapName) {
        this.colorMapName = colorMapName;
    }

    public String getColorMapName() {
        return colorMapName == null ? AnsiColorMap.DefaultName : colorMapName;
    }

    public AnsiColorMap getColorMap() {
        return getWrapperDescriptor().getColorMap(colorMapName);
    }

    private static DescriptorImpl getWrapperDescriptor() {
        return Jenkins.getActiveInstance().getDescriptorByType(DescriptorImpl.class);
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ExecutionImpl(context, colorMapName);
    }

    /**
     * Execution for {@link AnsiColorStep}.
     */
    private static class ExecutionImpl extends AbstractStepExecutionImpl {

        private static final long serialVersionUID = 1L;

        private final String colorMapName;

        ExecutionImpl(StepContext context, String colorMapName) {
            super(context);
            this.colorMapName = colorMapName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean start() throws Exception {
            StepContext context = getContext();
            context.get(Run.class).replaceAction(new ColorizedAction(colorMapName));
            EnvironmentExpander currentEnvironment = context.get(EnvironmentExpander.class);
            EnvironmentExpander terminalEnvironment = EnvironmentExpander.constant(Collections.singletonMap("TERM", colorMapName));
            context.newBodyInvoker()
                   .withContext(EnvironmentExpander.merge(currentEnvironment, terminalEnvironment))
                                                   .withCallback(BodyExecutionCallback.wrap(context)).start();
            return false;
        }

    }

    /**
     * Descriptor for {@link AnsiColorStep}.
     */
    @Extension(optional = true)
    public static class StepDescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getFunctionName() {
            return "ansiColor";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        public ListBoxModel doFillColorMapNameItems() {
            return getWrapperDescriptor().doFillColorMapNameItems();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(Run.class);
        }

    }
}
