package hudson.plugins.ansicolor;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Represents an HTML elements which maps to an ANSI attribute.
 * <p>
 * An ANSI attribute change may open a new element or close an earlier one (e.g. "bold" opens, "bold off" closes). Because HTML elements cannot overlap, any other enclosing element must be closed
 * beforehand and subsequently reopened.
 * <p>
 * How the HTML is actually emitted depends on the specified {@link AnsiAttributeElement.Emitter}. For Jenkins, the Emitter creates {@link hudson.console.ConsoleNote}s as part of the stream, but for
 * other software or testing the HTML may be emitted otherwise.
 */

class AnsiAttributeElement implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum AnsiAttrType {
        DEFAULT, BOLD, ITALIC, UNDERLINE, STRIKEOUT, FRAMED, OVERLINE, FG, BG, FGBG
    }

    AnsiAttrType ansiAttrType;

    String name;
    String attributes;

    public interface Emitter {
        void emitHtml(@Nonnull String html);

        /**
         * Emit invisible ANSI sequence
         * <p>
         * Used for emitting output for sequences like:
         * <li>SGR0 (Set Graphics Rendition 0)
         * <li>CSI (Control Sequence Introducer)
         * <p>
         * In particular called when SGR0 reset is encountered in the stream, but no tags have been opened since its last occurrence. If you are using the output of AnsiOutputStream directly, you
         * probably don't need to implement this method since the escape sequence will be transparently filtered from the output.
         */
        default void emitInvisibleSequence() { }
    }

    public AnsiAttributeElement(AnsiAttrType ansiAttrType, String name, String attributes) {
        this.ansiAttrType = ansiAttrType;
        this.name = name;
        this.attributes = attributes;
    }

    public void emitOpen(Emitter emitter) {
        final String openingTagHtml = "<" + name + (attributes.trim().equals("") ? "" : " " + attributes) + ">";
        emitter.emitHtml(openingTagHtml);
    }

    public void emitClose(Emitter emitter) {
        String closingTagHtml = "</" + name + ">";
        emitter.emitHtml(closingTagHtml);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnsiAttributeElement tag = (AnsiAttributeElement) o;

        return attributes.equals(tag.attributes) && name.equals(tag.name) && ansiAttrType == tag.ansiAttrType;

    }

    @Override
    public int hashCode() {
        int result = ansiAttrType.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + attributes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AnsiAttributeElement{ansiAttrType=" + ansiAttrType + ",name=" + name + ",attributes=" + attributes + "}";
    }

    public static AnsiAttributeElement bold() {
        return new AnsiAttributeElement(AnsiAttributeElement.AnsiAttrType.BOLD, "b", "");
    }

    public static AnsiAttributeElement italic() {
        return new AnsiAttributeElement(AnsiAttrType.ITALIC, "i", "");
    }

    public static AnsiAttributeElement underline() {
        return new AnsiAttributeElement(AnsiAttrType.UNDERLINE, "u", "");
    }

    public static AnsiAttributeElement underlineDouble() {
        return new AnsiAttributeElement(AnsiAttrType.UNDERLINE, "span", "style=\"border-bottom: 3px double;\"");
    }

    public static AnsiAttributeElement strikeout() {
        return new AnsiAttributeElement(AnsiAttrType.STRIKEOUT, "span", "style=\"text-decoration: line-through;\"");
    }

    public static AnsiAttributeElement framed() {
        return new AnsiAttributeElement(AnsiAttrType.FRAMED, "span", "style=\"border: 1px solid;\"");
    }

    public static AnsiAttributeElement overline() {
        return new AnsiAttributeElement(AnsiAttrType.OVERLINE, "span", "style=\"text-decoration: overline;\"");
    }

}
