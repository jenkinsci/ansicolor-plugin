package hudson.plugins.ansicolor.action;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import hudson.model.Run;

/**
 * Marker note accompanying a ColorizedAction showing where an action needs to take place
 */
public class ActionNote extends ConsoleNote<Run<?, ?>> {
    static final String TAG_ACTION_BEGIN = "<div style=\"display:none\" data-ansicolor-action=";
    private static final String TAG_ACTION_ID_TEMPLATE = "\"%s\"";
    static final String TAG_ACTION_END = "></div>";

    private final String actionId;

    public ActionNote(ColorizedAction action) {
        actionId = action.getId();
    }

    @Override
    public ConsoleAnnotator<Run<?, ?>> annotate(Run<?, ?> context, MarkupText text, int charPos) {
        text.addMarkup(charPos, TAG_ACTION_BEGIN + String.format(TAG_ACTION_ID_TEMPLATE, actionId) + TAG_ACTION_END);
        return null;
    }
}
