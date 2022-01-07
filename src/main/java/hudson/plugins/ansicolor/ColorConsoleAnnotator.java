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
import hudson.plugins.ansicolor.action.ColorizedAction;
import hudson.plugins.ansicolor.action.LineIdentifier;
import jenkins.model.Jenkins;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Applies ANSI coloration to log files where requested.
 */
final class ColorConsoleAnnotator extends ConsoleAnnotator<Object> {

    private static final Logger LOGGER = Logger.getLogger(ColorConsoleAnnotator.class.getName());

    private static final long serialVersionUID = 1;

    private static final Factory FACTORY = new Factory();

    private final String defaultColorMapName;

    private final LineIdentifier lineIdentifier;

    @CheckForNull
    private String colorMapName;

    @NonNull
    private List<AnsiAttributeElement> openTags = Collections.emptyList();

    private long lineNo;

    private ColorConsoleAnnotator(String defaultColorMapName, LineIdentifier lineIdentifier, long startLineNo) {
        this.defaultColorMapName = defaultColorMapName;
        this.lineIdentifier = lineIdentifier;
        this.lineNo = startLineNo;
    }

    @Override
    public ConsoleAnnotator<Object> annotate(@NonNull Object context, @NonNull MarkupText text) {
        lineNo++;
        Run<?, ?> run = runOf(context);
        if (run == null) {
            return this;
        }
        final ColorizedAction colorizedAction = lineNo == 1
            ? ColorizedAction.parseAction(text.getText(), lineNo, run, lineIdentifier)
            : ColorizedAction.parseAction(text, run);
        switch (colorizedAction.getCommand()) {
            case START:
            case CURRENT:
                colorMapName = colorizedAction.getColorMapName();
                break;
            case STOP:
                return FACTORY.newInstance(context, lineNo);
            case IGNORE:
                return this;
            default:
                if (colorMapName == null) {
                    colorMapName = defaultColorMapName;
                }
                break;
        }
        if (colorMapName == null) {
            return this;
        }

        String s = text.getText();
        List<AnsiAttributeElement> nextOpenTags = openTags;
        AnsiColorMap colorMap = Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).getColorMap(colorMapName);
        if (s.indexOf('\u001B') != -1 || !openTags.isEmpty() || colorMap.getDefaultBackground() != null || colorMap.getDefaultForeground() != null) {
            CountingOutputStream outgoing = new CountingOutputStream(new NullOutputStream());
            class EmitterImpl implements AnsiAttributeElement.Emitter {
                CountingOutputStream incoming;
                int adjustment;
                int lastPoint = -1; // multiple HTML tags may be emitted for one control sequence

                @Override
                public void emitHtml(@NonNull String html) {
                    final int inCount = getIncomingCount();
                    LOGGER.log(Level.FINEST, "emitting {0} @{1}/{2}", new Object[]{html, inCount, s.length()});
                    text.addMarkup(inCount, html);
                    hideIfNeeded(inCount, "");
                }

                /**
                 * All ANSI escapes sequences contain at least 2 bytes on modern platforms, so any HTML emitted
                 * directly after the first character is received is due to the initialization process of the stream and
                 * belongs at position 0 (i.e. default background/foreground colors).
                 *
                 * @return Incoming chars count
                 */
                private int getIncomingCount() {
                    final int inCount = incoming.getCount();
                    return inCount == 1 ? 0 : inCount;
                }

                private void hideIfNeeded(int inCount, String msg) {
                    if (inCount != lastPoint) {
                        lastPoint = inCount;
                        final int outCount = outgoing.getCount() + adjustment;
                        final int hide = inCount - outCount;
                        // If openTags is not empty, but there are no escape sequences directly on this line, or if we
                        // are emitting closing tags when closing the stream, there is nothing to hide.
                        if (hide != 0) {
                            LOGGER.log(Level.FINEST, "hiding {0} @{1}{2}", new Object[]{hide, outCount, msg});
                            text.addMarkup(outCount, outCount + hide, "<!--", "-->");
                            adjustment += hide;
                        }
                    }
                }

                @Override
                public void emitInvisibleSequence() {
                    hideIfNeeded(getIncomingCount(), " (ANSI sequence with no corresponding HTML tags)");
                }
            }
            EmitterImpl emitter = new EmitterImpl();
            // We need to reopen tags that were still open at the end of the previous line so the stream's state is
            // correct in case those tags are closed in the middle of this line.
            try (
                AnsiHtmlOutputStream ansiOs = new AnsiHtmlOutputStream(outgoing, colorMap, emitter, openTags);
                CountingOutputStream incoming = new CountingOutputStream(ansiOs)
            ) {
                emitter.incoming = incoming;
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
                nextOpenTags = ansiOs.getOpenTags();
                if (colorMap.getDefaultBackground() != null || colorMap.getDefaultForeground() != null) {
                    // The default color scheme will be opened automatically at the beginning of the stream on the next
                    // line, so we don't want to duplicate it.
                    // AnsiHtmlOutputStream#getOpenTags makes a copy so calling `remove` is safe.
                    nextOpenTags.remove(0);
                }
                // Tags open at the end of the line are closed when the stream is closed by the try-with-resources block.
            } catch (IOException x) {
                LOGGER.log(Level.WARNING, null, x);
            }
            LOGGER.finer(() -> "\"" + StringEscapeUtils.escapeJava(s) + "\" → \"" + StringEscapeUtils.escapeJava(text.toString(true)) + "\"");
        }
        openTags = nextOpenTags;
        return this;
    }

    @CheckForNull
    private static Run<?, ?> runOf(Object context) {
        LOGGER.log(Level.FINE, "context={0}", context);
        if (context instanceof Run) {
            return (Run<?, ?>) context;
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
                    return (Run<?, ?>) exec;
                }
            }
        }
        return null;
    }

    @Extension
    public static final class Factory extends ConsoleAnnotatorFactory<Object> {

        @Override
        public ConsoleAnnotator<Object> newInstance(Object context) {
            return newInstance(context, 0);
        }

        private ConsoleAnnotator<Object> newInstance(Object context, long startLineNo) {
            return new ColorConsoleAnnotator(Jenkins.get().getDescriptorByType(AnsiColorBuildWrapper.DescriptorImpl.class).getGlobalColorMapName(), new LineIdentifier(), startLineNo);
        }
    }
}
