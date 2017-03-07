package hudson.plugins.ansicolor;

/**
 * Represents an HTML elements which maps to an ANSI attribute.
 *
 * An ANSI attribute change may open a new element or close an earlier one (e.g. "bold" opens, "bold off" closes).
 * Because HTML elements cannot overlap, any other enclosing element must be closed beforehand and subsequently
 * reopened.
 *
 * How the HTML is actually emitted depends on the specified {@link AnsiAttributeElement.Emitter}.
 * For Jenkins, the Emitter creates {@link hudson.console.ConsoleNote}s as part of the stream, but for
 * other software or testing the HTML may be emitted otherwise.
 */

class AnsiAttributeElement {
    public static enum AnsiAttrType {
        DEFAULT, BOLD, ITALIC, UNDERLINE, STRIKEOUT, FRAMED, OVERLINE, FG, BG, FGBG
    }

    AnsiAttrType ansiAttrType;

    String name;
    String attributes;

    public static interface Emitter {
        public void emitHtml(String html);
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
}
