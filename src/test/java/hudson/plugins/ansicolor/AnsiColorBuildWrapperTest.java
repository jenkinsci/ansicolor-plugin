package hudson.plugins.ansicolor;

import hudson.Functions;
import hudson.Launcher;
import hudson.console.ConsoleNote;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import static org.hamcrest.Matchers.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.LoggerRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

public class AnsiColorBuildWrapperTest {

    public AnsiColorBuildWrapperTest() {
    }

    @Test
    public void testGetColorMapNameNull() {
        AnsiColorBuildWrapper instance = new AnsiColorBuildWrapper(null);
        assertEquals("xterm", instance.getColorMapName());
    }

    @Test
    public void testGetColorMapNameVga() {
        AnsiColorBuildWrapper instance = new AnsiColorBuildWrapper("vga");
        assertEquals("vga", instance.getColorMapName());
    }

    @Test
    public void testDecorateLogger() {
        AnsiColorBuildWrapper ansiColorBuildWrapper = new AnsiColorBuildWrapper(null);
        assertThat(ansiColorBuildWrapper, instanceOf(AnsiColorBuildWrapper.class));
    }

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();
    @Rule
    public LoggerRule logging = new LoggerRule().recordPackage(ConsoleNote.class, Level.FINE).record(ColorConsoleAnnotator.class, Level.FINER);

    @Test
    public void maven() throws Exception {
        story.then(r -> {
            FreeStyleProject p = r.createFreeStyleProject();
            p.getBuildWrappersList().add(new AnsiColorBuildWrapper(null));
            p.getBuildersList().add(new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    // Like Maven 3.6.0 when using (MNG-6380) MAVEN_OPTS=-Djansi.force=true
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] Scanning for projects...");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] ");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] \u001B[1m--------------< \u001B[0;36morg.jenkins-ci.plugins:build-token-root\u001B[0;1m >---------------\u001B[m");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] \u001B[1mBuilding Build Authorization Token Root Plugin 1.5-SNAPSHOT\u001B[m");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] \u001B[1m--------------------------------[ hpi ]---------------------------------\u001B[m");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] ");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] \u001B[1m--- \u001B[0;32mmaven-clean-plugin:3.0.0:clean\u001B[m \u001B[1m(default-clean)\u001B[m @ \u001B[36mbuild-token-root\u001B[0;1m ---\u001B[m");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] \u001B[1m------------------------------------------------------------------------\u001B[m");
                    listener.getLogger().println("[\u001B[1;34mINFO\u001B[m] \u001B[1;32mBUILD SUCCESS\u001B[m");
                    return true;
                }
            });
            FreeStyleBuild b = r.buildAndAssertSuccess(p);
            StringWriter writer = new StringWriter();
            b.getLogText().writeHtmlTo(0L, writer);
            String html = writer.toString();
            System.out.print(html);
            assertThat(html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("[<b><span style=\"color: #1E90FF;\">INFO</span></b>]"),
                    containsString("<b>--------------&lt; </b><span style=\"color: #00CDCD;\">org.jenkins-ci.plugins:build-token-root</span><b> &gt;---------------</b>")));
        });
    }

    @Issue("JENKINS-54133")
    @Test
    public void testWorkflowWrap() throws Exception {
        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue(!Functions.isWindows());
                story.j.createSlave();
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node('!master') {\n"
                        + "  wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm', 'defaultFg': 1, 'defaultBg': 2]) {\n"
                        + "    sh(\"\"\"#!/bin/bash\n"
                        + "      printf 'The following word is supposed to be \\\\e[31mred\\\\e[0m\\\\n'\"\"\"\n"
                        + "    )\n"
                        + "  }\n"
                        + "}"
                ));
                story.j.assertBuildStatusSuccess(p.scheduleBuild2(0));
                StringWriter writer = new StringWriter();
                p.getLastBuild().getLogText().writeHtmlTo(0L, writer);
                String html = writer.toString();
                assertTrue("Failed to match color attribute in following HTML log output:\n" + html,
                    html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*"));
            }
        });
    }
}
