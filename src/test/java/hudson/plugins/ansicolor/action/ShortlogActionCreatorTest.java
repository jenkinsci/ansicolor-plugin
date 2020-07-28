package hudson.plugins.ansicolor.action;

import hudson.console.ConsoleNote;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShortlogActionCreatorTest {
    private ShortlogActionCreator shortlogActionCreator;

    @Mock
    private LineIdentifier lineIdentifier;

    @Before
    public void setUp() throws Exception {
        shortlogActionCreator = new ShortlogActionCreator(lineIdentifier, "\n");
    }

    @Test
    public void canCreateActionForShortlog() {
        final String eol = "\n";
        canCreateActionForShortlog(shortlogActionCreator, eol, "testlog.log");
    }

    @Test
    public void canCreateActionForShortlogForWindows() {
        final String eol = "\r\n";
        canCreateActionForShortlog(new ShortlogActionCreator(lineIdentifier, eol), eol, "testlog-crlf.log");
    }

    private void canCreateActionForShortlog(ShortlogActionCreator shortlogActionCreator, String eol, String logFile) {
        final String lineHash = "mock-line-hash";
        final ColorizedAction colorizedAction = new ColorizedAction("xterm", ColorizedAction.Command.START);
        final String serializedNote = "<mock-serialized-note>";
        when(lineIdentifier.hash(eq("[Pipeline] echo" + eol), eq(1L))).thenReturn(lineHash);

        final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), logFile)).getFile());
        final HashMap<String, ColorizedAction> startActions = new HashMap<>();
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note0>", new ColorizedAction("css", ColorizedAction.Command.START));
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note1>", new ColorizedAction("vga", ColorizedAction.Command.START));
        startActions.put(ConsoleNote.PREAMBLE_STR + serializedNote, colorizedAction);
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note2>", new ColorizedAction("gnome-terminal", ColorizedAction.Command.START));

        final ColorizedAction shortlogAction = shortlogActionCreator.createActionForShortlog(file, startActions, 3);
        assertEquals(colorizedAction.getColorMapName(), shortlogAction.getColorMapName());
        assertEquals(colorizedAction.getCommand(), shortlogAction.getCommand());
        assertNotEquals(colorizedAction.getId(), shortlogAction.getId());
        assertEquals(lineHash, shortlogAction.getId());
    }
}
