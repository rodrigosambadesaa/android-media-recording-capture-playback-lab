package gal.rodrigosambade.multimedia.capturelab;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SanityTest {
    @Test public void extensionsAreConventional() {
        assertTrue(".m4a".startsWith("."));
        assertTrue(".mp4".startsWith("."));
    }
}
