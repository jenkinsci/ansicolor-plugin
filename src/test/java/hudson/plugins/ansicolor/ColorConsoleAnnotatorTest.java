package hudson.plugins.ansicolor;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.LoggerRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.StringWriter;
import java.util.logging.Level;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ColorConsoleAnnotatorTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public RestartableJenkinsRule jenkinsRule = new RestartableJenkinsRule();

    @Rule
    public LoggerRule logging = new LoggerRule().record(ColorConsoleAnnotator.class, Level.FINER);

    @Test
    public void testGlobalPipelineColorMap() {
        jenkinsRule.then(r -> {
            Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).setGlobalColorMapName("xterm");
            WorkflowJob p = jenkinsRule.j.jenkins.createProject(WorkflowJob.class, "p");
            p.setDefinition(new CpsFlowDefinition("echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'", true));
            jenkinsRule.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
            StringWriter writer = new StringWriter();
            assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
            String html = writer.toString();
            assertTrue(
                "Failed to match color attribute in following HTML log output:\n" + html,
                html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*")
            );
        });
    }

    @Test
    public void testNoGlobalPipelineColorMap() {
        jenkinsRule.then(r -> {
            Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).setGlobalColorMapName(null);
            WorkflowJob p = jenkinsRule.j.jenkins.createProject(WorkflowJob.class, "p");
            p.setDefinition(new CpsFlowDefinition("echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'", true));
            jenkinsRule.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
            StringWriter writer = new StringWriter();
            assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
            String html = writer.toString();
            assertFalse(
                "Color attribute was applied in following HTML log output even though the color map was not globally enabled:\n" + html,
                html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*")
            );
        });
    }
}
