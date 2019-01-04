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

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleAnnotatorFactory;
import hudson.model.Queue;
import hudson.model.Run;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

/**
 * Applies ANSI coloration to log files where requested.
 */
final class ColorConsoleAnnotator extends ConsoleAnnotator<Object> {

    private static final Logger LOGGER = Logger.getLogger(ColorConsoleAnnotator.class.getName());

    private static final long serialVersionUID = 1;

    private final String colorMapName;

    ColorConsoleAnnotator(String colorMapName) {
        this.colorMapName = colorMapName;
        LOGGER.fine("creating annotator with colorMapName=" + colorMapName);
    }

    @Override
    public ConsoleAnnotator<Object> annotate(Object context, MarkupText text) {
        String s = text.getText();
        if (s.indexOf('\u001B') != -1) {
            AnsiColorMap colorMap = Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).getColorMap(colorMapName);
            CountingOutputStream outgoing = new CountingOutputStream(new NullOutputStream());
            class EmitterImpl implements AnsiAttributeElement.Emitter {
                CountingOutputStream incoming;
                int adjustment;
                int lastPoint = -1; // multiple HTML tags may be emitted for one control sequence
                @Override
                public void emitHtml(String html) {
                    LOGGER.log(Level.FINEST, "emitting {0} @{1}/{2}", new Object[] { html, incoming.getCount(), s.length() });
                    text.addMarkup(incoming.getCount(), html);
                    if (incoming.getCount() != lastPoint) {
                        lastPoint = incoming.getCount();
                        int hide = incoming.getCount() - outgoing.getCount() - adjustment;
                        LOGGER.log(Level.FINEST, "hiding {0} @{1}", new Object[] { hide, outgoing.getCount() + adjustment });
                        text.addMarkup(outgoing.getCount() + adjustment, outgoing.getCount() + adjustment + hide, "<!--", "-->");
                        adjustment += hide;
                    }
                }
            }
            EmitterImpl emitter = new EmitterImpl();
            CountingOutputStream incoming = new CountingOutputStream(new AnsiHtmlOutputStream(outgoing, colorMap, emitter));
            emitter.incoming = incoming;
            try {
                /*
                 * We only use AnsiHtmlOutputStream for its calls to Emitter.emitHtml when it encounters ANSI escape
                 * sequences; the output of the stream will be discarded. To know where to insert HTML in the MarkupText,
                 * we track the number of bytes we have written, and use that as a char (UTF-16 code unit) offset into
                 * the original String. Since all ANSI escape sequences only use ASCII characters, and ASCII characters
                 * in UTF-16BE are all represented using a single code unit whose high byte is 0, and whose low byte is
                 * the same as it would be in an 8-bit ASCII encoding, we write all ASCII chars to the stream as the low
                 * byte of the code unit, and convert any other character into '?' as a placeholder so the number of
                 * bytes written matches the char offset into the String.
                 */
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    // The highest ASCII character is 0x7F (DEL). High and low surrogate pairs in UTF-16BE will always
                    // be at least 0xD800 and will be converted to '?'.
                    if (c >= 0x80) {
                        c = '?';
                    }
                    incoming.write(c);
                }
            } catch (IOException x) {
                LOGGER.log(Level.WARNING, null, x);
            }
            LOGGER.finer(() -> "\"" + StringEscapeUtils.escapeJava(s) + "\" → \"" + StringEscapeUtils.escapeJava(text.toString(true)) + "\"");
        }
        return this;
    }

    @Extension
    public static final class Factory extends ConsoleAnnotatorFactory<Object> {

        @Override
        public ConsoleAnnotator<Object> newInstance(Object context) {
            LOGGER.fine("context=" + context);
            if (context instanceof Run) {
                ColorizedAction action = ((Run) context).getAction(ColorizedAction.class);
                if (action != null) {
                    return new ColorConsoleAnnotator(action.colorMapName);
                }
            } else if (Jenkins.get().getPlugin("workflow-api") != null && context instanceof FlowNode) {
                FlowNode node = (FlowNode) context;
                FlowExecutionOwner owner = node.getExecution().getOwner();
                if (owner != null) {
                    Queue.Executable exec = null;
                    try {
                        exec = owner.getExecutable();
                    } catch (IOException x) {
                        LOGGER.log(Level.WARNING, null, x);
                    }
                    if (exec instanceof Run) {
                        ColorizedAction action = ((Run) exec).getAction(ColorizedAction.class);
                        if (action != null) {
                            return new ColorConsoleAnnotator(action.colorMapName);
                        }
                    }
                }
            }
            return null;
        }

    }

}
