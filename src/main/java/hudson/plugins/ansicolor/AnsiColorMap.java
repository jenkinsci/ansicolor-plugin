package hudson.plugins.ansicolor;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.kohsuke.stapler.DataBoundConstructor;

/*
 This class is serialized with the plugin configuration and needs to be backwards
 compatible. Please follow these guidelines:
 https://wiki.jenkins-ci.org/display/JENKINS/Hint+on+retaining+backward+compatibility
 when making changes.
 */
public final class AnsiColorMap implements Serializable {

    private static final long serialVersionUID = 2950700158497010341L;

    public enum Color {

        BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE;

        @Override
        public String toString() {
            // Capitalize the color name.
            return name().substring(0, 1) + name().substring(1).toLowerCase();
        }
    }

    private String name;

    // For backwards compatibility
    private transient String[] fgMap;
    private transient String[] bgMap;

    private Map<Color, String> normalMap = new EnumMap<Color, String>(Color.class);
    private Map<Color, String> brightMap = new EnumMap<Color, String>(Color.class);

    // Those are nullable to not impose any default color on the output.
    private final Integer defaultForeground;
    private final Integer defaultBackground;

    public static final AnsiColorMap XTerm = new AnsiColorMap(
            "xterm",
            "#000000", "#CD0000", "#00CD00", "#CDCD00", "#1E90FF", "#CD00CD", "#00CDCD", "#E5E5E5",
            "#4C4C4C", "#FF0000", "#00FF00", "#FFFF00", "#4682B4", "#FF00FF", "#00FFFF", "#FFFFFF",
            null,
            null
    );

    public static final AnsiColorMap VGA = new AnsiColorMap(
            "vga",
            "#000000", "#AA0000", "#00AA00", "#AA5500", "#0000AA", "#AA00AA", "#00AAAA", "#AAAAAA",
            "#555555", "#FF5555", "#55FF55", "#FFFF55", "#5555FF", "#FF55FF", "#55FFFF", "#FFFFFF",
            Color.WHITE.ordinal(),
            Color.BLACK.ordinal()
    );

    public static final AnsiColorMap CSS = new AnsiColorMap(
            "css",
            "black", "red", "green", "yellow", "blue", "magenta", "cyan", "white",
            "black", "red", "green", "yellow", "blue", "magenta", "cyan", "white",
            null,
            null
    );

    public static final AnsiColorMap GnomeTerminal = new AnsiColorMap(
            "gnome-terminal",
            "#2E3436", "#CC0000", "#4E9A06", "#C4A000", "#3465A4", "#75507B", "#06989A", "#D3D7CF",
            "#2E3436", "#CC0000", "#4E9A06", "#C4A000", "#3465A4", "#75507B", "#06989A", "#D3D7CF",
            Color.WHITE.ordinal(),
            Color.BLACK.ordinal()
    );

    public static final AnsiColorMap Default = XTerm;
    public static final String DefaultName = Default.getName();
    private static final AnsiColorMap[] DefaultColorMaps = {XTerm, VGA, CSS, GnomeTerminal};

    public static AnsiColorMap[] defaultColorMaps() {
        return (AnsiColorMap[]) DefaultColorMaps.clone();
    }

    protected Object readResolve() {
        if (fgMap != null) {
            this.normalMap = new EnumMap<Color, String>(Color.class);
            this.normalMap.put(Color.BLACK, fgMap[0]);
            this.normalMap.put(Color.RED, fgMap[1]);
            this.normalMap.put(Color.GREEN, fgMap[2]);
            this.normalMap.put(Color.YELLOW, fgMap[3]);
            this.normalMap.put(Color.BLUE, fgMap[4]);
            this.normalMap.put(Color.MAGENTA, fgMap[5]);
            this.normalMap.put(Color.CYAN, fgMap[6]);
            this.normalMap.put(Color.WHITE, fgMap[7]);
            fgMap = null;
        }

        if (bgMap != null) {
            this.brightMap = new EnumMap<Color, String>(Color.class);
            this.brightMap.put(Color.BLACK, bgMap[0]);
            this.brightMap.put(Color.RED, bgMap[1]);
            this.brightMap.put(Color.GREEN, bgMap[2]);
            this.brightMap.put(Color.YELLOW, bgMap[3]);
            this.brightMap.put(Color.BLUE, bgMap[4]);
            this.brightMap.put(Color.MAGENTA, bgMap[5]);
            this.brightMap.put(Color.CYAN, bgMap[6]);
            this.brightMap.put(Color.WHITE, bgMap[7]);
            bgMap = null;
        }
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 89).
                append(name).
                append(normalMap).
                append(brightMap).
                append(defaultForeground).
                append(defaultBackground).
                toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AnsiColorMap)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        AnsiColorMap ansiColorMap = (AnsiColorMap) obj;

        return new EqualsBuilder().
                append(name, ansiColorMap.name).                
                append(normalMap, ansiColorMap.normalMap).
                append(brightMap, ansiColorMap.brightMap).
                append(defaultForeground, ansiColorMap.defaultForeground).
                append(defaultBackground, ansiColorMap.defaultBackground).
                isEquals();
    }

    // Those are nullable to not impose any default color on the output.
    @DataBoundConstructor
    public AnsiColorMap(
            String name,
            String black, String red, String green, String yellow, String blue, String magenta, String cyan, String white,
            String blackB, String redB, String greenB, String yellowB, String blueB, String magentaB, String cyanB, String whiteB,
            Integer defaultForeground, Integer defaultBackground) {
        this.name = name;

        this.normalMap.put(Color.BLACK, black);
        this.normalMap.put(Color.RED, red);
        this.normalMap.put(Color.GREEN, green);
        this.normalMap.put(Color.YELLOW, yellow);
        this.normalMap.put(Color.BLUE, blue);
        this.normalMap.put(Color.MAGENTA, magenta);
        this.normalMap.put(Color.CYAN, cyan);
        this.normalMap.put(Color.WHITE, white);

        this.brightMap.put(Color.BLACK, blackB);
        this.brightMap.put(Color.RED, redB);
        this.brightMap.put(Color.GREEN, greenB);
        this.brightMap.put(Color.YELLOW, yellowB);
        this.brightMap.put(Color.BLUE, blueB);
        this.brightMap.put(Color.MAGENTA, magentaB);
        this.brightMap.put(Color.CYAN, cyanB);
        this.brightMap.put(Color.WHITE, whiteB);

        this.defaultForeground = defaultForeground;
        this.defaultBackground = defaultBackground;
    }

    public String getName() {
        return name;
    }

    public String getBlack() {
        return normalMap.get(Color.BLACK);
    }

    public String getRed() {
        return normalMap.get(Color.RED);
    }

    public String getGreen() {
        return normalMap.get(Color.GREEN);
    }

    public String getYellow() {
        return normalMap.get(Color.YELLOW);
    }

    public String getBlue() {
        return normalMap.get(Color.BLUE);
    }

    public String getMagenta() {
        return normalMap.get(Color.MAGENTA);
    }

    public String getCyan() {
        return normalMap.get(Color.CYAN);
    }

    public String getWhite() {
        return normalMap.get(Color.WHITE);
    }

    public String getBlackB() {
        return brightMap.get(Color.BLACK);
    }

    public String getRedB() {
        return brightMap.get(Color.RED);
    }

    public String getGreenB() {
        return brightMap.get(Color.GREEN);
    }

    public String getYellowB() {
        return brightMap.get(Color.YELLOW);
    }

    public String getBlueB() {
        return brightMap.get(Color.BLUE);
    }

    public String getMagentaB() {
        return brightMap.get(Color.MAGENTA);
    }

    public String getCyanB() {
        return brightMap.get(Color.CYAN);
    }

    public String getWhiteB() {
        return brightMap.get(Color.WHITE);
    }

    public String getNormal(int index) {
        return normalMap.get(Color.values()[index]);
    }

    public String getBright(int index) {
        return brightMap.get(Color.values()[index]);
    }

    public Integer getDefaultForeground() {
        return defaultForeground;
    }

    public Integer getDefaultBackground() {
        return defaultBackground;
    }
}
