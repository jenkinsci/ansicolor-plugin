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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.jansi.AnsiOutputStream;

public class AnsiHtmlOutputStream extends AnsiOutputStream {
	private boolean concealOn = false;

	@Override
	public void close() throws IOException {
		closeAttributes();
		super.close();
	}

	private final AnsiColorMap colorMap;

	public AnsiHtmlOutputStream(OutputStream os, final AnsiColorMap colorMap) {
		super(os);
		this.colorMap = colorMap;
	}

	private List<String> closingAttributes = new ArrayList<String>();

	private void write(String s) throws IOException {
		super.out.write(s.getBytes());
	}

	private void writeAttribute(String s) throws IOException {
		write("<" + s + ">");
		closingAttributes.add(0, s.split(" ", 2)[0]);
	}

	private void closeAttributes() throws IOException {
		for (String attr : closingAttributes) {
			write("</" + attr + ">");
		}
		closingAttributes.clear();
	}

	public void writeLine(byte[] buf, int offset, int len) throws IOException {
		write(buf, offset, len);
		closeAttributes();
	}
	
	@Override
	protected void processSetAttribute(int attribute) throws IOException {
		switch (attribute) {
		case ATTRIBUTE_CONCEAL_ON:
			write("\u001B[8m");
			concealOn = true;
			break;
		case ATTRIBUTE_INTENSITY_BOLD:
			writeAttribute("b");
			break;
		case ATTRIBUTE_INTENSITY_NORMAL:
			closeAttributes();
			break;
		case ATTRIBUTE_UNDERLINE:
			writeAttribute("u");
			break;
		case ATTRIBUTE_UNDERLINE_OFF:
			closeAttributes();
			break;
		case ATTRIBUTE_NEGATIVE_ON:
			break;
		case ATTRIBUTE_NEGATIVE_Off:
			break;
		}
	}
	
	@Override
	protected void processAttributeRest() throws IOException {
		if (concealOn) {
			write("\u001B[0m");
			concealOn = false;
		}
		closeAttributes();
	}

	@Override
	protected void processSetForegroundColor(int color) throws IOException {
		writeAttribute("span style=\"color: " + colorMap.getForeground(color) + ";\"");
	}

	@Override
	protected void processSetBackgroundColor(int color) throws IOException {
		writeAttribute("span style=\"background-color: " + colorMap.getBackground(color) + ";\"");
	}
}
