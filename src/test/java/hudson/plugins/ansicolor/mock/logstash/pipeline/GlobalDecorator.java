package hudson.plugins.ansicolor.mock.logstash.pipeline;

import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Fake decorator pretending to be a logstash-plugin component
 */
public class GlobalDecorator implements TaskListenerDecorator.Factory {
    @CheckForNull
    @Override
    public TaskListenerDecorator of(@NonNull FlowExecutionOwner owner) {
        return null;
    }
}
