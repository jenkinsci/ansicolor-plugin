package hudson.plugins.ansicolor;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.ansicolor.AnsiColorBuildWrapper.DescriptorImpl;
import hudson.plugins.ansicolor.action.ActionNote;
import hudson.plugins.ansicolor.action.ColorizedAction;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        this.colorMapName = Optional.ofNullable(colorMapName).orElseGet(() ->
            Optional.ofNullable(Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).getGlobalColorMapName()).orElse(AnsiColorMap.DefaultName));
    }

    private static DescriptorImpl getWrapperDescriptor() {
        return Jenkins.get().getDescriptorByType(DescriptorImpl.class);
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

            EnvironmentExpander currentEnvironment = context.get(EnvironmentExpander.class);
            EnvironmentExpander terminalEnvironment = EnvironmentExpander.constant(Collections.singletonMap("TERM", colorMapName));
            context.newBodyInvoker()
                .withContext(EnvironmentExpander.merge(currentEnvironment, terminalEnvironment))
                .withCallback(new AnsiColorExecution(colorMapName))
                .start();
            return false;
        }
    }

    /**
     * Descriptor for {@link AnsiColorStep}.
     */
    @Extension(optional = true)
    public static class StepDescriptorImpl extends StepDescriptor {

        @Nonnull
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

    private static class AnsiColorExecution extends BodyExecutionCallback {
        private static final Logger LOGGER = Logger.getLogger(AnsiColorExecution.class.getName());

        private static final Map<Class<?>, String[]> EXTENSIONS_NL = new HashMap<>();

        private final String colorMapName;

        private Boolean needsPrintln;

        static {
            EXTENSIONS_NL.put(DynamicContext.Typed.class, new String[]{"kubernetes.pipeline.SecretsMasker"});
            EXTENSIONS_NL.put(TaskListenerDecorator.Factory.class, new String[]{
                "timestamper.pipeline.GlobalDecorator",
                "logstash.pipeline.GlobalDecorator",
            });
        }

        public AnsiColorExecution(String colorMapName) {
            this.colorMapName = colorMapName;
        }

        @Override
        public void onStart(StepContext context) {
            issueAction(context, new ColorizedAction(colorMapName, ColorizedAction.Command.START));
            super.onStart(context);
        }

        @Override
        public void onSuccess(StepContext context, Object result) {
            issueAction(context, new ColorizedAction(colorMapName, ColorizedAction.Command.STOP));
            context.onSuccess(result);
        }

        @Override
        public void onFailure(StepContext context, Throwable t) {
            issueAction(context, new ColorizedAction(colorMapName, ColorizedAction.Command.STOP));
            context.onFailure(t);
        }

        private void issueAction(StepContext context, ColorizedAction action) {
            try {
                final TaskListener taskListener = context.get(TaskListener.class);
                final Run<?, ?> run = context.get(Run.class);
                if (taskListener != null && run != null) {
                    run.addAction(action);
                    taskListener.annotate(new ActionNote(action));
                    ensureRendering(taskListener);
                    final ColorizedAction currentAction = new ColorizedAction(action.getColorMapName(), ColorizedAction.Command.CURRENT);
                    if (action.getCommand().equals(ColorizedAction.Command.START)) {
                        run.addAction(currentAction);
                    } else {
                        run.removeAction(currentAction);
                    }
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.WARNING, "Could not annotate. Ansicolor plugin will not work correctly.", e);
            }
        }

        private boolean needsPrintln() {
            if (needsPrintln == null) {
                needsPrintln = EXTENSIONS_NL.entrySet().stream().anyMatch(e ->
                    ExtensionList.lookup(e.getKey()).stream().map(ext -> ext.getClass().getName()).anyMatch(n -> Arrays.stream(e.getValue()).anyMatch(n::contains))
                );
            }
            return needsPrintln;
        }

        private void ensureRendering(TaskListener taskListener) {
            if (needsPrintln()) {
                taskListener.getLogger().println();
            }
        }
    }
}
