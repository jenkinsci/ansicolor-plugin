/*
 * The MIT License
 *
 * Copyright 2018 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.ansicolor;

import hudson.console.ConsoleAnnotationOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.plugins.workflow.log.ConsoleAnnotators;
import org.junit.AssumptionViolatedException; // Ignore seems to be ignored in this context
import org.junit.ClassRule;
import org.junit.BeforeClass;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LoggerRule;

/**
 * Checks which kinds of console notes are successfully pregenerated for use in a remoted filter.
 */
@Issue("JENKINS-54133")
public class AnsiColorConsoleLogFilterTest extends AnsiHtmlOutputStreamTest {

    @ClassRule
    public static LoggerRule logging = new LoggerRule().record(AnsiColorConsoleLogFilter.class, Level.FINE);

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();
    
    private static DumbSlave s;

    @BeforeClass
    public static void createSlave() throws Exception {
        s = r.createOnlineSlave();
    }

    @Override
    protected String annotate(String text, AnsiColorMap colorMap) throws IOException {
        StringWriter sw = new StringWriter();
        try (OutputStream caos = new ConsoleAnnotationOutputStream<Void>(sw, ConsoleAnnotators.createAnnotator(null), null, StandardCharsets.UTF_8);
             StreamTaskListener listener = new StreamTaskListener(caos)) {
            s.getChannel().call(new AnnotateCallable(text, listener, new AnsiColorConsoleLogFilter(colorMap)));
        } catch (IOException x) {
            throw x;
        } catch (Exception x) {
            throw new IOException(x);
        }
        return sw.toString();
    }

    private static final class AnnotateCallable extends MasterToSlaveCallable<Void, Exception> {

        private final String text;
        private final TaskListener listener;
        private final AnsiColorConsoleLogFilter filter;

        AnnotateCallable(String text, TaskListener listener, AnsiColorConsoleLogFilter filter) {
            this.text = text;
            this.listener = listener;
            this.filter = filter;
        }

        @Override
        public Void call() throws Exception {
            try (OutputStream decorated = filter.decorateLogger((AbstractBuild) null, listener.getLogger());
                 PrintStream ps = new PrintStream(decorated)) {
                ps.print(text);
            }
            return null;
        }

    }

    @Override
    public void testEmbeddedConsoleNote() throws IOException {
        throw new AssumptionViolatedException("seems irrelevant");
    }

    @Override
    public void testNegative() throws IOException {
        throw new AssumptionViolatedException("TODO not implemented");
    }

    @Override
    public void testGreenOnWhite() throws IOException {
        throw new AssumptionViolatedException("TODO missing background-color");
    }

    @Override
    public void testGreenOnWhiteCSS() throws IOException {
        throw new AssumptionViolatedException("TODO missing background-color");
    }

    @Override
    public void testGreenOnWhiteXTerm() throws IOException {
        throw new AssumptionViolatedException("TODO missing background-color");
    }

    @Override
    public void testResetForegroundColor() throws IOException {
        throw new AssumptionViolatedException("TODO missing bold");
    }

    @Override
    public void testForegroundColorHighIntensity() throws IOException {
        throw new AssumptionViolatedException("TODO not implemented");
    }

    @Override
    public void testForegroundColor256() throws IOException {
        throw new AssumptionViolatedException("other than the standard colors, which could be split into a separate test, seems unimplementable");
    }

    @Override
    public void testForegroundColorRgb() throws IOException {
        throw new AssumptionViolatedException("probably unimplementable");
    }

    @Override
    public void testResetBackgroundColor() throws IOException {
        throw new AssumptionViolatedException("TODO not implemented");
    }

    @Override
    public void testBackgroundColorHighIntensity() throws IOException {
        throw new AssumptionViolatedException("TODO not implemented");
    }

    @Override
    public void testBackgroundColor256() throws IOException {
        throw new AssumptionViolatedException("other than the standard colors, which could be split into a separate test, seems unimplementable");
    }

    @Override
    public void testBackgroundColorRgb() throws IOException {
        throw new AssumptionViolatedException("probably unimplementable");
    }

    @Override
    public void testDefaultColors() throws IOException {
        throw new AssumptionViolatedException("TODO missing background-color");
    }

    @Override
    public void testConsoleNote() throws IOException {
        throw new AssumptionViolatedException("seems irrelevant");
    }

    @Override
    public void testOverlapping() throws IOException {
        throw new AssumptionViolatedException("TODO missing some things");
    }

}
