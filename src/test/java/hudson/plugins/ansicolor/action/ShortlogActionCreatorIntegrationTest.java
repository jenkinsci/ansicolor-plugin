package hudson.plugins.ansicolor.action;

import hudson.plugins.ansicolor.JenkinsTestSupport;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShortlogActionCreatorIntegrationTest extends JenkinsTestSupport {

    @Test
    public void canAnotateLongLogOutputInShortlog() {
        final String eol = System.lineSeparator();
        final String as1k = IntStream.range(0, 1024).mapToObj(i -> "a").collect(Collectors.joining());
        final String as10k = IntStream.range(0, 10 * 1024).mapToObj(i -> as1k + eol).collect(Collectors.joining());
        final String bs1k = IntStream.range(0, 1024).mapToObj(i -> "b").collect(Collectors.joining());
        final String bs30k = IntStream.range(0, 30).mapToObj(i -> bs1k + eol).collect(Collectors.joining());
        final String cs1k = IntStream.range(0, 1024).mapToObj(i -> "c").collect(Collectors.joining());
        final String cs50k = IntStream.range(0, 50).mapToObj(i -> cs1k + eol).collect(Collectors.joining());
        final String ds1k = IntStream.range(0, 1024).mapToObj(i -> "d").collect(Collectors.joining());
        final String ds50k = IntStream.range(0, 50).mapToObj(i -> ds1k + eol).collect(Collectors.joining());

//        final String as10k = IntStream.range(0, 10 * 1024).mapToObj(i -> "a").collect(Collectors.joining());
        final String script = "echo '\033[32mBeginning\033[0m'\n" +
            "ansiColor('vga') {\n" +
            "    echo '\033[32m" + as10k + "\033[0m'\n" +
            "}\n" +
            "ansiColor('xterm') {\n" +
            "    echo '\033[32m" + bs30k + "\033[0m'\n" +
            "}\n" +
            "ansiColor('css') {\n" +
            "    echo '\033[32m" + cs50k + "\033[0m'\n" +
            "    echo '\033[32m" + ds50k + "\033[0m'\n" +
            "}\n" +
            "echo 'End'";

        assertOutputOnRunningPipeline(
            Arrays.asList(
//                "<span style=\"color: #00CD00;\">" + bs30k + "</span>",
                "<span style=\"color: green;\">" + cs50k + "</span>",
                "<span style=\"color: green;\">" + ds50k + "</span>",
                "End"
            ),
            Arrays.asList(
                "Beginning",
                "<span style=\"color: #00AA00;\">a",
                "\033[32m" + as10k + "\033[0m",
                "\033[32m" + bs30k + "\033[0m",
                "\033[32m" + cs50k + "\033[0m",
                "\033[32m" + ds50k + "\033[0m"
            ),
            script,
            true
        );
    }
}
