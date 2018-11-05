package hudson.plugins.ansicolor;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

/**
 * A simple ConsoleNote which represents just a simple tag.
 * @deprecated Only here for serial form compatibility.
 */
@Deprecated
public class SimpleHtmlNote extends ConsoleNote<Object> {
    private String tagHtml;

    public SimpleHtmlNote(String tagHtml) {
        this.tagHtml = tagHtml;
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        text.addMarkup(charPos, tagHtml);
        return null;
    }
}
