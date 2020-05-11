package hudson.plugins.ansicolor.command.action;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import hudson.model.Run;

/**
 * Marker note accompanying a ColorizedAction showing where an action needs to take place
 */
public class ActionNote extends ConsoleNote<Run<?, ?>> {

    private static final String TAG_ACTION_BEGIN = "<div data-ansicolor-action=";
    private static final String TAG_ACTION_ID_TEMPLATE = "\"%s\"";
    private static final String TAG_ACTION_END = "></div>";

    private final String actionId;
    private static final ColorizedAction CONTINUE = new ColorizedAction("", ColorizedAction.Command.CONTINUE);

    public ActionNote(ColorizedAction action) {
        actionId = action.getId().toString();
    }

    @Override
    public ConsoleAnnotator<Run<?, ?>> annotate(Run<?, ?> context, MarkupText text, int charPos) {
        text.addMarkup(charPos, TAG_ACTION_BEGIN + String.format(TAG_ACTION_ID_TEMPLATE, actionId) + TAG_ACTION_END);
        return null;
    }

    public static ColorizedAction parseAction(MarkupText text, Run<?, ?> run) {
        final String line = text.toString(false);
        final int actionIdOffset = line.indexOf(TAG_ACTION_BEGIN);
        if (actionIdOffset != -1) {
            final int from = actionIdOffset + TAG_ACTION_BEGIN.length() + 1;
            final int to = line.indexOf("\"", from);
            final String id = line.substring(from, to);
            return run.getActions(ColorizedAction.class).stream().filter(a -> id.equals(a.getId().toString())).findAny().orElse(CONTINUE);
        }
        return CONTINUE;
    }
}
