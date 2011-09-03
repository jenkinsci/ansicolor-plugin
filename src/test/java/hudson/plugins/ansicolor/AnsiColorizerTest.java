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

import java.io.IOException;
import java.nio.charset.Charset;

import org.codehaus.plexus.util.StringOutputStream;
import org.junit.Test;

/**
 * Unit test for the {@link AnsiColorizer} class.
 */
public class AnsiColorizerTest {

	/**
	 * @throws IOException
	 */
	@Test
	public void testNoMarkup() throws IOException {
		assertThat(colorize("line"), is("line"));
	}

	@Test
	public void testClear() throws IOException {
		assertThat(colorize("[0m[K"), is(""));
		assertThat(colorize("[0mhello world"), is("hello world"));
	}

	@Test
	public void testBold() throws IOException {
		assertThat(colorize("[1mhello world"), is("<b>hello world</b>"));
	}

	@Test
	public void testGreen() throws IOException {
		assertThat(colorize("[32mhello world"),
				is("<font color=\"green\">hello world</font>"));
	}

	@Test
	public void testGreenOnWhite() throws IOException {
		assertThat(
				colorize("[47;32mhello world"),
				is("<span style=\"background-color: white\"><font color=\"green\">hello world</font></span>"));
	}

	private String colorize(String text) throws IOException {
		StringOutputStream out = new StringOutputStream();
		AnsiColorizer colorizer = new AnsiColorizer(out, Charset
				.defaultCharset());
		colorizer.eol(text.getBytes(), text.length());
		return out.toString();
	}
}
