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
        LOGGER.log(Level.FINE, "creating annotator with colorMapName={0} charset={1} openTags={2}", new Object[] { colorMapName, charset, openTags });
    }

    ColorConsoleAnnotator(String colorMapName, String charset) {
        this(colorMapName, charset, Collections.emptyList());
    }

    @Override
    public ConsoleAnnotator<Object> annotate(Object context, MarkupText text) {
        String s = text.getText();
        List<AnsiAttributeElement> nextOpenTags = openTags;
        // TODO: As a performance improvement, we could create a branch where `s.indexOf('\u001B') == -1` but other
        // conditions are true that surrounds the text in the appropriate tags without going through AnsiHtmlOutputStream.
        AnsiColorMap colorMap = Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).getColorMap(colorMapName);
        if (s.indexOf('\u001B') != -1 || !openTags.isEmpty() || colorMap.getDefaultBackground() != null || colorMap.getDefaultForeground() != null) {
            CountingOutputStream outgoing = new CountingOutputStream(new NullOutputStream());
            class EmitterImpl implements AnsiAttributeElement.Emitter {
                CountingOutputStream incoming;
                int multiByteCharAdjustment;
                int hiddenCharAdjustment;
                int lastPoint = -1; // multiple HTML tags may be emitted for one control sequence
                @Override
                public void emitHtml(String html) {
                    int inCount = incoming.getCount() - multiByteCharAdjustment;
                    int outCount = outgoing.getCount() - multiByteCharAdjustment + hiddenCharAdjustment;
                    // All ANSI escapes sequences contain at least 2 bytes on modern platforms, so any HTML emitted
                    // directly after the first character is received is due to the initialization process of the stream and
                    // belongs at position 0 (i.e. default background/foreground colors).
                    if (inCount == 1) {
                        inCount = 0;
                    }
                    LOGGER.log(Level.FINEST, "emitting {0} @{1}/{2}", new Object[] { html, inCount, text.getText().length() });
                    text.addMarkup(inCount, html);
                    if (inCount != lastPoint) {
                        lastPoint = inCount;
                        int hide = inCount - outCount;
                        // If openTags is not empty, but there are no escape sequences directly on this line, or if we
                        // are emitting closing tags when closing the stream, there is nothing to hide.
                        if (hide != 0) {
                            LOGGER.log(Level.FINEST, "hiding {0} @{1}", new Object[] { hide, outCount });
                            text.addMarkup(outCount, outCount + hide, "<!--", "-->");
                            hiddenCharAdjustment += hide;
                        }
                    }
                }
                public void emitHtmlDirect(String html) {
                    text.addMarkup(incoming.getCount(), html);
                }
            }
            EmitterImpl emitter = new EmitterImpl();
            // We need to reopen tags that were still open at the end of the previous line so the stream's state is
            // correct in case those tags are closed in the middle of this line.
            try (AnsiHtmlOutputStream ansiOs = new AnsiHtmlOutputStream(outgoing, colorMap, emitter, openTags);
                    CountingOutputStream incoming = new CountingOutputStream(ansiOs)) {
                emitter.incoming = incoming;
                // We need to write one UTF-16 code unit at a time so that byte offsets match Java character offsets when inserting HTML.
                for (int i = 0; i < s.length(); i++) {
                    byte[] chars = String.valueOf(s.charAt(i)).getBytes(charset);
                    emitter.multiByteCharAdjustment += chars.length - 1;
                    incoming.write(chars);
                }
                nextOpenTags = ansiOs.getOpenTags();
                if (colorMap.getDefaultBackground() != null || colorMap.getDefaultForeground() != null) {
                    // The default color scheme will be opened automatically at the beginning of the stream on the next
                    // line, so we don't want to duplicate it.
                    nextOpenTags.remove(0);
                }
                // Tags open at the end of the line are closed when the stream is closed by the try-with-resources block.
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
