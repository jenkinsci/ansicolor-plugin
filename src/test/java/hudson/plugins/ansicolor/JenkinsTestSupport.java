package hudson.plugins.ansicolor;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assume;
import org.junit.Rule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

public class JenkinsTestSupport {
    private static final int CONSOLE_TAIL_DEFAULT = 150;

    @Rule
    public RestartableJenkinsRule jenkinsRule = new RestartableJenkinsRule();

    protected void assertOutputOnRunningPipeline(
        BooleanSupplier assumption,
        String expectedOutput,
        String notExpectedOutput,
        String pipelineScript,
        boolean useShortLog,
        Map<String, String> properties
    ) {
        assertOutputOnRunningPipeline(assumption, Collections.singletonList(expectedOutput), Collections.singletonList(notExpectedOutput), pipelineScript, useShortLog, properties);
    }

    protected void assertOutputOnRunningPipeline(Collection<String> expectedOutput, Collection<String> notExpectedOutput, String pipelineScript, boolean useShortLog) {
        assertOutputOnRunningPipeline(() -> true, expectedOutput, notExpectedOutput, pipelineScript, useShortLog, Collections.emptyMap());
    }

    protected void assertOutputOnRunningPipeline(Collection<String> expectedOutput, Collection<String> notExpectedOutput, String pipelineScript, boolean useShortLog, Map<String, String> properties) {
        assertOutputOnRunningPipeline(() -> true, expectedOutput, notExpectedOutput, pipelineScript, useShortLog, properties);
    }

    protected void assertOutputOnRunningPipeline(
        BooleanSupplier assumption,
        Collection<String> expectedOutput,
        Collection<String> notExpectedOutput,
        String pipelineScript,
        boolean useShortLog,
        Map<String, String> properties
    ) {
        jenkinsRule.then(r -> {
            Assume.assumeTrue(assumption.getAsBoolean());
            properties.forEach(System::setProperty);
            final WorkflowJob project = jenkinsRule.j.jenkins.createProject(WorkflowJob.class, "test-project-" + JenkinsTestSupport.this.getClass().getSimpleName());
            project.setDefinition(new CpsFlowDefinition(pipelineScript, true));
            jenkinsRule.j.assertBuildStatusSuccess(project.scheduleBuild2(0));
            StringWriter writer = new StringWriter();
            final WorkflowRun lastBuild = project.getLastBuild();
            final long start = useShortLog ? new File(lastBuild.getRootDir(), "log").length() - CONSOLE_TAIL_DEFAULT * 1024 : 0;
            assertTrue(lastBuild.getLogText().writeHtmlTo(start, writer) > 0);
            properties.keySet().forEach(System::clearProperty);
            final String html = writer.toString().replaceAll("<!--.+?-->", "");
            for (String expected : expectedOutput) {
                assertThat(html, containsString(expected));
            }
            for (String notExpected : notExpectedOutput) {
                assertThat(html, not(containsString(notExpected)));
            }
        });
    }

    public static String repeat(String s, int times) {
        return IntStream.range(0, times).mapToObj(i -> s).collect(Collectors.joining());
    }
}
