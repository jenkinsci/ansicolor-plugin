package hudson.plugins.ansicolor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AnsiColorBuildWrapperTest {

    public AnsiColorBuildWrapperTest() {
    }

    @Test
    public void testGetColorMapNameNull() {
        AnsiColorBuildWrapper instance = new AnsiColorBuildWrapper(null, 1, 2);
        assertEquals("xterm", instance.getColorMapName());
    }

    @Test
    public void testGetColorMapNameVga() {
        AnsiColorBuildWrapper instance = new AnsiColorBuildWrapper("vga", 1, 2);
        assertEquals("vga", instance.getColorMapName());
    }

    @Test
    public void testDecorateLogger() {
        AnsiColorBuildWrapper ansiColorBuildWrapper = new AnsiColorBuildWrapper(null, 1, 2);
        assertThat(ansiColorBuildWrapper, instanceOf(AnsiColorBuildWrapper.class));
    }   
}
