package hudson.plugins.ansicolor;

import hudson.util.XStream2;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class AnsiColorMapTest {

    /*
    Ensure backwards compatibility with 0.4.1 format
    */
    @Test
    public void testDeserialize_0_4_1() {
        assertNotNull("Test file missing", getClass().getResource("/AnsiColorMap-0.4.1.xml"));

        XStream2 xs2 = new XStream2();
        InputStream is = getClass().getResourceAsStream("/AnsiColorMap-0.4.1.xml");
        AnsiColorMap deserializedColorMap = (AnsiColorMap) xs2.fromXML(is);
        
        AnsiColorMap colorMap = new AnsiColorMap("xterm",
                "#000000", "#CD0000", "#00CD00", "#CDCD00", "#1E90FF", "#CD00CD", "#00CDCD", "#E5E5E5",
                "#4C4C4C", "#FF0000", "#00FF00", "#FFFF00", "#4682B4", "#FF00FF", "#00FFFF", "#FFFFFF",
                null, null);
        
        assertEquals(deserializedColorMap, colorMap);
    }
}
