package hudson.plugins.ansicolor.action;

import hudson.MarkupText;
import hudson.model.FreeStyleBuild;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_BEGIN;
import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_END;
import static hudson.plugins.ansicolor.action.ColorizedAction.CONTINUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ColorizedActionTest {
    private static final ColorizedAction ACTION_0 = new ColorizedAction("map0", ColorizedAction.Command.START);
    private static final ColorizedAction ACTION_1 = new ColorizedAction("map1", ColorizedAction.Command.CONTINUE);
    private static final ColorizedAction ACTION_2 = new ColorizedAction("map2", ColorizedAction.Command.STOP);

    private ColorizedAction colorizedAction;

    @Mock
    private FreeStyleBuild buildRun;

    @Before
    public void setUp() throws Exception {
        when(buildRun.getActions(eq(ColorizedAction.class))).thenReturn(Arrays.asList(
            ACTION_0,
            ACTION_1,
            ACTION_2
        ));
        colorizedAction = new ColorizedAction("vga", ColorizedAction.Command.START);
    }

    @Test
    public void canInitAndGetProperties() {
        assertNotNull(colorizedAction.getId());
        assertEquals("vga", colorizedAction.getColorMapName());
        assertEquals(ColorizedAction.Command.START, colorizedAction.getCommand());
    }

    @Test
    public void canParseAction() {
        final MarkupText markupText = new MarkupText("Log line");
        markupText.addMarkup(0, TAG_ACTION_BEGIN + "\"" + ACTION_1.getId() + "\"" + TAG_ACTION_END);
        assertEquals(ACTION_1, ColorizedAction.parseAction(markupText, buildRun));
    }

    @Test
    public void willReturnDefaultIfLogDoesntContainAnnotation() {
        final MarkupText markupText = new MarkupText("Log line");
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRun));
    }

    @Test
    public void willReturnDefaultIfLogAnnotationPointsToNonexistingAction() {
        final MarkupText markupText = new MarkupText("Log line");
        markupText.addMarkup(0, TAG_ACTION_BEGIN + "\"identifier_not_in_actions\"" + TAG_ACTION_END);
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRun));
    }
}
