package hudson.plugins.ansicolor;

import org.junit.Test;

import java.util.Arrays;

public class TimestamperIntegrationTest extends JenkinsTestSupport {

    @Test
    public void canTriggerFunctionalityTimestamperFirst() {
        final String script = "timestamps {" +
            "ansiColor('xterm') {" +
            "echo '\033[34mHello\033[0m \033[33mcolorful\033[0m \033[35mworld!\033[0m'" +
            "}" +
            "}";
        canTriggerFunctionality(script);
    }


    @Test
    public void canTriggerFunctionalityTimestamperLast() {
        final String script = "ansiColor('xterm') {" +
            "timestamps {" +
            "echo '\033[34mHello\033[0m \033[33mcolorful\033[0m \033[35mworld!\033[0m'" +
            "}" +
            "}";
        canTriggerFunctionality(script);
    }

    private void canTriggerFunctionality(String script) {
        assertOutputOnRunningPipeline(
            Arrays.asList(
                "<span style=\"color: #1E90FF;\">Hello</span>",
                "<span style=\"color: #CDCD00;\">colorful</span>",
                "<span style=\"color: #CD00CD;\">world!</span>"
            ),
            Arrays.asList(
                "\033[34mHello",
                "\033[33mcolorful",
                "\033[35mworld!"
            ),
            script,
            false
        );
    }
}
