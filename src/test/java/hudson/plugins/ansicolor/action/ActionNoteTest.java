package hudson.plugins.ansicolor.action;

import hudson.MarkupText;
import hudson.model.FreeStyleBuild;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_BEGIN;
import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_END;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionNoteTest {
    private static final UUID UUID = java.util.UUID.randomUUID();

    private ActionNote actionNote;

    @Mock
    private ColorizedAction colorizedAction;

    @Mock
    private FreeStyleBuild buildRun;

    @Before
    public void setUp() throws Exception {
        when(colorizedAction.getId()).thenReturn(UUID.toString());
        actionNote = new ActionNote(colorizedAction);
    }

    @Test
    public void canAnnotate() {
        final MarkupText markupText = new MarkupText("abc123");
        assertNull(actionNote.annotate(buildRun, markupText, 4));
        final String output = markupText.toString(false);
        assertTrue(output.contains(TAG_ACTION_BEGIN + "\"" + UUID + "\"" + TAG_ACTION_END));
    }
}
