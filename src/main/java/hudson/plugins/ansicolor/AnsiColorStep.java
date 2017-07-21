package hudson.plugins.ansicolor;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.plugins.ansicolor.AnsiColorBuildWrapper.DescriptorImpl;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.Collections;

import javax.annotation.Nonnull;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.BodyInvoker;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.inject.Inject;

/**
 * Custom pipeline step that can be used without a node and build wrapper.
 */
public class AnsiColorStep extends AbstractStepImpl {

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

    /**
     * Execution for {@link AnsiColorStep}.
     */
    public static class ExecutionImpl extends AbstractStepExecutionImpl {

        private static final long serialVersionUID = 1L;

        @Inject(optional = true)
        private transient AnsiColorStep step;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean start() throws Exception {
            StepContext context = getContext();
            EnvironmentExpander currentEnvironment = context.get(EnvironmentExpander.class);
            EnvironmentExpander terminalEnvironment = EnvironmentExpander.constant(Collections.singletonMap("TERM", step.getColorMapName()));
            context.newBodyInvoker().withContext(createConsoleLogFilter(context))
                   .withContext(EnvironmentExpander.merge(currentEnvironment, terminalEnvironment))
                                                   .withCallback(BodyExecutionCallback.wrap(context)).start();
            return false;
        }

        private ConsoleLogFilter createConsoleLogFilter(StepContext context)
                throws IOException, InterruptedException {
            ConsoleLogFilter original = context.get(ConsoleLogFilter.class);
            ConsoleLogFilter subsequent = new AnsiColorConsoleLogFilter(step.getColorMap());
            return BodyInvoker.mergeConsoleLogFilters(original, subsequent);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void stop(@Nonnull Throwable cause) throws Exception {
            getContext().onFailure(cause);
        }
    }

    /**
     * Descriptor for {@link AnsiColorStep}.
     */
    @Extension(optional = true)
    public static class StepDescriptorImpl extends AbstractStepDescriptorImpl {

        public StepDescriptorImpl() {
            super(ExecutionImpl.class);
        }

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
    }
}
