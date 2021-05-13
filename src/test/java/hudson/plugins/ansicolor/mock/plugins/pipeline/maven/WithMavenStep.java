package hudson.plugins.ansicolor.mock.plugins.pipeline.maven;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import java.util.Set;

/**
 * Fake step pretending to be a pipeline-maven-plugin component
 */
public class WithMavenStep extends Step {

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return null;
    }

    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return null;
        }

        @Override
        public String getFunctionName() {
            return "fake function";
        }
    }
}
