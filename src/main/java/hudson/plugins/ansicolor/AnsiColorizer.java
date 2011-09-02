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

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.fusesource.jansi.AnsiString;

/**
 * Time-stamp note that is inserted into the console output.
 * 
 * @author Daniel Doubrovkine
 */
public final class AnsiColorizer extends LineTransformationOutputStream {

	/**
	 * Serialization UID.
	 */
	private static final long serialVersionUID = 1L;
	private final OutputStream out;
	
	@SuppressWarnings("unused")
	private final Charset charset;

	public AnsiColorizer(OutputStream out, Charset charset) {
		this.out = out;
		this.charset = charset;
	}

	@Override
	protected void eol(byte[] b, int len) throws IOException {
		AnsiString ansiString = new AnsiString(new String(b));		
		out.write(ansiString.getPlain().toString().getBytes(), 0, ansiString.length());
	}

	@Override
	public void close() throws IOException {
		super.close();
		out.close();
	}
}
