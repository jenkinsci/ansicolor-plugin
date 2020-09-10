package hudson.plugins.ansicolor;

import hudson.ExtensionList;
import hudson.model.queue.QueueTaskFuture;
import hudson.plugins.ansicolor.mock.kubernetes.pipeline.SecretsMasker;
import hudson.plugins.ansicolor.mock.timestamper.pipeline.GlobalDecorator;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;
import org.jenkinsci.plugins.workflow.steps.DynamicContext;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.LoggerRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.mockito.Mockito;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

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

    @Issue("200")
    @Test
    public void canRenderLongOutputWhileBuildStillRunning() {
        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final String a1k = JenkinsTestSupport.repeat("a", 1024);
                final String script = "ansiColor('xterm') {\n" +
                    "for (i = 0; i < 1000; i++) {" +
                    "echo '\033[32m" + a1k + "\033[0m'\n" +
                    "}" +
                    "}";
                final WorkflowJob project = story.j.jenkins.createProject(WorkflowJob.class, "canRenderLongOutputWhileBuildStillRunning");
                project.setDefinition(new CpsFlowDefinition(script, true));
                QueueTaskFuture<WorkflowRun> runFuture = project.scheduleBuild2(0);
                assertNotNull(runFuture);
                final WorkflowRun lastBuild = runFuture.waitForStart();
                await().pollInterval(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(150)).until(() -> {
                    StringWriter writer = new StringWriter();
                    final int skipInitialStartAction = 3000;
                    assertTrue(lastBuild.getLogText().writeHtmlTo(skipInitialStartAction, writer) > 0);
                    final String html = writer.toString().replaceAll("<!--.+?-->", "");
                    return !runFuture.isDone() && html.contains("<span style=\"color: #00CD00;\">" + a1k + "</span>") && !html.contains("\033[32m");
                });
            }
        });
    }

    @Test
    public void willPrintAdditionalNlOnKubernetesPlugin() {
        ExtensionList.lookup(DynamicContext.Typed.class).add(0, new SecretsMasker());
        assertNlsOnRunningPipeline();
    }

    @Test
    public void willPrintAdditionalNlOnTimestamperPlugin() {
        ExtensionList.lookup(TaskListenerDecorator.Factory.class).add(0, new GlobalDecorator());
        assertNlsOnRunningPipeline();
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

    private void assertNlsOnRunningPipeline() {
        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final String script = "ansiColor('xterm') {\n" +
                    "echo '\033[34mHello\033[0m \033[33mcolorful\033[0m \033[35mworld!\033[0m'" +
                    "}";
                final WorkflowJob project = story.j.jenkins.createProject(WorkflowJob.class, "willPrintAdditionalNlOnKubernetesPlugin");
                project.setDefinition(new CpsFlowDefinition(script, true));
                story.j.assertBuildStatusSuccess(project.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                assertTrue(project.getLastBuild().getLogText().writeHtmlTo(0, writer) > 0);
                final String html = writer.toString().replaceAll("<!--.+?-->", "")
                    .replaceAll("</span>", "")
                    .replaceAll("<span.+?>", "")
                    .replaceAll("<div.+?/div>", "");
                final String nl = System.lineSeparator();
                assertThat(html).contains("ansiColor" + nl + "[Pipeline] {" + nl + nl).contains("[Pipeline] }" + nl + nl + "[Pipeline] // ansiColor");
            }
        });
    }
}
