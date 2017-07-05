package hudson.plugins.ansicolor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class AnsiColorStepTest {

    public AnsiColorStepTest() {
    }

    @Test
    public void testGetColorMapNameNull() {
        AnsiColorStep instance = new AnsiColorStep(null);
        assertEquals("xterm", instance.getColorMapName());
    }

    @Test
    public void testGetColorMapNameVga() {
        AnsiColorStep instance = new AnsiColorStep("vga");
        assertEquals("vga", instance.getColorMapName());
    }

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Test
    public void testPipelineStep() throws Exception {
        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "ansiColor('xterm') {\n"
                                + "  echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'\n"
                                + " echo \"TERM=${env.TERM}\""
                                + "}"
                        , true));
                WorkflowRun run = story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                p.getLastBuild().getLogText().writeHtmlTo(0L, writer);
                String html = writer.toString();
                story.j.assertLogContains("TERM=xterm", run);
                assertTrue("Failed to match color attribute in following HTML log output:\n" + html,
                        html.matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"));
            }
        });
    }
}
