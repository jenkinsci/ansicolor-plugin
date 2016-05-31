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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import hudson.console.ConsoleNote;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;

public class AnsiHtmlOutputStreamTest {

    @Test
    public void testEmpty() throws IOException {
        assertThatAnnotateIs("", "");
    }

    @Test
	public void testNoMarkup() throws IOException {
		assertThatAnnotateIs("line", "line");
	}

    @Test
	public void testClearBlank() throws IOException {
		assertThatAnnotateIs("\033[0m", "");
	}

	@Test
	public void testClear() throws IOException {
        assertThatAnnotateIs("\033[0m\033[K", "");
	}

    @Test
    public void testConceal() throws IOException {
        assertThatAnnotateIs(
            "there is concealed text here, \033[8mCONCEAL\033[0m, and it should vanish.",
            "there is concealed text here, , and it should vanish."
        );
    }

    @Test
    public void testEmbeddedConsoleNote() throws IOException {
        assertThatAnnotateIs(
            "there is a ConsoleNote here, \033[8mha:CONSOLENOTE\033[0m, and it should be left untouched.",
            "there is a ConsoleNote here, \033[8mha:CONSOLENOTE\033[0m, and it should be left untouched."
        );
    }

    @Test
    public void testConcealedConsoleNote() throws IOException {
        assertThatAnnotateIs(
            "there is a concealed note here, \033[8m\033[8mha:CONCEALEDCONSOLENOTE\033[0m\033[0m, " +
                "and it should vanish.",
            "there is a concealed note here, , and it should vanish."
        );
    }

    @Test
    public void testConcealedConsoleNoteDoesNotUnconceal() throws IOException {
        assertThatAnnotateIs(
            "there is a concealed note here, \033[8m\033[8mha:CONCEALEDCONSOLENOTE\033[0m, " +
                "and it may not affect ongoing concealing.",
            "there is a concealed note here, "
        );
    }

    @Test
    public void testBold() throws IOException {
        assertThatAnnotateIs("\033[1mhello world", "<b>hello world</b>");
    }

    @Test
    public void testUnderline() throws IOException {
        assertThatAnnotateIs("\033[4mhello world", "<u>hello world</u>");
    }

    @Test
    public void testUnderlineDouble() throws IOException {
        assertThatAnnotateIs("\033[21mhello world", "<span style=\"border-bottom: 3px double;\">hello world</span>");
    }

    @Test
    public void testGreen() throws IOException {
        assertThatAnnotateIs("\033[32mhello world", "<span style=\"color: #00CD00;\">hello world</span>");
    }

    @Test
    public void testGreenXTerm() throws IOException {
        assertThat(annotate("\033[32mhello world", AnsiColorMap.XTerm),
            is("<span style=\"color: "
                + AnsiColorMap.XTerm.getGreen() + ";\">hello world</span>"));
    }

    @Test
    public void testGreenCSS() throws IOException {
        assertThat(annotate("\033[32mhello world", AnsiColorMap.CSS),
            is("<span style=\"color: green;\">hello world</span>"));
    }


    @Test
    public void testGreenOnWhite() throws IOException {
        assertThat(
            annotate("\033[47;32mhello world"),
            is("<span style=\"background-color: #E5E5E5;\"><span style=\"color: #00CD00;\">hello world</span></span>"));
    }

    @Test
    public void testGreenOnWhiteCSS() throws IOException {
        assertThat(
            annotate("\033[47;32mhello world", AnsiColorMap.CSS),
            is("<span style=\"background-color: white;\"><span style=\"color: green;\">hello world</span></span>"));
    }

    @Test
    public void testGreenOnWhiteXTerm() throws IOException {
        assertThat(
            annotate("\033[47;32mhello world", AnsiColorMap.XTerm),
            is("<span style=\"background-color: "
                + AnsiColorMap.XTerm.getWhite() + ";\"><span style=\"color: "
                + AnsiColorMap.XTerm.getGreen() + ";\">hello world</span></span>"));
    }

    @Test
    public void testResetForegroundColor() throws IOException {
        assertThatAnnotateIs("\033[32mtic\033[1mtac\033[39mtoe",
                "<span style=\"color: #00CD00;\">tic<b>tac</b></span><b>toe</b>");
    }

    @Test
    public void testSetForegroundColorToHighIntensity() throws IOException {
        assertThatAnnotateIs("\033[91mLight red\033[0m",
                "<span style=\"color: #FF0000;\">Light red</span>");
        assertThatAnnotateIs("\033[92mLight green\033[0m",
                "<span style=\"color: #00FF00;\">Light green</span>");
        assertThatAnnotateIs("\033[93mLight yellow\033[0m",
                "<span style=\"color: #FFFF00;\">Light yellow</span>");
        assertThatAnnotateIs("\033[94mLight blue\033[0m",
                "<span style=\"color: #4682B4;\">Light blue</span>");
        assertThatAnnotateIs("\033[95mLight magenta\033[0m",
                "<span style=\"color: #FF00FF;\">Light magenta</span>");
        assertThatAnnotateIs("\033[96mLight cyan\033[0m",
                "<span style=\"color: #00FFFF;\">Light cyan</span>");
        assertThatAnnotateIs("\033[97mWhite\033[0m",
                "<span style=\"color: #FFFFFF;\">White</span>");
    }

    @Test
    public void testResetBackgroundColor() throws IOException {
        assertThatAnnotateIs("\033[42mtic\033[1mtac\033[49mtoe",
                "<span style=\"background-color: #00CD00;\">tic<b>tac</b></span><b>toe</b>");
    }

    @Test
    public void testDefaultColors() throws IOException {
        assertThat(
                annotate("\033[32mtic\033[1mtac\033[39mtoe", AnsiColorMap.VGA),
                is("<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                        "<span style=\"color: #00AA00;\">tic<b>tac</b></span><b>toe</b>" +
                        "</div>"));
    }


    @Test
    public void testConsoleNote() throws IOException {
        assertThat(
            annotate(ConsoleNote.PREAMBLE_STR + "hello world" + ConsoleNote.POSTAMBLE_STR),
            is(ConsoleNote.PREAMBLE_STR + "hello world" + ConsoleNote.POSTAMBLE_STR));
    }

    @Test
    public void testResetOnOpen() throws IOException {
        assertThat(
            annotate("\033[0;31;49mred\033[0m"),
            is("" +
                "<span style=\"color: #CD0000;\">red" +
                "</span>")
        );
    }

    @Test
    public void testUnicode() throws IOException {
        assertThatAnnotateIs("\033[32mmünchen", "<span style=\"color: #00CD00;\">münchen</span>");
    }

    @Test
    public void testJapanese() throws IOException {
        assertThatAnnotateIs("\033[32mこんにちは", "<span style=\"color: #00CD00;\">こんにちは</span>");
    }

    @Test
    public void testOverlapping() throws IOException {
        assertThatAnnotateIs("plain\033[32mgreen\033[1mboldgreen\033[4mulboldgreen\033[31mulboldred" +
            "\033[22mulred\033[24mred",
            "plain" +
                "<span style=\"color: #00CD00;\">" +                    // +green
                "green" +
                "<b>" +                                                 // +bold (now green,bold)
                "boldgreen" +
                "<u>" +                                                 // +underline (now green,bold,ul)
                "ulboldgreen" +
                "</u></b></span><b><u>" +                               // -green (now bold,ul)
                "<span style=\"color: #CD0000;\">" +                    // +red (now bold,ul,red)
                "ulboldred" +
                "</span></u></b><u><span style=\"color: #CD0000;\">" +  // -bold (now ul,red)
                "ulred" +
                "</span></u><span style=\"color: #CD0000;\">" +         // -underline (now red)
                "red" +
                "</span>"                                               // close all.
        );
    }

    private void assertThatAnnotateIs(String ansi, String html) throws IOException {
        assertThat(annotate(ansi), is(html));
    }

    private String annotate(String text, AnsiColorMap colorMap) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnsiHtmlOutputStream ansi = new AnsiHtmlOutputStream(bos, colorMap, new AnsiAttributeElement.Emitter() {
            public void emitHtml(String html) {
                try {
                    bos.write(html.getBytes("UTF-8"));
                } catch (IOException e) {
                    throw new RuntimeException("error emitting HTML", e);
                }
            }
        });
        ansi.write(text.getBytes("UTF-8"));
        ansi.close();
        return bos.toString("UTF-8");
    }

    private String annotate(String text) throws IOException {
        return annotate(text, AnsiColorMap.Default);
    }

}
