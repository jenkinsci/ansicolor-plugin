package hudson.plugins.ansicolor;

import java.io.Serializable;				
import org.kohsuke.stapler.DataBoundConstructor;

public final class AnsiColorMap implements Serializable
{
	public static final String XTermName = "xterm";
	private static final String[] XTermFg = { "#000000", "#CD0000", "#00CD00", "#CDCD00", "#1E90FF", "#CD00CD", "#00CDCD", "#E5E5E5" };
	private static final String[] XTermBg = { "#4C4C4C", "#FF0000", "#00FF00", "#FFFF00", "#4682B4", "#FF00FF", "#00FFFF", "#FFFFFF" };

	public static final String VGAName = "vga";
	private static final String[] VGAFg = { "#000000", "#AA0000", "#00AA00", "#AA5500", "#0000AA", "#AA00AA", "#00AAAA", "#AAAAAA" };
	private static final String[] VGABg = { "#555555", "#FF5555", "#55FF55", "#FFFF55", "#5555FF", "#FF55FF", "#55FFFF", "#FFFFFF" };

	public static final String CSSName = "css";
	private static final String[] CSSFg = { "black", "red", "green", "yellow", "blue", "magenta", "cyan", "white" };
	private static final String[] CSSBg = CSSFg;

	public static final AnsiColorMap XTerm = new AnsiColorMap(XTermName, XTermFg, XTermBg);
	public static final AnsiColorMap VGA = new AnsiColorMap(VGAName, VGAFg, VGABg);
	public static final AnsiColorMap CSS = new AnsiColorMap(CSSName, CSSFg, CSSBg);

	public static final AnsiColorMap Default = XTerm;
	public static final String DefaultName = Default.getName();
	private static final AnsiColorMap[] DefaultColorMaps = { XTerm, VGA, CSS };

	public static AnsiColorMap[] defaultColorMaps() {
		return (AnsiColorMap[])DefaultColorMaps.clone();
	}
	
	private String name;
	private final String[] fgMap;
	private final String[] bgMap;

	@DataBoundConstructor
	public AnsiColorMap(
		String name,
		String black, String red, String green, String blue, String yellow, String magenta, String cyan, String white,
		String blackB, String redB, String greenB, String blueB, String yellowB, String magentaB, String cyanB, String whiteB) {

		this(
			name,
			colorArray(black,red,green,blue,yellow,magenta,cyan,white),
			colorArray(blackB,redB,greenB,blueB,yellowB,magentaB,cyanB,whiteB));
	}

	private static String[] colorArray(String a, String b, String c, String d, String e, String f, String g, String h) {
		String[] arr = {a,b,c,d,e,f,g,h};
		return arr;
	}

	public AnsiColorMap(String name, String[] fgMap, String[] bgMap) {
		this.name = name;
		this.fgMap = (String[])fgMap.clone();
		this.bgMap = (String[])bgMap.clone();
	}

	public String getName() { return name; }

	public String[] getBackgroundMap() { return (String[])bgMap.clone(); }
	public String[] getForegroundMap() { return (String[])fgMap.clone(); }

	public String getBlack() { return fgMap[0]; }
	public String getRed() { return fgMap[1]; }
	public String getGreen() { return fgMap[2]; }
	public String getBlue() { return fgMap[3]; }
	public String getYellow() { return fgMap[4]; }
	public String getMagenta() { return fgMap[5]; }
	public String getCyan() { return fgMap[6]; }
	public String getWhite() { return fgMap[7]; }

	public String getBlackB() { return bgMap[0]; }
	public String getRedB() { return bgMap[1]; }
	public String getGreenB() { return bgMap[2]; }
	public String getBlueB() { return bgMap[3]; }
	public String getYellowB() { return bgMap[4]; }
	public String getMagentaB() { return bgMap[5]; }
	public String getCyanB() { return bgMap[6]; }
	public String getWhiteB() { return bgMap[7]; }

	public String getForeground(int index) { return fgMap[index]; }
	public String getBackground(int index) { return bgMap[index]; }
}