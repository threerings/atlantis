//
// $Id$

package com.threerings.atlantis.shared;

import java.util.Set;

import com.google.common.collect.Sets;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the {@link Logic} class.
 */
public class LogicTest
{
    @Test public void testComputeLegalPlays () {
        Placements plays = new Placements();
        plays.add(new Placement(GameTiles.STARTER, Orient.NORTH, new Location(0, 0)));

        // test a few placements against the starter tile
        checkPlay(plays, Terrain.CITY_TWO, 0,1, 0,-1);
        checkPlay(plays, Terrain.CITY_TWO_ACROSS, 0,1, 0,-1);
        checkPlay(plays, Terrain.FOUR_WAY_ROAD, 1,0, -1,0);
        checkPlay(plays, Terrain.CITY_ONE_ROAD_RIGHT, 1,0, 0,1, -1,0, 0,-1);
        checkPlay(plays, Terrain.CLOISTER_ROAD, 1,0, 0,1, -1,0);
    }

    protected void checkPlay (Placements plays, Terrain terrain, int... expect) {
        GameTile play = new GameTile(terrain, false);
        Set<Location> valid = Sets.newHashSet();
        for (int ii = 0; ii < expect.length; ii += 2) {
            valid.add(new Location(expect[ii], expect[ii+1]));
        }
        assertEquals(valid, Logic.computeLegalPlays(plays, play));
    }
}
