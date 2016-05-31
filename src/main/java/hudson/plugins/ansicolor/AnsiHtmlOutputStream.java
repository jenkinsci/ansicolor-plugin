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

    @Override
    protected void processSetAttribute(int attribute) throws IOException {
        switch (attribute) {
        case ATTRIBUTE_CONCEAL_ON:
            startConcealing();
            break;
        case ATTRIBUTE_INTENSITY_BOLD:
            closeTagOfType(AnsiAttrType.BOLD);
            openTag(new AnsiAttributeElement(AnsiAttrType.BOLD, "b", ""));
            break;
        case ATTRIBUTE_INTENSITY_NORMAL:
            closeTagOfType(AnsiAttrType.BOLD);
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
        }
    }

    @Override
    protected void processAttributeRest() throws IOException {
        stopConcealing();
        closeOpenTags(AnsiAttrType.DEFAULT);
    }

    @Override
    protected void processSetForegroundColor(int color) throws IOException {
        closeTagOfType(AnsiAttrType.FG); // Strictly not needed, but makes for cleaner HTML.
        openTag(new AnsiAttributeElement(AnsiAttrType.FG, "span", "style=\"color: " + colorMap.getNormal(color) + ";\""));
    }

    /**
     * Function for setting the foreground color to non standard ANSI colors (90 - 97).
     */
    @Override
    protected void processSetForegroundColor(int color, boolean bright) throws IOException {
         closeTagOfType(AnsiAttrType.BG); // Strictly not needed, but makes for cleaner HTML.
         openTag(new AnsiAttributeElement(AnsiAttrType.BG, "span", "style=\"color: " + colorMap.getBright(color) + ";\"")); 	    
    }

    @Override
    protected void processSetBackgroundColor(int color) throws IOException {
        closeTagOfType(AnsiAttrType.BG); // Strictly not needed, but makes for cleaner HTML.
        openTag(new AnsiAttributeElement(AnsiAttrType.BG, "span", "style=\"background-color: " + colorMap.getNormal(color) + ";\""));
    }

    @Override
    protected void processDefaultTextColor() throws IOException {
        closeTagOfType(AnsiAttrType.FG);
    }

    @Override
    protected void processDefaultBackgroundColor() throws IOException {
        closeTagOfType(AnsiAttrType.BG);
    }
}
