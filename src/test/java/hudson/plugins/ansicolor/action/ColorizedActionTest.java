package hudson.plugins.ansicolor.action;

import hudson.MarkupText;
import hudson.model.FreeStyleBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_BEGIN;
import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_END;
import static hudson.plugins.ansicolor.action.ColorizedAction.CONTINUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColorizedActionTest {
    private static final ColorizedAction ACTION_0 = new ColorizedAction("map0", ColorizedAction.Command.START);
    private static final ColorizedAction ACTION_1 = new ColorizedAction("map0", ColorizedAction.Command.CONTINUE);
    private static final ColorizedAction ACTION_2 = new ColorizedAction("map0", ColorizedAction.Command.STOP);
    private static final ColorizedAction ACTION_3 = new ColorizedAction("map3", ColorizedAction.Command.START);
    private static final ColorizedAction ACTION_4 = new ColorizedAction("map3", ColorizedAction.Command.STOP);
    private static final ColorizedAction ACTION_5 = new ColorizedAction("map0", ColorizedAction.Command.CURRENT);

    private ColorizedAction colorizedAction;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FreeStyleBuild buildRunSingleStart;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FreeStyleBuild buildRunMultipleStarts;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FreeStyleBuild buildRunOneCurrent;

    @BeforeEach
    void setUp() throws Exception {
        when(buildRunSingleStart.getActions(eq(ColorizedAction.class))).thenReturn(Arrays.asList(
            ACTION_0,
            ACTION_1,
            ACTION_2
        ));
        when(buildRunMultipleStarts.getActions(eq(ColorizedAction.class))).thenReturn(Arrays.asList(
            ACTION_0,
            ACTION_1,
            ACTION_2,
            ACTION_3,
            ACTION_4
        ));
        when(buildRunOneCurrent.getActions(eq(ColorizedAction.class))).thenReturn(Arrays.asList(
            ACTION_0,
            ACTION_5
        ));

        colorizedAction = new ColorizedAction("vga", ColorizedAction.Command.START);
    }

    @Test
    void canInitAndGetProperties() {
        assertNotNull(colorizedAction.getId());
        assertEquals("vga", colorizedAction.getColorMapName());
        assertEquals(ColorizedAction.Command.START, colorizedAction.getCommand());
    }

    @Test
    void canParseActionSingleStart() {
        final MarkupText markupText = new MarkupText("Log line");
        markupText.addMarkup(0, TAG_ACTION_BEGIN + "\"" + ACTION_1.getId() + "\"" + TAG_ACTION_END);
        assertEquals(ACTION_1, ColorizedAction.parseAction(markupText, buildRunSingleStart));
    }

    @Test
    void willNotTriggerStartIfThereIsExactlyOneStartActionAndNoCorrespondingAnnotation() {
        final MarkupText markupText = new MarkupText("Log line");
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRunSingleStart));
    }

    @Test
    void willReturnDefaultIfLogAnnotationPointsToNonexistentActionSingleStart() {
        final MarkupText markupText = new MarkupText("Log line");
        markupText.addMarkup(0, TAG_ACTION_BEGIN + "\"identifier_not_in_actions\"" + TAG_ACTION_END);
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRunSingleStart));
    }

    @Test
    void willReturnCommandIgnoreOnPipelineInternalLineSingleStart() {
        final MarkupText markupText = new MarkupText("Some internal line");
        markupText.addMarkup(0, "<span class=\"pipeline-new-node\">");
        final ColorizedAction colorizedAction = ColorizedAction.parseAction(markupText, buildRunSingleStart);
        assertEquals(ColorizedAction.Command.IGNORE, colorizedAction.getCommand());
    }

    @Test
    void canParseActionMultipleStarts() {
        final MarkupText markupText = new MarkupText("Log line");
        markupText.addMarkup(0, TAG_ACTION_BEGIN + "\"" + ACTION_3.getId() + "\"" + TAG_ACTION_END);
        assertEquals(ACTION_3, ColorizedAction.parseAction(markupText, buildRunMultipleStarts));
    }

    @Test
    void willReturnDefaultIfLogDoesntContainAnnotation() {
        final MarkupText markupText = new MarkupText("Log line");
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRunMultipleStarts));
    }

    @Test
    void willReturnDefaultIfLogAnnotationPointsToNonexistentActionMultipleStarts() {
        final MarkupText markupText = new MarkupText("Log line");
        markupText.addMarkup(0, TAG_ACTION_BEGIN + "\"identifier_not_in_actions\"" + TAG_ACTION_END);
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRunMultipleStarts));
    }

    @Test
    void willReturnCommandIgnoreOnPipelineInternalLineMultipleStarts() {
        final MarkupText markupText = new MarkupText("Some internal line");
        markupText.addMarkup(0, "<span class=\"pipeline-new-node\">");
        final ColorizedAction colorizedAction = ColorizedAction.parseAction(markupText, buildRunMultipleStarts);
        assertEquals(ColorizedAction.Command.IGNORE, colorizedAction.getCommand());
    }

    @Test
    void canParseActionCurrentWhileBuildRunning() {
        when(buildRunOneCurrent.isBuilding()).thenReturn(true);
        final MarkupText markupText = new MarkupText("Log line");
        assertEquals(ACTION_5, ColorizedAction.parseAction(markupText, buildRunOneCurrent));
    }

    @Test
    void wontParseActionCurrentWhileBuildNotRunning() {
        when(buildRunOneCurrent.isBuilding()).thenReturn(false);
        final MarkupText markupText = new MarkupText("Log line");
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRunOneCurrent));
    }

    @Test
    void wontParseActionCurrentWhileBuildRunningButNoCurrentAction() {
        when(buildRunSingleStart.isBuilding()).thenReturn(true);
        final MarkupText markupText = new MarkupText("Log line");
        assertEquals(CONTINUE, ColorizedAction.parseAction(markupText, buildRunSingleStart));
    }
}
