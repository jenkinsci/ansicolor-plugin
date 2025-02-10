package hudson.plugins.ansicolor.action;

import hudson.MarkupText;
import hudson.model.FreeStyleBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_BEGIN;
import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_END;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionNoteTest {
    private static final UUID UUID = java.util.UUID.randomUUID();

    private ActionNote actionNote;

    @Mock
    private ColorizedAction colorizedAction;

    @Mock
    private FreeStyleBuild buildRun;

    @BeforeEach
    void setUp() throws Exception {
        when(colorizedAction.getId()).thenReturn(UUID.toString());
        actionNote = new ActionNote(colorizedAction);
    }

    @Test
    void canAnnotate() {
        final MarkupText markupText = new MarkupText("abc123");
        assertNull(actionNote.annotate(buildRun, markupText, 4));
        final String output = markupText.toString(false);
        assertTrue(output.contains(TAG_ACTION_BEGIN + "\"" + UUID + "\"" + TAG_ACTION_END));
    }
}
