package hudson.plugins.ansicolor;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.LoggerRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.StringWriter;
import java.util.logging.Level;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ColorConsoleAnnotatorTest {

    public ColorConsoleAnnotatorTest() {
    }

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();
    @Rule
    public LoggerRule logging = new LoggerRule().record(ColorConsoleAnnotator.class, Level.FINER);

    @Test
    public void testGlobalPipelineColorMap() {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).setGlobalColorMapName("xterm");
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                                "echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'"
                        , true));
                story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
                String html = writer.toString();
                assertTrue("Failed to match color attribute in following HTML log output:\n" + html,
                        html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"));
            }
        });
    }

    @Test
    public void testNoGlobalPipelineColorMap() {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).setGlobalColorMapName(null);
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                                "echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'"
                        , true));
                story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
                String html = writer.toString();
                assertFalse("Color attribute was applied in following HTML log output even though the color map was not globally enabled:\n" + html,
                        html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"));
            }
        });
    }
}
