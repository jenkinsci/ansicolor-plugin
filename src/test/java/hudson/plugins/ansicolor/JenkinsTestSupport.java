package hudson.plugins.ansicolor;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class JenkinsTestSupport {
    @Rule
    public RestartableJenkinsRule jenkinsRule = new RestartableJenkinsRule();
    private static final int CONSOLE_TAIL_DEFAULT = 150;

    protected void assertOutputOnRunningPipeline(String expectedOutput, String notExpectedOutput, String pipelineScript, boolean useShortLog) {
        assertOutputOnRunningPipeline(Collections.singletonList(expectedOutput), Collections.singletonList(notExpectedOutput), pipelineScript, useShortLog);
    }

    protected void assertOutputOnRunningPipeline(Collection<String> expectedOutput, Collection<String> notExpectedOutput, String pipelineScript, boolean useShortLog) {
        jenkinsRule.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final WorkflowJob project = jenkinsRule.j.jenkins.createProject(WorkflowJob.class, "test-project-" + JenkinsTestSupport.this.getClass().getSimpleName());
                project.setDefinition(new CpsFlowDefinition(pipelineScript, true));
                jenkinsRule.j.assertBuildStatusSuccess(project.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                final WorkflowRun lastBuild = project.getLastBuild();
                final long start = useShortLog ? new File(lastBuild.getRootDir(), "log").length() - CONSOLE_TAIL_DEFAULT * 1024 : 0;
                assertTrue(lastBuild.getLogText().writeHtmlTo(start, writer) > 0);
                final String html = writer.toString().replaceAll("<!--.+?-->", "");
                assertThat(html).contains(expectedOutput);
                assertThat(html).doesNotContain(notExpectedOutput);
            }
        });
    }

    protected static String repeat(String s, int times) {
        return IntStream.range(0, times).mapToObj(i -> s).collect(Collectors.joining());
    }
}
