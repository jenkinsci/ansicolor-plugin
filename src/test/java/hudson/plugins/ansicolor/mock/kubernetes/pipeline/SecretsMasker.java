package hudson.plugins.ansicolor.mock.kubernetes.pipeline;

import org.jenkinsci.plugins.workflow.steps.DynamicContext;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;

/**
 * Fake masker pretending to be a kubernetes-plugin component
 */
public class SecretsMasker extends DynamicContext.Typed<String> {
    @NonNull
    @Override
    protected Class<String> type() {
        return String.class;
    }

    @CheckForNull
    @Override
    protected String get(DelegatedContext context) throws IOException, InterruptedException {
        return null;
    }
}
