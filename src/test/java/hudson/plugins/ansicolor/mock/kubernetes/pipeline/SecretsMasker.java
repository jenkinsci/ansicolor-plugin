package hudson.plugins.ansicolor.mock.kubernetes.pipeline;

import org.jenkinsci.plugins.workflow.steps.DynamicContext;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Fake masker pretending to be a kubernetes-plugin component
 */
public class SecretsMasker extends DynamicContext.Typed<String> {
    @Nonnull
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
