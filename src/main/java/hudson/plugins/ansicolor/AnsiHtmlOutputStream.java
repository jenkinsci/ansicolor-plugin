package hudson.plugins.ansicolor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.jansi.AnsiOutputStream;

public class AnsiHtmlOutputStream extends AnsiOutputStream {

	@Override
	public void close() throws IOException {
		closeAttributes();
		super.close();
	}

	private static final String ANSI_COLOR_MAP[] = { "black", "red",
			"green", "yellow", "blue", "magenta", "cyan", "white", };

	public AnsiHtmlOutputStream(OutputStream os) {
		super(os);
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
		closeAttributes();
	}

	@Override
	protected void processSetForegroundColor(int color) throws IOException {
		writeAttribute("span style=\"color: " + ANSI_COLOR_MAP[color] + ";\"");
	}

	@Override
	protected void processSetBackgroundColor(int color) throws IOException {
		writeAttribute("span style=\"background-color: " + ANSI_COLOR_MAP[color] + ";\"");
	}
}
