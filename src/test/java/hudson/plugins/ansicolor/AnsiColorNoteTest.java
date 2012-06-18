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
import hudson.MarkupText;

import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

/**
 * Unit test for the {@link AnsiColorNote} class.
 */
public class AnsiColorNoteTest {
	
	@Test
	public void testAnnotate() throws IOException {
		assertThat(annotate("line"), is("line"));
	}

	@Test
	public void testEscapeHtml() throws IOException {
		// Jenkins seems to have its own idea of when to html-escape the text
		assertThatAnnotateIs("[0m\"", "&quot;", "[0m\"");
		assertThatAnnotateIs("[0m&", "&amp;", "[0m&amp;");
		assertThatAnnotateIs("[0m<", "&lt;", "[0m&lt;");
		assertThatAnnotateIs("[0m>", "&gt;", "[0m>");
	}
	
	@Test
	public void testMultibyte() throws IOException {
		assertThatAnnotateIs("[1m\u3053\u3093\u306b\u3061\u306f", 
				"<b>" + StringEscapeUtils.escapeHtml("\u3053\u3093\u306b\u3061\u306f") + "</b>", 
				"[1m\u3053\u3093\u306b\u3061\u306f");
	}
	
	private void assertThatAnnotateIs(String ansi, String html, String spanned) throws IOException {
		assertThat(annotate(ansi), is(html + span(spanned)));
	}
	
	private String span(String text) {
		return "<span style=\"display: none;\">" + text + "</span>";
	}
	
	private String annotate(String text) throws IOException {
		MarkupText markupText = new MarkupText(text);
		AnsiColorNote note = new AnsiColorNote(text, AnsiColorMap.Default);
		note.annotate(null, markupText, 0);
		return markupText.toString(true);
	}
	
}
