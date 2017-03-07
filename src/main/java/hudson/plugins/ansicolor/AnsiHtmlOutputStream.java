/*
 * The MIT License
 *
 * Copyright (c) 2011 Daniel Doubrovkine
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

import static hudson.plugins.ansicolor.AnsiAttributeElement.AnsiAttrType;

import hudson.console.ConsoleNote;
import hudson.util.NullStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;
import org.fusesource.jansi.AnsiOutputStream;

/**
 * Filters an outputstream of ANSI escape sequences and emits appropriate HTML elements instead.
 *
 * Overlapping ANSI attribute combinations are handled by rewinding the HTML element stack.
 *
 * How the HTML is actually emitted depends on the specified {@link AnsiAttributeElement.Emitter}.
 * For Jenkins, the Emitter creates {@link ConsoleNote}s as part of the stream, but for
 * other software or testing the HTML may be emitted otherwise.
 *
 * The only thing that ties this class to Jenkins is the rather unfortunate {@link ConsoleNote} preamble/postamble
 * handling via state machine. Simply remove this if you plan to use this class somewhere else.
 */
public class AnsiHtmlOutputStream extends AnsiOutputStream {
    private final AnsiColorMap colorMap;
    private final AnsiAttributeElement.Emitter emitter;

    private static enum State {
        INIT, DATA, PREAMBLE, NOTE, POSTAMBLE
    }
    private State state = State.INIT;
    private int amblePos = 0;

    private String currentForegroundColor = null;
    private String currentBackgroundColor = null;
    private boolean swapColors = false;  // true if negative / inverse mode is active (esc[7m)

    // A Deque might be a better choice, but we are constrained by the Java 5 API.
    private ArrayList<AnsiAttributeElement> openTags = new ArrayList<AnsiAttributeElement>();

    private OutputStream logOutput;

    public AnsiHtmlOutputStream(final OutputStream os, final AnsiColorMap colorMap,
        final AnsiAttributeElement.Emitter emitter) {
        super(os);
        this.logOutput = os;
        this.colorMap = colorMap;
        this.emitter = emitter;
    }

    // Debug output for plugin developers. Puts the debug message into the html page
    @SuppressWarnings("unused")
    private void logdebug(String format, Object... args) throws IOException {
        String msg = String.format(format, args);
        emitter.emitHtml("<span style=\"border: 1px solid; color: #009000; background-color: #003000; font-size: 70%; font-weight: normal; font-style: normal\">");
        out.write(msg.getBytes());
        emitter.emitHtml("</span>");
    }

    /* Concealing has to happen *after* ANSI interpretation.
     * Instead of adding yet another filter, we switch the target stream to a null stream while concealing.
     *
     * Both the start- and stop-Method are idempotent and may be called regardless of current concealing state.
     */
    private void startConcealing() {
        this.out = new NullStream();
    }

    private void stopConcealing() {
        this.out = logOutput;
    }

    private void openTag(AnsiAttributeElement tag) throws IOException {
        openTags.add(tag);
        tag.emitOpen(emitter);
    }

    private void closeOpenTags(AnsiAttrType until) throws IOException {
        while (!openTags.isEmpty()) {
            int index = openTags.size() - 1;
            if (until != null && openTags.get(index).ansiAttrType == until)
                break;

            openTags.remove(index).emitClose(emitter);
        }
    }

    /* ANSI Attributes, unlike HTML elements, can overlap.
     * This method implements the unwinding of elements up until the element of the requested type.
     * That last element is just closed, while the others before it have to be reopened.
     *
     * Nothing happens when trying to close an element which has never been opened (i.e. "bold off" when there
     * was no "bold" before). */
    private void closeTagOfType(AnsiAttrType ansiAttrType) throws IOException {
        int sameTypePos;

        // Search for an element with matching type.
        for (sameTypePos = openTags.size(); sameTypePos > 0; sameTypePos--) {
            if (openTags.get(sameTypePos - 1).ansiAttrType == ansiAttrType) {
                break;
            }
        }

        if (sameTypePos == 0) {
            // No need to unwind anything if the attribute has not been touched yet.
            return;
        }

        Stack<AnsiAttributeElement> reopen = new Stack<AnsiAttributeElement>();

        // Unwind ...
        for (int unwindAt = openTags.size(); unwindAt > sameTypePos; unwindAt--) {
            AnsiAttributeElement tag = openTags.remove(unwindAt-1);
            tag.emitClose(emitter);
            reopen.push(tag);
        }

        // ... close matching element ...
        AnsiAttributeElement offendingTag = openTags.remove(sameTypePos-1);
        offendingTag.emitClose(emitter);

        // ... reopen.
        while (!reopen.isEmpty()) {
            AnsiAttributeElement tag = reopen.pop();
            tag.emitOpen(emitter);
            openTags.add(tag);
        }
    }

    @Override
    public void write(int data) throws IOException {
        // This little state machine only exists to handle embedded notes from other sources, whereas
        // the preamble is an ANSI escape sequence itself.

        if (state == State.INIT) {
            Integer defaultFg = colorMap.getDefaultForeground();
            Integer defaultBg = colorMap.getDefaultBackground();

            if (defaultFg != null || defaultBg != null) {
                openTag(new AnsiAttributeElement(AnsiAttrType.DEFAULT, "div", "style=\"" +
                        (defaultBg != null ? "background-color: " + colorMap.getNormal(defaultBg) + ";" : "") +
                        (defaultFg != null ? "color: " + colorMap.getNormal(defaultFg) + ";" : "") + "\""));
            }

            state = State.DATA;
        }

        switch (state) {
            case DATA:
                if (data == ConsoleNote.PREAMBLE[0]) {
                    state = State.PREAMBLE;
                    amblePos = 0;
                    collectAmbleCharacter(data, ConsoleNote.PREAMBLE);
                } else {
                    super.write(data);
                }
                break;
            case NOTE:
                if (data == ConsoleNote.POSTAMBLE[0]) {
                    state = State.POSTAMBLE;
                    amblePos = 0;
                    collectAmbleCharacter(data, ConsoleNote.POSTAMBLE);
                } else {
                    out.write(data); // Note that notes are directly written out, no ANSI interpretation.
                }
                break;
            case PREAMBLE:
                collectAmbleCharacter(data, ConsoleNote.PREAMBLE);
                break;
            case POSTAMBLE:
                collectAmbleCharacter(data, ConsoleNote.POSTAMBLE);
                break;
        }
    }

    private void collectAmbleCharacter(int data, byte[] amble) throws IOException {
        // The word "amble" is a cute generalization of preamble and postamble.

        if (data != amble[amblePos]) {
            // This was not an amble after all, so replay everything and try interpreting it as ANSI.
            for (int i = 0; i < amblePos; i++) {
                super.write(amble[i]);
            }

            // Replay the new character in the state machine (may or may not be part of another amble).
            state = state == State.POSTAMBLE ? State.NOTE : State.DATA;
            amblePos = 0;
            this.write(data);
        } else if (amblePos == amble.length - 1) {
            // Successfully read a whole preamble.

            out.write(amble);

            state = state == State.POSTAMBLE ? State.DATA : State.NOTE;
            amblePos = 0;
        } else {
            amblePos++;
        }
    }

    @Override
    public void close() throws IOException {
        stopConcealing();
        closeOpenTags(null);
        super.close();
    }

    private String getDefaultForegroundColor() {
        String color = null;
        Integer defaultFgIndex = colorMap.getDefaultForeground();
        if (defaultFgIndex != null) color = colorMap.getNormal(defaultFgIndex);
        if (color == null) {
            // with no default foreground set, we need to guess about (currently happened in xterm and css themes)
            // possible approaches are:
            // • hardcoded black "#000000"
            // • colorMap.getNormal(0)
            // • "currentColor" → see also: http://stackoverflow.com/a/42586457/2880699
            //   but note, that this approach only works, if all <span style="color: …"> are currently closed

            // Note that in my default szenario with, the default text color is #333333. Tested with:
            // • Jenkins 1.6.5.3, AnsiColor plugin 0.4.3, xterm scheme
            // • Firefox 51.0.1, Kubuntu 16.04
            // • Chromium 56.0.2924.76, Kubuntu 16.04
            // • Firefox 51.0.1, Windows XP

            // So I finally decide for the "currentColor" approach, because:
            // • It gives the best results for negative / inverse text (what is the major reason for implementing this function).
            // • It looks also the best alternative, if e.g. someone customizes Jenkins colors.
            // • Finally, the clause "only works, if all <span style="color: …"> are currently closed" is fulfilled for the negative / inverse case

            // color = "#000000";              // hardcoded black
            // color = colorMap.getNormal(0);  // hardcoded index 0
            color = "currentColor";            // see http://stackoverflow.com/a/42586457/2880699
        }
        return color;
    }

    private String getDefaultBackgroundColor() {
        String color = null;
        Integer defaultBgIndex = colorMap.getDefaultBackground();
        if (defaultBgIndex != null) color = colorMap.getNormal(defaultBgIndex);
        if (color == null) {
            // with no default foreground set, we need to guess about (currently happened in xterm and css themes)
            // possible approaches are:
            // • hardcoded white "#FFFFFF"
            // • colorMap.getNormal(7)
            // • colorMap.getBright(7)
            // I finally decide for colorMap.getBright(7).

            //color "#FFFFFF";                 // hardcoded white
            //color = colorMap.getNormal(7);   // hardcoded normal index 7
            color = colorMap.getBright(7);     // hardcoded bright index 7
        }
        return color;
    }

    // @in  color  Html color value like e.g. "#AABBCC" or null for default color
    private void setForegroundColor(String color) throws IOException {
        AnsiAttrType attrType = !swapColors ? AnsiAttrType.FG : AnsiAttrType.BG;
        String attrName = !swapColors ? "color" : "background-color";
        if (color == null && swapColors) color = getDefaultForegroundColor();
        boolean restorebg = false;
        if (swapColors && color.equals("currentColor")) {
            // need also to temporarily unwind textcolor, to having correct access to the "currentColor" value
            closeTagOfType(AnsiAttrType.FG);
            restorebg = true;
        }
        closeTagOfType(attrType);
        if (color != null)
            openTag(new AnsiAttributeElement(attrType, "span", "style=\"" + attrName + ": " + color + ";\""));
        if (restorebg) {
            String bg = currentBackgroundColor;
            if (bg == null) bg = getDefaultBackgroundColor();
            openTag(new AnsiAttributeElement(AnsiAttrType.FG, "span", "style=\"color: " + bg + ";\""));
        }
        currentForegroundColor = color;
    }

    // @in  color  Html color value like e.g. "#AABBCC" or null for default color
    public void setBackgroundColor(String color) throws IOException {
        AnsiAttrType attrType = !swapColors ? AnsiAttrType.BG : AnsiAttrType.FG;
        String attrName = !swapColors ? "background-color" : "color";
        if (color == null && swapColors) color = getDefaultBackgroundColor();
        closeTagOfType(attrType);
        if (color != null)
            openTag(new AnsiAttributeElement(attrType, "span", "style=\"" + attrName + ": " + color + ";\""));
        currentBackgroundColor = color;
    }

    // add attribute constants which are currently missing in jansi
    // see also https://en.wikipedia.org/wiki/ANSI_escape_code
    protected static final int ATTRIBUTE_STRIKEOUT       =  9;
    protected static final int ATTRIBUTE_ITALIC_OFF      = 23;
    protected static final int ATTRIBUTE_STRIKEOUT_OFF   = 29;
    protected static final int ATTRIBUTE_FRAMED          = 51;
    protected static final int ATTRIBUTE_ENCIRCLED       = 52;  // not implemented yet
    protected static final int ATTRIBUTE_OVERLINE        = 53;
    protected static final int ATTRIBUTE_FRAMED_OFF      = 54;  // framed and encircled off
    protected static final int ATTRIBUTE_OVERLINE_OFF    = 55;

    @Override
    protected void processSetAttribute(int attribute) throws IOException {
        // For some (unknown) reason, AnsiOutputStream.processEscapeCommand() sometimes(?) won't call our processSetFore/BackgroundColor().
        // see also: https://github.com/dblock/jenkins-ansicolor-plugin/issues/91
        if (attribute >= 90 && attribute <= 97) {
            processSetForegroundColor(attribute - 90, true);
        }
        else if (attribute >= 100 && attribute <= 107) {
            processSetBackgroundColor(attribute - 100, true);
        }
        else switch (attribute) {
        case ATTRIBUTE_CONCEAL_ON:
            startConcealing();
            break;
        case ATTRIBUTE_CONCEAL_OFF:
            stopConcealing();
            break;
        case ATTRIBUTE_INTENSITY_BOLD:
            closeTagOfType(AnsiAttrType.BOLD);
            openTag(new AnsiAttributeElement(AnsiAttrType.BOLD, "b", ""));
            break;
        case ATTRIBUTE_INTENSITY_NORMAL:
            closeTagOfType(AnsiAttrType.BOLD);
            break;
        case ATTRIBUTE_ITALIC:
            closeTagOfType(AnsiAttrType.ITALIC);
            openTag(new AnsiAttributeElement(AnsiAttrType.ITALIC, "i", ""));
            break;
        case ATTRIBUTE_ITALIC_OFF:
            closeTagOfType(AnsiAttrType.ITALIC);
            break;
        case ATTRIBUTE_UNDERLINE:
            closeTagOfType(AnsiAttrType.UNDERLINE);
            openTag(new AnsiAttributeElement(AnsiAttrType.UNDERLINE, "u", ""));
            break;
        case ATTRIBUTE_UNDERLINE_DOUBLE:
            // Double underlining is handled entirely different from single underlining, by using a CSS border
            // instead of a u-element, but it's still of the same attribute type and previously opened elements of
            // either type are closed accordingly.
            closeTagOfType(AnsiAttrType.UNDERLINE);
            openTag(new AnsiAttributeElement(AnsiAttrType.UNDERLINE, "span", "style=\"border-bottom: 3px double;\""));
            break;
        case ATTRIBUTE_UNDERLINE_OFF:
            closeTagOfType(AnsiAttrType.UNDERLINE);
            break;
        case ATTRIBUTE_NEGATIVE_ON:
        case ATTRIBUTE_NEGATIVE_Off:
            // swap foreground / background colors
            boolean swapNow = attribute == ATTRIBUTE_NEGATIVE_ON;
            if (swapNow == swapColors) break; // nothing to do
            swapColors = swapNow;
            String bg = currentBackgroundColor;
            String fg = currentForegroundColor;
            if (swapColors) {
                if (bg == null) bg = getDefaultBackgroundColor();
                if (fg == null) fg = getDefaultForegroundColor();
                String tmp = fg;
                fg = bg;
                bg = tmp;
            }
            closeTagOfType(AnsiAttrType.FG);
            closeTagOfType(AnsiAttrType.BG);
            if (fg != null && bg != null) {
                openTag(new AnsiAttributeElement(AnsiAttrType.BG, "span", "style=\"background-color: " + bg + ";\""));
                openTag(new AnsiAttributeElement(AnsiAttrType.FG, "span", "style=\"color: " + fg + ";\""));
                //closeTagOfType(AnsiAttrType.FGBG);
                //openTag(new AnsiAttributeElement(AnsiAttrType.FGBG, "span", "style=\"color: " + fg + "; background-color: " + bg + ";\""));
            } else {
                if (bg != null) openTag(new AnsiAttributeElement(AnsiAttrType.BG, "span", "style=\"background-color: " + bg + ";\""));
                if (fg != null) openTag(new AnsiAttributeElement(AnsiAttrType.FG, "span", "style=\"color: " + fg + ";\""));
            }
            break;
        case ATTRIBUTE_STRIKEOUT:
            // <strike> is deprecated in HTML 4 and obsoleted in HTML5 (but still worked in my firefox 51.0.1)
            // alternatives are <del> <s> (both tested and successfully rendered in firefox 51.0.1)
            // but I finally decide for "text-decoration: line-through"
            closeTagOfType(AnsiAttrType.STRIKEOUT);
            openTag(new AnsiAttributeElement(AnsiAttrType.STRIKEOUT, "span", "style=\"text-decoration: line-through;\""));
            // openTag(new AnsiAttributeElement(AnsiAttrType.STRIKEOUT, "s", "")); // alternate approach
            break;
        case ATTRIBUTE_STRIKEOUT_OFF:
            closeTagOfType(AnsiAttrType.STRIKEOUT);
            break;
        case ATTRIBUTE_FRAMED:
            closeTagOfType(AnsiAttrType.FRAMED);
            openTag(new AnsiAttributeElement(AnsiAttrType.FRAMED, "span", "style=\"border: 1px solid;\""));
            break;
        case ATTRIBUTE_FRAMED_OFF:
            closeTagOfType(AnsiAttrType.FRAMED);
            break;
        case ATTRIBUTE_OVERLINE:
            closeTagOfType(AnsiAttrType.OVERLINE);
            openTag(new AnsiAttributeElement(AnsiAttrType.OVERLINE, "span", "style=\"text-decoration: overline;\""));
            //openTag(new AnsiAttributeElement(AnsiAttrType.OVERLINE, "span", "style=\"border-top: 1px solid;\"")); // alternate approach
            break;
        case ATTRIBUTE_OVERLINE_OFF:
            closeTagOfType(AnsiAttrType.OVERLINE);
            break;
        }
    }

    @Override
    protected void processAttributeRest() throws IOException {
        currentForegroundColor = null;
        currentBackgroundColor = null;
        swapColors = false;
        stopConcealing();
        closeOpenTags(AnsiAttrType.DEFAULT);
    }

    @Override
    protected void processSetForegroundColor(int color) throws IOException {
        setForegroundColor(colorMap.getNormal(color));
    }

    // set foreground color to non standard ANSI colors (90 - 97)
    @Override
    protected void processSetForegroundColor(int color, boolean bright) throws IOException {
        setForegroundColor(colorMap.getBright(color));
    }

    @Override
    protected void processSetBackgroundColor(int color) throws IOException {
        setBackgroundColor(colorMap.getNormal(color));
    }

    // set background color to non standard ANSI colors (100 - 107)
    @Override
    protected void processSetBackgroundColor(int color, boolean bright) throws IOException {
        setBackgroundColor(colorMap.getBright(color));
    }

    @Override
    protected void processDefaultTextColor() throws IOException {
        setForegroundColor(null);
    }

    @Override
    protected void processDefaultBackgroundColor() throws IOException {
        setBackgroundColor(null);
    }
}
