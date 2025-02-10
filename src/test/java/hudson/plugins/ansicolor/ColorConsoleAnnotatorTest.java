package hudson.plugins.ansicolor;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class ColorConsoleAnnotatorTest {

    @Test
    void testGlobalPipelineColorMap(JenkinsRule jenkinsRule) throws Exception {
        Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).setGlobalColorMapName("xterm");
        WorkflowJob p = jenkinsRule.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'", true));
        jenkinsRule.assertBuildStatusSuccess(p.scheduleBuild2(0));
        StringWriter writer = new StringWriter();
        assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
        String html = writer.toString();
        assertTrue(
            html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"),
            "Failed to match color attribute in following HTML log output:\n" + html
        );
    }

    @Test
    void testNoGlobalPipelineColorMap(JenkinsRule jenkinsRule) throws Exception {
        Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).setGlobalColorMapName(null);
        WorkflowJob p = jenkinsRule.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("echo 'The following word is supposed to be \\u001B[31mred\\u001B[0m'", true));
        jenkinsRule.assertBuildStatusSuccess(p.scheduleBuild2(0));
        StringWriter writer = new StringWriter();
        assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
        String html = writer.toString();
        assertFalse(
            html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"),
            "Color attribute was applied in following HTML log output even though the color map was not globally enabled:\n" + html
        );
    }
}
