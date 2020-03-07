package hudson.plugins.ansicolor;

import hudson.Functions;
import hudson.Launcher;
import hudson.console.ConsoleNote;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AnsiColorBuildWrapperTest {
    private static final String ESC = "\033";
    private static final String CLR = ESC + "[2K";

    @SuppressWarnings("unused")
    private enum CSI {
        CUU("A", 1),
        CUD("B", 1),
        CUF("C", 1),
        CUB("D", 1),
        CNL("E", 1),
        CPL("F", 1),
        CHA("G", 1),
        CUP("H", 2),
        ED("J", 1),
        EL("K", 1),
        SU("S", 1),
        SD("T", 1),
        HVP("f", 2),
        AUXON("5i", 0),
        AUXOFF("4i", 0),
        DSR("6n", 0),
        SCP("s", 0),
        RCP("u", 0),
        ;
        private final String code;
        private final int paramsAmount;

        CSI(String code, int paramsAmount) {
            this.code = code;
            this.paramsAmount = paramsAmount;
        }
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
    public void maven() {
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
                    listener.getLogger()
                        .println(
                            "[\u001B[1;34mINFO\u001B[m] \u001B[1m--- \u001B[0;32mmaven-clean-plugin:3.0.0:clean\u001B[m \u001B[1m(default-clean)\u001B[m @ \u001B[36mbuild-token-root\u001B[0;1m ---\u001B[m");
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
            assertThat(
                html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("[<b><span style=\"color: #1E90FF;\">INFO</span></b>]"),
                    containsString("<b>--------------&lt; </b><span style=\"color: #00CDCD;\">org.jenkins-ci.plugins:build-token-root</span><b> &gt;---------------</b>")
                )
            );
        });
    }

    @Test
    public void testMultilineEscapeSequence() {
        story.then(r -> {
            FreeStyleProject p = r.createFreeStyleProject();
            p.getBuildWrappersList().add(new AnsiColorBuildWrapper(null));
            p.getBuildersList().add(new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    listener.getLogger().println("\u001B[1;34mThis text should be bold and blue");
                    listener.getLogger().println("Still bold and blue");
                    listener.getLogger().println("\u001B[mThis text should be normal");
                    return true;
                }
            });
            FreeStyleBuild b = r.buildAndAssertSuccess(p);
            StringWriter writer = new StringWriter();
            b.getLogText().writeHtmlTo(0L, writer);
            String html = writer.toString();
            System.out.print(html);
            String nl = System.lineSeparator();
            assertThat(
                html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("<b><span style=\"color: #1E90FF;\">This text should be bold and blue" + nl + "</span></b>"),
                    containsString("<b><span style=\"color: #1E90FF;\">Still bold and blue" + nl + "</span></b>"),
                    not(containsString("\u001B[m"))
                )
            );
        });
    }

    @Test
    public void testDefaultForegroundBackground() {
        story.then(r -> {
            FreeStyleProject p = r.createFreeStyleProject();
            // The VGA ColorMap sets default foreground and background colors.
            p.getBuildWrappersList().add(new AnsiColorBuildWrapper("vga"));
            p.getBuildersList().add(new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    listener.getLogger().println("White on black");
                    listener.getLogger().println("\u001B[1;34mBold and blue on black");
                    listener.getLogger().println("Still bold and blue on black\u001B[mBack to white on black");
                    return true;
                }
            });
            FreeStyleBuild b = r.buildAndAssertSuccess(p);
            StringWriter writer = new StringWriter();
            b.getLogText().writeHtmlTo(0L, writer);
            String html = writer.toString();
            System.out.print(html);
            String nl = System.lineSeparator();
            assertThat(
                html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("<div style=\"background-color: #000000;color: #AAAAAA;\">White on black" + nl + "</div>"),
                    containsString("<div style=\"background-color: #000000;color: #AAAAAA;\"><b><span style=\"color: #0000AA;\">Bold and blue on black" + nl + "</span></b></div>"),
                    containsString(
                        "<div style=\"background-color: #000000;color: #AAAAAA;\"><b><span style=\"color: #0000AA;\">Still bold and blue on black</span></b>Back to white on black" + nl + "</div>"
                    )
                )
            );
        });
    }

    @Issue("JENKINS-54133")
    @Test
    public void testWorkflowWrap() {
        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue(!Functions.isWindows());
                story.j.createSlave();
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                    "node('!master') {\n"
                        + "  wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {\n"
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
                assertTrue(
                    "Failed to match color attribute in following HTML log output:\n" + html,
                    html.replaceAll("<!--.+?-->", "").matches("(?s).*<span style=\"color: #CD0000;\">red</span>.*")
                );
            }
        });
    }

    @Test
    public void testNonAscii() {
        story.then(r -> {
            FreeStyleProject p = r.createFreeStyleProject();
            p.getBuildWrappersList().add(new AnsiColorBuildWrapper(null));
            p.getBuildersList().add(new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    listener.getLogger().println("\033[94;1m[ INFO ] Récupération du numéro de version de l'application\033[0m");
                    listener.getLogger().println("\033[94;1m[ INFO ] ビルドのコンソール出力を取得します。\033[0m");
                    // There are 3 smiley face emojis in this String
                    listener.getLogger().println("\033[94;1m[ INFO ] 😀😀\033[0m😀");
                    return true;
                }
            });
            FreeStyleBuild b = r.buildAndAssertSuccess(p);
            StringWriter writer = new StringWriter();
            b.getLogText().writeHtmlTo(0L, writer);
            String html = writer.toString();
            assertThat(
                html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("<span style=\"color: #4682B4;\"><b>[ INFO ] Récupération du numéro de version de l'application</b></span>"),
                    containsString("<span style=\"color: #4682B4;\"><b>[ INFO ] ビルドのコンソール出力を取得します。</b></span>"),
                    containsString("<span style=\"color: #4682B4;\"><b>[ INFO ] 😀😀</b></span>😀")
                )
            );
        });
    }

    @Issue("JENKINS-55139")
    @Test
    public void testTerraform() {
        story.then(r -> {
            FreeStyleProject p = r.createFreeStyleProject();
            p.getBuildWrappersList().add(new AnsiColorBuildWrapper(null));
            p.getBuildersList().add(new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    // Mimics terraform, c.f. `docker run --rm hashicorp/terraform plan 2>&1 | od -t a`
                    listener.getLogger().println("\033[31m");
                    listener.getLogger().println("\033[1m\033[31mError: \033[0m\033[0m\033[1mNo configuration files found!");
                    listener.getLogger().println("bold text blurb\033[0m");
                    listener.getLogger().println("\033[0m\033[0m\033[0m");
                    return true;
                }
            });
            FreeStyleBuild b = r.buildAndAssertSuccess(p);
            StringWriter writer = new StringWriter();
            b.getLogText().writeHtmlTo(0L, writer);
            String html = writer.toString();
            System.out.print(html);
            assertThat(
                html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("Error"),
                    containsString("No configuration files found!"),
                    not(containsString("\033[0m"))
                )
            );
        });
    }

    @Issue("JENKINS-55139")
    @Test
    public void testRedundantResets() {
        story.then(r -> {
            FreeStyleProject p = r.createFreeStyleProject();
            p.getBuildWrappersList().add(new AnsiColorBuildWrapper(null));
            p.getBuildersList().add(new TestBuilder() {
                @Override
                public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                    listener.getLogger().println("[Foo] \033[0m[\033[0m\033[0minfo\033[0m] \033[0m\033[0m\033[32m- this text is green\033[0m\033[0m");
                    return true;
                }
            });
            FreeStyleBuild b = r.buildAndAssertSuccess(p);
            StringWriter writer = new StringWriter();
            b.getLogText().writeHtmlTo(0L, writer);
            String html = writer.toString();
            System.out.print(html);
            assertThat(
                html.replaceAll("<!--.+?-->", ""),
                allOf(
                    containsString("[Foo]"),
                    containsString("[info]"),
                    containsString("<span style=\"color: #00CD00;\">- this text is green</span>"),
                    not(containsString("\033[0m"))
                )
            );
        });
    }

    @Test
    public void canWorkWithMovingSequences() {
        final String op1 = "Creating container_1";
        final String op2 = "Creating container_2";
        final String up2lines = csi(2, CSI.CUU);
        final String down2lines = csi(2, CSI.CUD);
        final String back7chars = csi(7, CSI.CUB);
        final String forward4chars = csi(4, CSI.CUF);
        final Consumer<PrintStream> inputProvider = stream -> {
            stream.println(op1 + " ...");
            stream.println(op2 + " ...");
            stream.print(up2lines);
            stream.print(CLR);
            stream.print(op1 + " ... " + "done\r");
            stream.print(down2lines);
            stream.print(back7chars);
            stream.print(forward4chars);
        };

        assertCorrectOutput(
            Arrays.asList(op1 + " ... done", op2 + " ..."),
            Arrays.asList(up2lines, CLR, down2lines, back7chars, forward4chars),
            inputProvider
        );
    }

    @Test
    public void canWorkWithVariousCsiSequences() {
        final String txt0 = "Test various sequences begin";
        final List<String> csiSequences = Arrays.stream(CSI.values()).map(csi -> {
            switch (csi.paramsAmount) {
                case 0:
                    return csi(csi);
                case 1:
                    return csi(6, csi);
                case 2:
                    return csi(6, 4, csi);
            }
            throw new IllegalArgumentException("Not supported amount of params");
        }).collect(Collectors.toList());
        final String txt1 = "Test various sequences end";
        final Consumer<PrintStream> inputProvider = stream -> {
            stream.println(txt0);
            csiSequences.forEach(stream::println);
            stream.println(txt1);
        };

        assertCorrectOutput(Arrays.asList(txt0, txt1), csiSequences, inputProvider);
    }

    @Issue("172")
    @Test
    public void canRenderSgrNormalIntensity() {
        final String sgrReset = sgr(0);
        final String msg0 = "lightgreen and reset sgr ";
        final String sgrLightGreen = sgr(92);
        final String msg1 = "lightgreen and combined reset sgr ";
        final String sgrLightGreenRegular = sgr(92, 22);
        final String msg2 = "lightgreen bold ";
        final String msg3 = "lightgreen normal";
        final String sgrBold = sgr(1);
        final String sgrNormal = sgr(22);
        final String msg4 = "lightgreen and separate reset sgr ";
        final String msg5 = "this text should just be normal ";

        final Consumer<PrintStream> inputProvider = stream -> {
            stream.println(sgrLightGreen + msg0 + sgrReset);
            stream.println(sgrLightGreenRegular + msg1 + sgrReset);
            stream.println(sgrLightGreen + sgrBold + msg2 + sgrLightGreen + sgrNormal + msg3 + sgrReset);
            stream.println(sgrLightGreen + sgrNormal + msg4 + sgrReset);
            stream.println(sgrNormal + msg5 + sgrReset);
        };
        assertCorrectOutput(
            Arrays.asList(
                "<span style=\"color: #00FF00;\">" + msg0 + "</span>",
                "<span style=\"color: #00FF00;\">" + msg1 + "</span>",
                "<span style=\"color: #00FF00;\"><b>" + msg2 + "</b></span>",
                "<span style=\"color: #00FF00;\">" + msg3 + "</span>",
                "<span style=\"color: #00FF00;\">" + msg4 + "</span>",
                msg5
            ),
            Arrays.asList(sgrReset, sgrLightGreen, sgrLightGreenRegular, sgrBold, sgrNormal),
            inputProvider
        );
    }

    @Test
    public void canRenderSgrFaintIntensity() {
        final String sgrReset = sgr(0);
        final String msg0 = "lightblue and also faint";
        final String sgrLightBlueFaint = sgr(94, 2);
        final String msg1 = "lightblue and";
        final String msg2 = "also faint";
        final String sgrLightBlue = sgr(94);
        final String sgrFaint = sgr(2);
        final String msg3 = "normal ordinary text";
        final String sgrNormal = sgr(22);

        final Consumer<PrintStream> inputProvider = stream -> {
            stream.println(sgrLightBlueFaint + msg0 + sgrReset);
            stream.println(sgrLightBlue + msg1 + sgrFaint + msg2 + sgrReset);
            stream.println(sgrLightBlue + sgrFaint + sgrNormal + msg3 + sgrReset);
        };
        assertCorrectOutput(
            Arrays.asList(
                "<span style=\"color: #4682B4;\"><span style=\"font-weight: lighter;\">" + msg0 + "</span></span>",
                "<span style=\"color: #4682B4;\">" + msg1 + "<span style=\"font-weight: lighter;\">" + msg2 + "</span></span>",
                msg3
            ),
            Arrays.asList(sgrReset, sgrLightBlueFaint, sgrLightBlue, sgrFaint, sgrNormal),
            inputProvider
        );
    }

    private static String csi(CSI csi) {
        return csi("", csi);
    }

    private static String csi(int n, CSI csi) {
        return csi(String.valueOf(n), csi);
    }

    private static String csi(int n, int m, CSI csi) {
        return csi(n + ";" + m, csi);
    }

    private static String csi(String nm, CSI csi) {
        return ESC + "[" + nm + csi.code;
    }

    private static String sgr(int... sgrParam) {
        return ESC + "[" + Arrays.stream(sgrParam).boxed().map(String::valueOf).collect(Collectors.joining(";")) + "m";
    }

    private void assertCorrectOutput(Collection<String> expectedOutput, Collection<String> notExpectedOutput, Consumer<PrintStream> inputProvider) {
        story.then(r -> {
            final String html = runBuildWithPlugin(r, inputProvider).replaceAll("<!--.+?-->", "");
            expectedOutput.forEach(s -> assertThat(html, containsString(s)));
            notExpectedOutput.forEach(s -> assertThat("Test failed for sequence: " + s.replace(ESC, "ESC"), html, not(containsString(s))));
        });
    }

    private String runBuildWithPlugin(JenkinsRule rule, Consumer<PrintStream> inputProvider) throws Exception {
        final FreeStyleProject p = rule.createFreeStyleProject();
        p.getBuildWrappersList().add(new AnsiColorBuildWrapper(null));
        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                inputProvider.accept(listener.getLogger());
                return true;
            }
        });
        final FreeStyleBuild b = rule.buildAndAssertSuccess(p);
        final StringWriter writer = new StringWriter();
        b.getLogText().writeHtmlTo(0L, writer);
        return writer.toString();
    }
}
