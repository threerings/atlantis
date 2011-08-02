//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the {@link Location} class.
 */
public class LocationTest
{
    @Test public void testDirectionTo () {
        Location l = new Location(-15, 42);
        for (Location n : l.neighbors()) {
            assertEquals(l.neighbor(l.directionTo(n)), n);
        }
    }
}
