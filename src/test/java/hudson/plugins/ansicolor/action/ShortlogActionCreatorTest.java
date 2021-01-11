package hudson.plugins.ansicolor.action;

import hudson.console.ConsoleNote;
import org.junit.Before;
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
        final String shortlogLine = "\u001B[3B\u001B[2A\u001B[2K \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold\n";
        canCreateActionForShortlog(shortlogActionCreator, shortlogLine, "testlog.log", true);
    }

    @Test
    public void canCreateActionForShortlogBreakLines() {
        canCreateActionForShortlog(shortlogActionCreator, "[Pipeline] echo\n", "testlog.log", false);
    }

    @Test
    public void canCreateActionForShortlogForWindows() {
        final String shortlogLine = "\u001B[3B\u001B[2A\u001B[2K \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mlightgreen bold " +
            "\u001B[92m\u001B[22mlightgreen normal\u001B[0m \u001B[92m\u001B[1mligh";
        final String eol = "\r\n";
        canCreateActionForShortlog(new ShortlogActionCreator(lineIdentifier, eol), shortlogLine + eol, "testlog-crlf.log", true);
    }

    @Test
    public void canCreateActionForShortlogForWindowsBreakLines() {
        final String eol = "\r\n";
        canCreateActionForShortlog(new ShortlogActionCreator(lineIdentifier, eol), "[Pipeline] echo" + eol, "testlog-crlf.log", false);
    }

    private void canCreateActionForShortlog(ShortlogActionCreator shortlogActionCreator, String shortlogLine, String logFile, boolean keepLinesWhole) {
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
        final ColorizedAction shortlogAction = shortlogActionCreator.createActionForShortlog(file, startActions, 3, keepLinesWhole);
        assertEquals(colorizedAction.getColorMapName(), shortlogAction.getColorMapName());
        assertEquals(colorizedAction.getCommand(), shortlogAction.getCommand());
        assertNotEquals(colorizedAction.getId(), shortlogAction.getId());
        assertEquals(lineHash, shortlogAction.getId());
    }

    @Test
    public void wontCreateActionForLogFileShorterThanShortlogLimit() {
        final boolean[] keepLinesWholeOptions = {true, false};
        for (boolean keepLinesWhole : keepLinesWholeOptions) {
            final String serializedNote = "<mock-serialized-note-start>";
            final ColorizedAction colorizedAction = new ColorizedAction("xterm", ColorizedAction.Command.START);
            final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog.log")).getFile());
            final HashMap<String, ColorizedAction> startActions = new HashMap<>();
            startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
            startActions.put(ConsoleNote.PREAMBLE_STR + serializedNote, colorizedAction);
            assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 256, keepLinesWhole));
            verify(lineIdentifier, never()).hash(anyString(), anyLong());
        }
    }

    @Test
    public void wontCreateActionIfBuildHasNoStartActions() {
        final boolean[] keepLinesWholeOptions = {true, false};
        for (boolean keepLinesWhole : keepLinesWholeOptions) {
            final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog.log")).getFile());
            assertNull(shortlogActionCreator.createActionForShortlog(file, new HashMap<>(), 3, keepLinesWhole));
            verify(lineIdentifier, never()).hash(anyString(), anyLong());
        }
    }

    @Test
    public void wontCreateActionIfNoCorrespondingNotesArePresent() {
        final boolean[] keepLinesWholeOptions = {true, false};
        for (boolean keepLinesWhole : keepLinesWholeOptions) {
            final HashMap<String, ColorizedAction> startActions = new HashMap<>();
            startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
            startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-stop0>", new ColorizedAction("css", ColorizedAction.Command.STOP));
            final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog-no-notes.log")).getFile());
            assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 3, keepLinesWhole));
            verify(lineIdentifier, never()).hash(anyString(), anyLong());
        }
    }

    @Test
    public void wontCreateActionIfNoLogFileIsPresent() {
        final boolean[] keepLinesWholeOptions = {true, false};
        for (boolean keepLinesWhole : keepLinesWholeOptions) {
            final HashMap<String, ColorizedAction> startActions = new HashMap<>();
            startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start0>", new ColorizedAction("css", ColorizedAction.Command.START));
            final File file = new File("non-existing.log");
            assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 3, keepLinesWhole));
            verify(lineIdentifier, never()).hash(anyString(), anyLong());
        }
    }

    @Test
    public void wontCreateActionIfActionIsNotActiveAtShortlogLimit() {
        final boolean[] keepLinesWholeOptions = {true, false};
        for (boolean keepLinesWhole : keepLinesWholeOptions) {
            final File file = new File(getClass().getResource(String.join("/", "", getClass().getName().replace('.', '/'), "testlog-action-not-active.log")).getFile());
            final HashMap<String, ColorizedAction> startActions = new HashMap<>();
            startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-start>", new ColorizedAction("css", ColorizedAction.Command.START));
            startActions.put(ConsoleNote.PREAMBLE_STR + "<mock-serialized-note-stop>", new ColorizedAction("css", ColorizedAction.Command.STOP));

            assertNull(shortlogActionCreator.createActionForShortlog(file, startActions, 3, keepLinesWhole));
            verify(lineIdentifier, never()).hash(anyString(), anyLong());
        }
    }

    @Test
    public void canCreateActionForShortlogOnLogLineExceedingBufferSize() {
        final String s = "[Pipeline]  echo a very very very long line,a very very very long line,a very very very long line,a very very very long line,a very very very long line";
        canCreateActionForShortlog(shortlogActionCreator, s + "\n", "testlog-long.log", true);
    }

    @Test
    public void canCreateActionForShortlogOnLogLineExceedingBufferSizeBreakLines() {
        final String s = "[Pipeline]  echo a very very very long line,a very very very long line,a very very very long line,a very very very long line,a very very very long line";
        canCreateActionForShortlog(shortlogActionCreator, s + "\n", "testlog-long.log", false);
    }
}
