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

import hudson.Extension;
import hudson.Launcher;
import hudson.console.LineTransformationOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.io.OutputStream;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Build wrapper that decorates the build's logger to insert a
 * {@link AnsiColorNote} on each output line.
 * 
 * @author Daniel Doubrovkine
 */
public final class AnsiColorBuildWrapper extends BuildWrapper {

	/**
	 * Create a new {@link AnsiColorBuildWrapper}.
	 */
	@DataBoundConstructor
	public AnsiColorBuildWrapper() {

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		return new Environment() {
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public OutputStream decorateLogger(AbstractBuild build, OutputStream logger) {
		return new AnsiColorOutputStream(logger);
	}

	/**
	 * Output stream that writes each line to the provided delegate output
	 * stream after inserting a {@link AnsiColorNote}.
	 */
	private static class AnsiColorOutputStream extends
			LineTransformationOutputStream {

		/**
		 * The delegate output stream.
		 */
		private final OutputStream delegate;

		/**
		 * Create a new {@link AnsiColorOutputStream}.
		 * 
		 * @param delegate
		 *            the delegate output stream
		 */
		private AnsiColorOutputStream(OutputStream delegate) {
			this.delegate = delegate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void eol(byte[] b, int len) throws IOException {
			new AnsiColorNote(new String(b, 0, len)).encodeTo(delegate);
			delegate.write(b, 0, len);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws IOException {
			super.close();
			delegate.close();
		}
	}

	/**
	 * Registers {@link AnsiColorBuildWrapper} as a {@link BuildWrapper}.
	 */
	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getDisplayName() {
			return Messages.DisplayName();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}
	}
}
