package hudson.plugins.ansicolor.action;

import hudson.console.ConsoleNote;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        canCreateActionForShortlog(shortlogActionCreator, "[Pipeline] echo\n", "testlog.log");
    }

    @Test
    public void canCreateActionForShortlogForWindows() {
        final String eol = "\r\n";
        canCreateActionForShortlog(new ShortlogActionCreator(lineIdentifier, eol), "[Pipeline] echo" + eol, "testlog-crlf.log");
    }

    private void canCreateActionForShortlog(ShortlogActionCreator shortlogActionCreator, String shortlogLine, String logFile) {
        final String lineHash = "mock-line-hash";
        final ColorizedAction colorizedAction = new ColorizedAction("xterm", ColorizedAction.Command.START);
        final String serializedNote = "<mock-serialized-note-start>";
        when(lineIdentifier.hash(eq(shortlogLine), eq(1L))).thenReturn(lineHash);

        final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), logFile)).getFile());
        final HashMap<String, ColorizedAction> startActions = new HashMap<>();
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-stop0>", new ColorizedAction("css", ColorizedAction.Command.STOP));
        startActions.put(ConsoleNote.PREAMBLE_STR + serializedNote, colorizedAction);
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-stop>", new ColorizedAction("xterm", ColorizedAction.Command.STOP));
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start1>", new ColorizedAction("gnome-terminal", ColorizedAction.Command.START));
        final ColorizedAction shortlogAction = shortlogActionCreator.createActionForShortlog(file, startActions, 3, false);
        assertEquals(colorizedAction.getColorMapName(), shortlogAction.getColorMapName());
        assertEquals(colorizedAction.getCommand(), shortlogAction.getCommand());
        assertNotEquals(colorizedAction.getId(), shortlogAction.getId());
        assertEquals(lineHash, shortlogAction.getId());
    }

    @Test
    public void wontCreateActionForLogFileShorterThanShortlogLimit() {
        final String serializedNote = "<mock-serialized-note-start>";
        final ColorizedAction colorizedAction = new ColorizedAction("xterm", ColorizedAction.Command.START);
        final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog.log")).getFile());
        final HashMap<String, ColorizedAction> startActions = new HashMap<>();
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
        startActions.put(ConsoleNote.PREAMBLE_STR + serializedNote, colorizedAction);
        assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 256, false));
        verify(lineIdentifier, never()).hash(anyString(), anyLong());
    }

    @Test
    public void wontCreateActionIfBuildHasNoStartActions() {
        final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog.log")).getFile());
        assertNull(shortlogActionCreator.createActionForShortlog(file, new HashMap<>(), 3, false));
        verify(lineIdentifier, never()).hash(anyString(), anyLong());
    }

    @Test
    public void wontCreateActionIfNoCorrespondingNotesArePresent() {
        final HashMap<String, ColorizedAction> startActions = new HashMap<>();
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-stop0>", new ColorizedAction("css", ColorizedAction.Command.STOP));
        final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog-no-notes.log")).getFile());
        assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 3, false));
        verify(lineIdentifier, never()).hash(anyString(), anyLong());
    }

    @Test
    public void wontCreateActionIfNoLogFileIsPresent() {
        final HashMap<String, ColorizedAction> startActions = new HashMap<>();
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
        final File file = new File("non-existing.log");
        assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 3, false));
        verify(lineIdentifier, never()).hash(anyString(), anyLong());
    }

    @Test
    public void wontCreateActionIfActionIsNotActiveAtShortlogLimit() {
        final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog-action-not-active.log")).getFile());
        final HashMap<String, ColorizedAction> startActions = new HashMap<>();
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start>", new ColorizedAction("css", ColorizedAction.Command.START));
        startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-stop>", new ColorizedAction("css", ColorizedAction.Command.STOP));

        assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 3, false));
        verify(lineIdentifier, never()).hash(anyString(), anyLong());
    }

    @Test
    public void canCreateActionForShortlogOnLogLineExceedingBufferSize() {
        final String s = "[Pipeline]  echo a very very very long line,a very very very long line,a very very very long line,a very very very long line,a very very very long line";
        canCreateActionForShortlog(shortlogActionCreator, s + "\n", "testlog-long.log");
    }

    @Test
    @Ignore
    public void canCreateActionForShortlogPreJenkins2261() {
        // todo
    }
}
