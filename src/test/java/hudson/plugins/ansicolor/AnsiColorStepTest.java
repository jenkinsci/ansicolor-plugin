package hudson.plugins.ansicolor;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.LoggerRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    @Rule
    public LoggerRule logging = new LoggerRule().record(ColorConsoleAnnotator.class, Level.FINER);

    @Test
    public void testPipelineStep() {
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
                assertTrue(p.getLastBuild().getLogText().writeHtmlTo(0L, writer) > 0);
                String html = writer.toString();
                story.j.assertLogContains("TERM=xterm", run);
                assertTrue(
                    "Failed to match color attribute in following HTML log output:\n" + html,
                    html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*")
                );
            }
        });
    }

    @Issue("180")
    @Test
    public void canRenderMultiplePipelineSteps() {
        final String script = "echo '\033[32mbefore\033[0m'\n" +
            "ansiColor('vga') {\n" +
            "    echo '\033[32mstep one\033[0m'\n" +
            "}\n" +
            "echo '\033[32mbetween two steps\033[0m'\n" +
            "ansiColor('xterm') {\n" +
            "    echo '\033[32mstep two\033[0m'\n" +
            "}\n" +
            "echo '\033[32mafter step two\033[0m'\n" +
            "ansiColor {\n" +
            "    echo '\033[32mstep three\033[0m'\n" +
            "}\n" +
            "echo '\033[32mafter step three\033[0m'";

        assertOutputOnRunningPipeline(
            Arrays.asList(
                "\033[32mbefore\033[0m",
                "<span style=\"color: #00AA00;\">step one</span>",
                "\033[32mbetween two steps\033[0m",
                "<span style=\"color: #00CD00;\">step two</span>",
                "\033[32mafter step two\033[0m",
                "<span style=\"color: #00CD00;\">step three</span>",
                "\033[32mafter step three\033[0m"
            ),
            Arrays.asList(
                "<span style=\"color: #00AA00;\">before</span>",
                "\033[32mstep one\033[0m",
                "<span style=\"color: #00AA00;\">between two steps</span>",
                "\033[32mstep two\033[0m",
                "<span style=\"color: #00AA00;\">after step two</span>",
                "\033[32mstep three\033[0m",
                "<span style=\"color: #00AA00;\">after step three</span>"
            ),
            script
        );
    }

    @Issue("JENKINS-61598")
    @Test
    public void willNotLeakFormattingToMetadataLines() {
        final String script = "ansiColor('xterm') {\n" +
            "    echo '\033[33mYellow words, white background.'\n" +
            "    echo '\033[35mMagenta words, white background.'\n" +
            "}";
        String nl = System.lineSeparator();
        assertOutputOnRunningPipeline(
            Arrays.asList(
                "<span style=\"color: #CDCD00;\">Yellow words, white background." + nl + "</span>",
                "[Pipeline] echo",
                "<span style=\"color: #CD00CD;\">Magenta words, white background." + nl + "</span>",
                "[Pipeline] }"
            ),
            Arrays.asList(
                "\033[33mYellow words, white background.",
                "<span style=\"color: #CDCD00;\">[Pipeline] echo",
                "\033[35mMagenta words, white background.",
                "<span style=\"color: #CD00CD;\">[Pipeline] }" + nl + "</span>"
            ),
            script
        );
    }

    private void assertOutputOnRunningPipeline(Collection<String> expectedOutput, Collection<String> notExpectedOutput, String pipelineScript) {
        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final WorkflowJob project = story.j.jenkins.createProject(WorkflowJob.class, "p");
                project.setDefinition(new CpsFlowDefinition(pipelineScript, true));
                story.j.assertBuildStatusSuccess(project.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                assertTrue(project.getLastBuild().getLogText().writeHtmlTo(0, writer) > 0);
                final String html = writer.toString().replaceAll("<!--.+?-->", "");
                assertThat(html).contains(expectedOutput);
                assertThat(html).doesNotContain(notExpectedOutput);
            }
        });
    }
}
