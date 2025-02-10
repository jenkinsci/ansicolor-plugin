package hudson.plugins.ansicolor;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Arrays;

@WithJenkins
class TimestamperIntegrationTest extends JenkinsTestSupport {

    @Test
    void canTriggerFunctionalityTimestamperFirst(JenkinsRule jenkinsRule) throws Exception {
        final String script = "timestamps {" +
            "ansiColor('xterm') {" +
            "echo '\033[34mHello\033[0m \033[33mcolorful\033[0m \033[35mworld!\033[0m'" +
            "}" +
            "}";
        canTriggerFunctionality(jenkinsRule, script);
    }


    @Test
    void canTriggerFunctionalityTimestamperLast(JenkinsRule jenkinsRule) throws Exception {
        final String script = "ansiColor('xterm') {" +
            "timestamps {" +
            "echo '\033[34mHello\033[0m \033[33mcolorful\033[0m \033[35mworld!\033[0m'" +
            "}" +
            "}";
        canTriggerFunctionality(jenkinsRule, script);
    }

    private void canTriggerFunctionality(JenkinsRule jenkinsRule, String script) throws Exception {
        assertOutputOnRunningPipeline(
            jenkinsRule,
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
