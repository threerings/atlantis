//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import java.util.List;
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
        Logic logic = new Logic();
        logic.addPlacement(new Placement(Rules.STARTER, Orient.NORTH, new Location(0, 0)));

        // test a few placements against the starter tile
        checkPlay(logic, Terrain.CITY_TWO, 0,1, 0,-1);
        checkPlay(logic, Terrain.CITY_TWO_ACROSS, 0,1, 0,-1);
        checkPlay(logic, Terrain.FOUR_WAY_ROAD, 1,0, -1,0);
        checkPlay(logic, Terrain.CITY_ONE_ROAD_RIGHT, 1,0, 0,1, -1,0, 0,-1);
        checkPlay(logic, Terrain.CLOISTER_ROAD, 1,0, 0,1, -1,0);
        checkPlay(logic, Terrain.CITY_FOUR, 0,-1);
    }

    protected void checkPlay (Logic logic, Terrain terrain, int... expect) {
        GameTile play = new GameTile(terrain, false);
        Set<Location> valid = Sets.newHashSet();
        for (int ii = 0; ii < expect.length; ii += 2) {
            valid.add(new Location(expect[ii], expect[ii+1]));
        }
        assertEquals(valid, logic.computeLegalPlays(play));
    }

    @Test public void testDoubleScore () {
        Logic logic = new Logic();
        logic.addPlacement(new Placement(Rules.STARTER, Orient.NORTH, new Location(0, 0)));
        logic.addPlacement(placement(Terrain.CITY_THREE, Orient.EAST, 0, -1));
        logic.addPiecen(new Piecen(0, new Location(0, -1), 0));
        logic.addPlacement(placement(Terrain.CITY_TWO, Orient.SOUTH, 0, -2));
        logic.addPlacement(placement(Terrain.CITY_TWO, Orient.NORTH, 1, -1));
        Placement play = placement(Terrain.TWO_CITY_TWO, Orient.SOUTH, 1, -2);
        logic.addPlacement(play);

        List<Logic.FeatureScore> scores = logic.computeScores(play);
        assertEquals(1, scores.size());
        assertEquals(10, scores.get(0).score);
    }

    protected static Placement placement (Terrain tile, Orient orient, int x, int y) {
        return new Placement(new GameTile(tile, false), orient, new Location(x, y));
    }
}
