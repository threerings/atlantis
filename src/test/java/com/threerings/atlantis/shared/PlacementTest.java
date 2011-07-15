//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests various bits of the {@link Placement} class.
 */
public class PlacementTest
{
    @Test public void testFindFeature () {
        // take a straight road tile (with features as numbered):
        // +-------+
        // |   |   |
        // |0  2  1|
        // |   |   |
        // +-------+
        //
        // rotate it into the EAST orientation
        // +-------+
        // |   0   | ENE
        // |===2===|
        // |   1   | ESE
        // +-------+
        GameTile road = new GameTile(Terrain.STRAIGHT_ROAD, false);
        Location loc = new Location(0, 0);
        Placement p = new Placement(road, Orient.EAST, loc);
        // then ensure that asking for the feature connected to its ENE edge is 0:
        assertEquals(road.terrain.features[0], p.findFeature(Edge.ENE_F));
        // and ESE edge is 1:
        assertEquals(road.terrain.features[1], p.findFeature(Edge.ESE_F));
    }
}
