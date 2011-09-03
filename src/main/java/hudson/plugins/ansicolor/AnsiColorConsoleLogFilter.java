package hudson.plugins.ansicolor;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.OutputStream;

@Extension
public class AnsiColorConsoleLogFilter extends ConsoleLogFilter {

	@SuppressWarnings("unchecked")
	@Override
	public OutputStream decorateLogger(AbstractBuild build, OutputStream logger)
			throws IOException, InterruptedException {
		return new AnsiColorizer(logger, build.getCharset());
	}

}
