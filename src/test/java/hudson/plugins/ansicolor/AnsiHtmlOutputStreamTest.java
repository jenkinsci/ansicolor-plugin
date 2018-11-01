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
    public void testItalic() throws IOException {
        assertThatAnnotateIs("\033[3mhello world\033[23mnormal", "<i>hello world</i>normal");
    }

    @Test
    public void testNegative() throws IOException {
       // simple tests
        assertThatAnnotateIs(
                "\033[7mon\033[moff",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\">on</span></span>off");

        assertThatAnnotateIs(
                "\033[7mon\033[27moff",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\">on</span></span>off");

        assertThatAnnotateIs(
                "\033[33;7mon\033[27moff",
                "<span style=\"color: #CDCD00;\"></span>" +                                                            // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00; color: #FFFFFF;\">on</span>" +
                "<span style=\"color: #CDCD00;\">off</span>");

        assertThatAnnotateIs(
                "\033[7;33mon\033[27moff",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\"></span></span>" +     // unnecessary <span> tag, could be removed …
                "<span style=\"color: #FFFFFF;\"><span style=\"background-color: #CDCD00;\">on</span></span>" +        // could be optimized to be a single <span> tag
                "<span style=\"color: #CDCD00;\">off</span>");

        assertThatAnnotateIs(
                "\033[41;7mon\033[27moff",
                "<span style=\"background-color: #CD0000;\"></span>" +                                                 // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: currentColor;\"><span style=\"color: #CD0000;\">on</span></span>" +
                "<span style=\"background-color: #CD0000;\">off</span>");

        assertThatAnnotateIs(
                "\033[7;41mon\033[27moff",
                "<span style=\"background-color: currentColor;\">" +
                  "<span style=\"color: #FFFFFF;\"></span>" +                                                          // unnecessary <span> tag, could be removed …
                  "<span style=\"color: #CD0000;\">on</span>" +
                "</span>" +
                "<span style=\"background-color: #CD0000;\">off</span>");

        assertThatAnnotateIs(
                "\033[33;41;7mon\033[27moff",
                "<span style=\"color: #CDCD00;\"><span style=\"background-color: #CD0000;\"></span></span>" +          // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00; color: #CD0000;\">on</span>" +
                "<span style=\"background-color: #CD0000; color: #CDCD00;\">off</span>");

        assertThatAnnotateIs(
                "\033[7;33;41mon\033[27moff",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\"></span></span>" +     // unnecessary <span> tag, could be removed …
                "<span style=\"color: #FFFFFF;\"><span style=\"background-color: #CDCD00;\"></span></span>" +          // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00;\"><span style=\"color: #CD0000;\">on</span></span>" +        // could be optimized to be a single <span> tag
                "<span style=\"background-color: #CD0000; color: #CDCD00;\">off</span>");


        // reset foreground / background to default while [7m is active
        assertThatAnnotateIs(
                "\033[33;7mon\033[39mdefault",
                "<span style=\"color: #CDCD00;\"></span>" +                                                            // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00; color: #FFFFFF;\">on</span>" +
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\">default</span></span>");

        assertThatAnnotateIs(
                "\033[7;33mon\033[39mdefault",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\"></span></span>" +     // unnecessary <span> tag, could be removed …
                "<span style=\"color: #FFFFFF;\"><span style=\"background-color: #CDCD00;\">on</span></span>" +        // could be optimized to be a single <span> tag
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\">default</span></span>");

        assertThatAnnotateIs(
                "\033[41;7mon\033[49mdefault",
                "<span style=\"background-color: #CD0000;\"></span>" +                                                 // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: currentColor;\">" +
                  "<span style=\"color: #CD0000;\">on</span>" +
                  "<span style=\"color: #FFFFFF;\">default</span>" +
                "</span>");

        assertThatAnnotateIs(
                "\033[7;41mon\033[49mdefault",
                "<span style=\"background-color: currentColor;\">" +
                  "<span style=\"color: #FFFFFF;\"></span>" +                                                          // unnecessary <span> tag, could be removed …
                  "<span style=\"color: #CD0000;\">on</span>" +
                  "<span style=\"color: #FFFFFF;\">default</span>" +
                "</span>");

        assertThatAnnotateIs(
                "\033[33;41;7mon\033[39mdefault",
                "<span style=\"color: #CDCD00;\"><span style=\"background-color: #CD0000;\"></span></span>" +          // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00; color: #CD0000;\">on</span>" +
                "<span style=\"background-color: currentColor;\"><span style=\"color: #CD0000;\">default</span></span>");

        assertThatAnnotateIs(
                "\033[7;33;41mon\033[39mdefault",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\"></span></span>" +     // unnecessary <span> tag, could be removed …
                "<span style=\"color: #FFFFFF;\"><span style=\"background-color: #CDCD00;\"></span></span>" +          // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00;\"><span style=\"color: #CD0000;\">on</span></span>" +
                "<span style=\"background-color: currentColor;\"><span style=\"color: #CD0000;\">default</span></span>");

        assertThatAnnotateIs(
                "\033[33;41;7mon\033[49mdefault",
                "<span style=\"color: #CDCD00;\"><span style=\"background-color: #CD0000;\"></span></span>" +          // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00; color: #CD0000;\">" +
                  "on" +
                  "<span style=\"color: #FFFFFF;\">default</span>" +
                "</span>");

        assertThatAnnotateIs(
                "\033[7;33;41mon\033[49mdefault",
                "<span style=\"background-color: currentColor;\"><span style=\"color: #FFFFFF;\"></span></span>" +     // unnecessary <span> tag, could be removed …
                "<span style=\"color: #FFFFFF;\"><span style=\"background-color: #CDCD00;\"></span></span>" +          // unnecessary <span> tag, could be removed …
                "<span style=\"background-color: #CDCD00;\">" +
                  "<span style=\"color: #CD0000;\">on</span>" +
                  "<span style=\"color: #FFFFFF;\">default</span>" +
                "</span>");


        // simple tests with dark theme, as there has been default foreground / background colors defined (in contrast to xterm scheme)
        assertThatAnnotateIs(AnsiColorMap.VGA,
                "\033[7mon\033[moff",
                "<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                  "<span style=\"background-color: #AAAAAA; color: #000000;\">on</span>off" +
                "</div>");

        assertThatAnnotateIs(AnsiColorMap.VGA,
                "\033[7mon\033[27moff",
                "<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                  "<span style=\"background-color: #AAAAAA; color: #000000;\">on</span>off" +
                "</div>");

        assertThatAnnotateIs(AnsiColorMap.VGA,
                "\033[33;7mon\033[27moff",
                "<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                  "<span style=\"color: #AA5500;\"></span>" +                                                          // unnecessary <span> tag, could be removed …
                  "<span style=\"background-color: #AA5500; color: #000000;\">on</span>" +
                  "<span style=\"color: #AA5500;\">off</span>" +
                "</div>");

        assertThatAnnotateIs(AnsiColorMap.VGA,
                "\033[7;33mon\033[27moff",
                "<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                  "<span style=\"background-color: #AAAAAA; color: #000000;\">" +                                      // unnecessary <span> tag, could be removed / merged with the following <span>
                    "<span style=\"background-color: #AA5500;\">on</span>" +
                  "</span>" +
                  "<span style=\"color: #AA5500;\">off</span>" +
                "</div>");

        assertThatAnnotateIs(AnsiColorMap.VGA,
                "\033[41;7mon\033[27moff",
                "<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                  "<span style=\"background-color: #AA0000;\"></span>" +                                               // unnecessary <span> tag, could be removed …
                  "<span style=\"background-color: #AAAAAA; color: #AA0000;\">on</span>" +
                  "<span style=\"background-color: #AA0000;\">off</span>" +
                "</div>");

        assertThatAnnotateIs(AnsiColorMap.VGA,
                "\033[7;41mon\033[27moff",
                "<div style=\"background-color: #000000;color: #AAAAAA;\">" +
                  "<span style=\"background-color: #AAAAAA; color: #000000;\">" +                                      // unnecessary <span> tag, could be removed / merged with the following <span>
                    "<span style=\"color: #AA0000;\">on</span>" +
                  "</span>" +
                  "<span style=\"background-color: #AA0000;\">off</span>" +
                "</div>");


        // a bit more stress
        assertThatAnnotateIs(
                "\033[33;41m" + "yellow on red, " +
                "\033[7m"     + "now inverse, " +
                "\033[7m"     + "one more [7m should change nothing, " +
                "\033[27m"    + "turned back to non inverse",
                "<span style=\"color: #CDCD00;\"><span style=\"background-color: #CD0000;\">yellow on red, </span></span>" +     // could be optimized to be a single <span> tag
                "<span style=\"background-color: #CDCD00; color: #CD0000;\">now inverse, one more [7m should change nothing, </span>" +
                "<span style=\"background-color: #CD0000; color: #CDCD00;\">turned back to non inverse</span>");

        assertThatAnnotateIs(
                "\033[33;41m" + "yellow on red, " +
                "\033[7m"     + "now inverse, " +
                "\033[30m"    + "[30m → red on black, " +
                "\033[103m"   + "[103m  → yellow on black, " +
                "\033[27m"    + "[27m → black on yellow",
                "<span style=\"color: #CDCD00;\"><span style=\"background-color: #CD0000;\">yellow on red, </span></span>" +     // could be optimized to be a single <span> tag
                "<span style=\"background-color: #CDCD00; color: #CD0000;\">" +
                  "now inverse, " +
                  "<span style=\"background-color: #000000;\">" +
                    "[30m → red on black, " +
                    "<span style=\"color: #FFFF00;\">" +
                      "[103m  → yellow on black, " +
                    "</span>" +
                  "</span>" +
                "</span>" +
                "<span style=\"background-color: #FFFF00; color: #000000;\">[27m → black on yellow</span>");
    }

    @Test
    public void testStrikeout() throws IOException {
        assertThatAnnotateIs("\033[9mhello world\033[29mnormal", "<span style=\"text-decoration: line-through;\">hello world</span>normal");
    }

    @Test
    public void testFramed() throws IOException {
        assertThatAnnotateIs("\033[51mhello world\033[54mnormal", "<span style=\"border: 1px solid;\">hello world</span>normal");
    }

    @Test
    public void testOverlined() throws IOException {
        assertThatAnnotateIs("\033[53mhello world\033[55mnormal", "<span style=\"text-decoration: overline;\">hello world</span>normal");
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
    public void testForegroundColorHighIntensity() throws IOException {
        assertThatAnnotateIs("\033[90mDark gray\033[0m"    , "<span style=\"color: #4C4C4C;\">Dark gray</span>");
        assertThatAnnotateIs("\033[91mLight red\033[0m"    , "<span style=\"color: #FF0000;\">Light red</span>");
        assertThatAnnotateIs("\033[92mLight green\033[0m"  , "<span style=\"color: #00FF00;\">Light green</span>");
        assertThatAnnotateIs("\033[93mLight yellow\033[0m" , "<span style=\"color: #FFFF00;\">Light yellow</span>");
        assertThatAnnotateIs("\033[94mLight blue\033[0m"   , "<span style=\"color: #4682B4;\">Light blue</span>");
        assertThatAnnotateIs("\033[95mLight magenta\033[0m", "<span style=\"color: #FF00FF;\">Light magenta</span>");
        assertThatAnnotateIs("\033[96mLight cyan\033[0m"   , "<span style=\"color: #00FFFF;\">Light cyan</span>");
        assertThatAnnotateIs("\033[97mWhite\033[0m"        , "<span style=\"color: #FFFFFF;\">White</span>");
    }

    @Test
    public void testForegroundColor256() throws IOException {
        // standard colors 0-15
        assertThatAnnotateIs("\033[38;5;0mBlack\033[0m"         , "<span style=\"color: #000000;\">Black</span>");
        assertThatAnnotateIs("\033[38;5;1mRed\033[0m"           , "<span style=\"color: #CD0000;\">Red</span>");
        assertThatAnnotateIs("\033[38;5;2mGreen\033[0m"         , "<span style=\"color: #00CD00;\">Green</span>");
        assertThatAnnotateIs("\033[38;5;3mYellow\033[0m"        , "<span style=\"color: #CDCD00;\">Yellow</span>");
        assertThatAnnotateIs("\033[38;5;4mBlue\033[0m"          , "<span style=\"color: #1E90FF;\">Blue</span>");
        assertThatAnnotateIs("\033[38;5;5mMagenta\033[0m"       , "<span style=\"color: #CD00CD;\">Magenta</span>");
        assertThatAnnotateIs("\033[38;5;6mCyan\033[0m"          , "<span style=\"color: #00CDCD;\">Cyan</span>");
        assertThatAnnotateIs("\033[38;5;7mGray\033[0m"          , "<span style=\"color: #E5E5E5;\">Gray</span>");
        assertThatAnnotateIs("\033[38;5;8mDark gray\033[0m"     , "<span style=\"color: #4C4C4C;\">Dark gray</span>");
        assertThatAnnotateIs("\033[38;5;9mLight red\033[0m"     , "<span style=\"color: #FF0000;\">Light red</span>");
        assertThatAnnotateIs("\033[38;5;10mLight green\033[0m"  , "<span style=\"color: #00FF00;\">Light green</span>");
        assertThatAnnotateIs("\033[38;5;11mLight yellow\033[0m" , "<span style=\"color: #FFFF00;\">Light yellow</span>");
        assertThatAnnotateIs("\033[38;5;12mLight blue\033[0m"   , "<span style=\"color: #4682B4;\">Light blue</span>");
        assertThatAnnotateIs("\033[38;5;13mLight magenta\033[0m", "<span style=\"color: #FF00FF;\">Light magenta</span>");
        assertThatAnnotateIs("\033[38;5;14mLight cyan\033[0m"   , "<span style=\"color: #00FFFF;\">Light cyan</span>");
        assertThatAnnotateIs("\033[38;5;15mWhite\033[0m"        , "<span style=\"color: #FFFFFF;\">White</span>");

        // some of the 6x6x6=216 color cube
        assertThatAnnotateIs("\033[38;5;16mABC\033[0m"          , "<span style=\"color: #000000;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;21mABC\033[0m"          , "<span style=\"color: #0000FF;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;196mABC\033[0m"         , "<span style=\"color: #FF0000;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;201mABC\033[0m"         , "<span style=\"color: #FF00FF;\">ABC</span>");

        assertThatAnnotateIs("\033[38;5;22mABC\033[0m"          , "<span style=\"color: #005F00;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;27mABC\033[0m"          , "<span style=\"color: #005FFF;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;202mABC\033[0m"         , "<span style=\"color: #FF5F00;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;207mABC\033[0m"         , "<span style=\"color: #FF5FFF;\">ABC</span>");

        assertThatAnnotateIs("\033[38;5;28mABC\033[0m"          , "<span style=\"color: #008700;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;33mABC\033[0m"          , "<span style=\"color: #0087FF;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;208mABC\033[0m"         , "<span style=\"color: #FF8700;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;213mABC\033[0m"         , "<span style=\"color: #FF87FF;\">ABC</span>");

        assertThatAnnotateIs("\033[38;5;34mABC\033[0m"          , "<span style=\"color: #00AF00;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;39mABC\033[0m"          , "<span style=\"color: #00AFFF;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;214mABC\033[0m"         , "<span style=\"color: #FFAF00;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;219mABC\033[0m"         , "<span style=\"color: #FFAFFF;\">ABC</span>");

        assertThatAnnotateIs("\033[38;5;40mABC\033[0m"          , "<span style=\"color: #00D700;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;45mABC\033[0m"          , "<span style=\"color: #00D7FF;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;220mABC\033[0m"         , "<span style=\"color: #FFD700;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;225mABC\033[0m"         , "<span style=\"color: #FFD7FF;\">ABC</span>");

        assertThatAnnotateIs("\033[38;5;46mABC\033[0m"          , "<span style=\"color: #00FF00;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;51mABC\033[0m"          , "<span style=\"color: #00FFFF;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;226mABC\033[0m"         , "<span style=\"color: #FFFF00;\">ABC</span>");
        assertThatAnnotateIs("\033[38;5;231mABC\033[0m"         , "<span style=\"color: #FFFFFF;\">ABC</span>");

        // some of the 24 gray shades
        assertThatAnnotateIs("\033[38;5;232mGray\033[0m"        , "<span style=\"color: #080808;\">Gray</span>");
        assertThatAnnotateIs("\033[38;5;240mGray\033[0m"        , "<span style=\"color: #585858;\">Gray</span>");
        assertThatAnnotateIs("\033[38;5;248mGray\033[0m"        , "<span style=\"color: #A8A8A8;\">Gray</span>");
        assertThatAnnotateIs("\033[38;5;255mGray\033[0m"        , "<span style=\"color: #EEEEEE;\">Gray</span>");
    }

    @Test
    public void testForegroundColorRgb() throws IOException {
        assertThatAnnotateIs("\033[38;2;0;0;0mBlack\033[0m"      , "<span style=\"color: #000000;\">Black</span>");
        assertThatAnnotateIs("\033[38;2;255;0;0mRed\033[0m"      , "<span style=\"color: #FF0000;\">Red</span>");
        assertThatAnnotateIs("\033[38;2;0;255;0mGreen\033[0m"    , "<span style=\"color: #00FF00;\">Green</span>");
        assertThatAnnotateIs("\033[38;2;255;255;0mYellow\033[0m" , "<span style=\"color: #FFFF00;\">Yellow</span>");
        assertThatAnnotateIs("\033[38;2;0;0;255mBlue\033[0m"     , "<span style=\"color: #0000FF;\">Blue</span>");
        assertThatAnnotateIs("\033[38;2;255;0;255mMagenta\033[0m", "<span style=\"color: #FF00FF;\">Magenta</span>");
        assertThatAnnotateIs("\033[38;2;0;255;255mCyan\033[0m"   , "<span style=\"color: #00FFFF;\">Cyan</span>");
        assertThatAnnotateIs("\033[38;2;255;255;255mWhite\033[0m", "<span style=\"color: #FFFFFF;\">White</span>");
        assertThatAnnotateIs("\033[38;2;128;128;128mGray\033[0m" , "<span style=\"color: #808080;\">Gray</span>");
    }

    @Test
    public void testResetBackgroundColor() throws IOException {
        assertThatAnnotateIs("\033[42mtic\033[1mtac\033[49mtoe",
                "<span style=\"background-color: #00CD00;\">tic<b>tac</b></span><b>toe</b>");
    }

    @Test
    public void testBackgroundColorHighIntensity() throws IOException {
        assertThatAnnotateIs("\033[100mDark gray\033[0m"    , "<span style=\"background-color: #4C4C4C;\">Dark gray</span>");
        assertThatAnnotateIs("\033[101mLight red\033[0m"    , "<span style=\"background-color: #FF0000;\">Light red</span>");
        assertThatAnnotateIs("\033[102mLight green\033[0m"  , "<span style=\"background-color: #00FF00;\">Light green</span>");
        assertThatAnnotateIs("\033[103mLight yellow\033[0m" , "<span style=\"background-color: #FFFF00;\">Light yellow</span>");
        assertThatAnnotateIs("\033[104mLight blue\033[0m"   , "<span style=\"background-color: #4682B4;\">Light blue</span>");
        assertThatAnnotateIs("\033[105mLight magenta\033[0m", "<span style=\"background-color: #FF00FF;\">Light magenta</span>");
        assertThatAnnotateIs("\033[106mLight cyan\033[0m"   , "<span style=\"background-color: #00FFFF;\">Light cyan</span>");
        assertThatAnnotateIs("\033[107mWhite\033[0m"        , "<span style=\"background-color: #FFFFFF;\">White</span>");
    }

    @Test
    public void testBackgroundColor256() throws IOException {
        // mainly copied code from testForegroundColor256()
        // standard colors 0-15
        assertThatAnnotateIs("\033[48;5;0mBlack\033[0m"         , "<span style=\"background-color: #000000;\">Black</span>");
        assertThatAnnotateIs("\033[48;5;1mRed\033[0m"           , "<span style=\"background-color: #CD0000;\">Red</span>");
        assertThatAnnotateIs("\033[48;5;2mGreen\033[0m"         , "<span style=\"background-color: #00CD00;\">Green</span>");
        assertThatAnnotateIs("\033[48;5;3mYellow\033[0m"        , "<span style=\"background-color: #CDCD00;\">Yellow</span>");
        assertThatAnnotateIs("\033[48;5;4mBlue\033[0m"          , "<span style=\"background-color: #1E90FF;\">Blue</span>");
        assertThatAnnotateIs("\033[48;5;5mMagenta\033[0m"       , "<span style=\"background-color: #CD00CD;\">Magenta</span>");
        assertThatAnnotateIs("\033[48;5;6mCyan\033[0m"          , "<span style=\"background-color: #00CDCD;\">Cyan</span>");
        assertThatAnnotateIs("\033[48;5;7mGray\033[0m"          , "<span style=\"background-color: #E5E5E5;\">Gray</span>");
        assertThatAnnotateIs("\033[48;5;8mDark gray\033[0m"     , "<span style=\"background-color: #4C4C4C;\">Dark gray</span>");
        assertThatAnnotateIs("\033[48;5;9mLight red\033[0m"     , "<span style=\"background-color: #FF0000;\">Light red</span>");
        assertThatAnnotateIs("\033[48;5;10mLight green\033[0m"  , "<span style=\"background-color: #00FF00;\">Light green</span>");
        assertThatAnnotateIs("\033[48;5;11mLight yellow\033[0m" , "<span style=\"background-color: #FFFF00;\">Light yellow</span>");
        assertThatAnnotateIs("\033[48;5;12mLight blue\033[0m"   , "<span style=\"background-color: #4682B4;\">Light blue</span>");
        assertThatAnnotateIs("\033[48;5;13mLight magenta\033[0m", "<span style=\"background-color: #FF00FF;\">Light magenta</span>");
        assertThatAnnotateIs("\033[48;5;14mLight cyan\033[0m"   , "<span style=\"background-color: #00FFFF;\">Light cyan</span>");
        assertThatAnnotateIs("\033[48;5;15mWhite\033[0m"        , "<span style=\"background-color: #FFFFFF;\">White</span>");

        // some of the 6x6x6=216 color cube
        assertThatAnnotateIs("\033[48;5;16mABC\033[0m"          , "<span style=\"background-color: #000000;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;21mABC\033[0m"          , "<span style=\"background-color: #0000FF;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;196mABC\033[0m"         , "<span style=\"background-color: #FF0000;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;201mABC\033[0m"         , "<span style=\"background-color: #FF00FF;\">ABC</span>");

        assertThatAnnotateIs("\033[48;5;22mABC\033[0m"          , "<span style=\"background-color: #005F00;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;27mABC\033[0m"          , "<span style=\"background-color: #005FFF;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;202mABC\033[0m"         , "<span style=\"background-color: #FF5F00;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;207mABC\033[0m"         , "<span style=\"background-color: #FF5FFF;\">ABC</span>");

        assertThatAnnotateIs("\033[48;5;28mABC\033[0m"          , "<span style=\"background-color: #008700;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;33mABC\033[0m"          , "<span style=\"background-color: #0087FF;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;208mABC\033[0m"         , "<span style=\"background-color: #FF8700;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;213mABC\033[0m"         , "<span style=\"background-color: #FF87FF;\">ABC</span>");

        assertThatAnnotateIs("\033[48;5;34mABC\033[0m"          , "<span style=\"background-color: #00AF00;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;39mABC\033[0m"          , "<span style=\"background-color: #00AFFF;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;214mABC\033[0m"         , "<span style=\"background-color: #FFAF00;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;219mABC\033[0m"         , "<span style=\"background-color: #FFAFFF;\">ABC</span>");

        assertThatAnnotateIs("\033[48;5;40mABC\033[0m"          , "<span style=\"background-color: #00D700;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;45mABC\033[0m"          , "<span style=\"background-color: #00D7FF;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;220mABC\033[0m"         , "<span style=\"background-color: #FFD700;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;225mABC\033[0m"         , "<span style=\"background-color: #FFD7FF;\">ABC</span>");

        assertThatAnnotateIs("\033[48;5;46mABC\033[0m"          , "<span style=\"background-color: #00FF00;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;51mABC\033[0m"          , "<span style=\"background-color: #00FFFF;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;226mABC\033[0m"         , "<span style=\"background-color: #FFFF00;\">ABC</span>");
        assertThatAnnotateIs("\033[48;5;231mABC\033[0m"         , "<span style=\"background-color: #FFFFFF;\">ABC</span>");

        // some of the 24 gray shades
        assertThatAnnotateIs("\033[48;5;232mGray\033[0m"        , "<span style=\"background-color: #080808;\">Gray</span>");
        assertThatAnnotateIs("\033[48;5;240mGray\033[0m"        , "<span style=\"background-color: #585858;\">Gray</span>");
        assertThatAnnotateIs("\033[48;5;248mGray\033[0m"        , "<span style=\"background-color: #A8A8A8;\">Gray</span>");
        assertThatAnnotateIs("\033[48;5;255mGray\033[0m"        , "<span style=\"background-color: #EEEEEE;\">Gray</span>");
    }

    @Test
    public void testBackgroundColorRgb() throws IOException {
        // mainly copied code from testForegroundColorRgb()
        assertThatAnnotateIs("\033[48;2;0;0;0mBlack\033[0m"      , "<span style=\"background-color: #000000;\">Black</span>");
        assertThatAnnotateIs("\033[48;2;255;0;0mRed\033[0m"      , "<span style=\"background-color: #FF0000;\">Red</span>");
        assertThatAnnotateIs("\033[48;2;0;255;0mGreen\033[0m"    , "<span style=\"background-color: #00FF00;\">Green</span>");
        assertThatAnnotateIs("\033[48;2;255;255;0mYellow\033[0m" , "<span style=\"background-color: #FFFF00;\">Yellow</span>");
        assertThatAnnotateIs("\033[48;2;0;0;255mBlue\033[0m"     , "<span style=\"background-color: #0000FF;\">Blue</span>");
        assertThatAnnotateIs("\033[48;2;255;0;255mMagenta\033[0m", "<span style=\"background-color: #FF00FF;\">Magenta</span>");
        assertThatAnnotateIs("\033[48;2;0;255;255mCyan\033[0m"   , "<span style=\"background-color: #00FFFF;\">Cyan</span>");
        assertThatAnnotateIs("\033[48;2;255;255;255mWhite\033[0m", "<span style=\"background-color: #FFFFFF;\">White</span>");
        assertThatAnnotateIs("\033[48;2;128;128;128mGray\033[0m" , "<span style=\"background-color: #808080;\">Gray</span>");
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

    @Test
    public void testResetCharacterSet() throws IOException {
        assertThatAnnotateIs("(\033(0)", "()");
        assertThatAnnotateIs("(\033)0)", "()");
    }

    @Test
    public void testFontDefault() throws IOException {
        assertThatAnnotateIs("(\033[10m)", "()");
    }

    private void assertThatAnnotateIs(String ansi, String html) throws IOException {
        assertThat(annotate(ansi), is(html));
    }

    private void assertThatAnnotateIs(AnsiColorMap colorMap, String ansi, String html) throws IOException {
        assertThat(annotate(ansi, colorMap), is(html));
    }

    protected String annotate(String text, AnsiColorMap colorMap) throws IOException {
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
