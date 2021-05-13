package hudson.plugins.ansicolor.mock.logstash.pipeline;

import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Fake decorator pretending to be a logstash-plugin component
 */
public class GlobalDecorator implements TaskListenerDecorator.Factory {
    @CheckForNull
    @Override
    public TaskListenerDecorator of(@Nonnull FlowExecutionOwner owner) {
        return null;
    }
}
