package hudson.plugins.ansicolor;

import hudson.model.Run;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.junit.Test;
import static org.junit.Assert.*;

public class AnsiColorSimpleBuildWrapperTest {

    public AnsiColorSimpleBuildWrapperTest() {
    }

    @Test
    public void testCreateLoggerDecorator() {
        AnsiColorSimpleBuildWrapper ansiColorSimpleBuildWrapper = new AnsiColorSimpleBuildWrapper("default");
        assertThat(ansiColorSimpleBuildWrapper.createLoggerDecorator(null),
                instanceOf(AnsiColorSimpleBuildWrapper.ConsoleLogFilterImpl.class));
    }

    @Test
    public void testHashCode() {
        try {
            AnsiColorSimpleBuildWrapper original = new AnsiColorSimpleBuildWrapper("default");
            byte[] serialized = SerializationUtils.serialize(original);
            AnsiColorSimpleBuildWrapper copy = (AnsiColorSimpleBuildWrapper)SerializationUtils.deserialize(serialized);
            assertEquals(original.hashCode(), copy.hashCode());
        } catch (SerializationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testEquals() {
        try {
            AnsiColorSimpleBuildWrapper original = new AnsiColorSimpleBuildWrapper("default");
            byte[] serialized = SerializationUtils.serialize(original);
            AnsiColorSimpleBuildWrapper copy = (AnsiColorSimpleBuildWrapper)SerializationUtils.deserialize(serialized);
            assertEquals(original, copy);
        } catch (SerializationException e) {
            fail(e.getMessage());
        }
    }

}
