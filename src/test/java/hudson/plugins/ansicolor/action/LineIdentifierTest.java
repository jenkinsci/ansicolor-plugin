package hudson.plugins.ansicolor.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineIdentifierTest {

    private LineIdentifier lineIdentifier;

    @BeforeEach
    void setUp() throws Exception {
        lineIdentifier = new LineIdentifier();
    }

    @Test
    void canHashLine() {
        assertEquals("ojq32twB56Mha38FSpsOvwxZDdkOKa/SveGHDC4tgHY=", lineIdentifier.hash("test line 123", 735));
    }

    @Test
    void canDetermineIsEqualPositive() {
        assertTrue(lineIdentifier.isEqual("\u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m", 67, "1mo5/lFK3s3+qaz1vK3o62k+EAJkd8Q0j3dMRH8Wkh4="));
    }

    @Test
    void canDetermineIsEqualNegative() {
        assertFalse(lineIdentifier.isEqual("\u001B[92m\u001B[1mlightgreen bold \u001B[92m\u001B[22mlightgreen normal\u001B[0m", 67, "bogus"));
    }
}
