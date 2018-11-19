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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
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
    private final String charset;
    private final @Nonnull List<AnsiAttributeElement> openTags;

    ColorConsoleAnnotator(String colorMapName, String charset, List<AnsiAttributeElement> openTags) {
        this.colorMapName = colorMapName;
        this.charset = charset;
        this.openTags = openTags;
        LOGGER.log(Level.FINE, "creating annotator with colorMapName={0} charset={1}, openTags={2}", new Object[] { colorMapName, charset, openTags });
    }

    ColorConsoleAnnotator(String colorMapName, String charset) {
        this(colorMapName, charset, Collections.emptyList());
    }

    @Override
    public ConsoleAnnotator<Object> annotate(Object context, MarkupText text) {
        String s = text.getText();
        List<AnsiAttributeElement> nextOpenTags = openTags;
        if (s.indexOf('\u001B') != -1 || !openTags.isEmpty()) {
            AnsiColorMap colorMap = Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).getColorMap(colorMapName);
            CountingOutputStream outgoing = new CountingOutputStream(new NullOutputStream());
            class EmitterImpl implements AnsiAttributeElement.Emitter {
                CountingOutputStream incoming;
                int adjustment;
                int lastPoint = -1; // multiple HTML tags may be emitted for one control sequence
                @Override
                public void emitHtml(String html) {
                    LOGGER.log(Level.FINEST, "emitting {0} @{1}", new Object[] { html, incoming.getCount() });
                    text.addMarkup(incoming.getCount(), html);
                    if (incoming.getCount() != lastPoint) {
                        lastPoint = incoming.getCount();
                        int hide = incoming.getCount() - outgoing.getCount() - adjustment;
                        // If openTags is not empty, but there are no escape sequences directly on this line, or if we
                        // are just closing tags at the end of the line, there is nothing to hide.
                        if (hide != 0) {
                            LOGGER.log(Level.FINEST, "hiding {0} @{1}", new Object[] { hide, outgoing.getCount() + adjustment });
                            text.addMarkup(outgoing.getCount() + adjustment, outgoing.getCount() + adjustment + hide, "<span style=\"display: none\">", "</span>");
                            adjustment += hide;
                        }
                    }
                }
                public void emitHtmlDirect(String html) {
                    text.addMarkup(incoming.getCount(), html);
                }
            }
            EmitterImpl emitter = new EmitterImpl();
            AnsiHtmlOutputStream ansiOs = new AnsiHtmlOutputStream(outgoing, colorMap, emitter);
            CountingOutputStream incoming = new CountingOutputStream(ansiOs);
            emitter.incoming = incoming;
            try {
                for (AnsiAttributeElement element : openTags) {
                    // We need to reopen tags that were still open at the end of the previous line in the
                    // so the stream's state is correct in case those tags are closed mid-line.
                    ansiOs.openTag(element);
                }
                byte[] data = s.getBytes(charset);
                for (int i = 0; i < data.length; i++) {
                    // Do not use write(byte[]) as offsets in incoming would not be accurate.
                    incoming.write(data[i]);
                }
                nextOpenTags = ansiOs.getOpenTags();
                // Close any tags that are still open at the end of the line.
                ansiOs.closeOpenTags(null);
            } catch (IOException x) {
                LOGGER.log(Level.WARNING, null, x);
            }
            LOGGER.finer(() -> "\"" + StringEscapeUtils.escapeJava(s) + "\" → \"" + StringEscapeUtils.escapeJava(text.toString(true)) + "\"");
        }
        return openTags == nextOpenTags
                ? this
                : new ColorConsoleAnnotator(colorMapName, charset, nextOpenTags);
    }

    private Object readResolve() {
        // Compatibility for instances serialized before the openTags field was added.
        if (openTags == null) {
            return new ColorConsoleAnnotator(colorMapName, charset);
        } else {
            return this;
        }
    }

    @Extension
    public static final class Factory extends ConsoleAnnotatorFactory<Object> {

        @Override
        public ConsoleAnnotator<Object> newInstance(Object context) {
            LOGGER.log(Level.FINE, "context={0}", context);
            if (context instanceof Run) {
                ColorizedAction action = ((Run) context).getAction(ColorizedAction.class);
                if (action != null) {
                    return new ColorConsoleAnnotator(action.colorMapName, ((Run) context).getCharset().name());
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
                            return new ColorConsoleAnnotator(action.colorMapName, /* JEP-206 */ "UTF-8");
                        }
                    }
                }
            }
            return null;
        }

    }

}
