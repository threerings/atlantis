//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Code relating to the rules of the game.
 */
public class Rules
{
    /** The tile used to start a game. */
    public static final GameTile STARTER = new GameTile(Terrain.CITY_ONE_ROAD_STRAIGHT, false);

    /** The number of piecens granted to each player at the start of the game. */
    public static final int STARTING_PIECENS = 5;

    /**
     * Creates and returns the standard selection of game tiles.
     */
    public static List<GameTile> standardTiles () {
        List<GameTile> tiles = Lists.newArrayList();
        add(1, tiles, Terrain.CITY_FOUR, true);

        add(3, tiles, Terrain.CITY_THREE, false);
        add(1, tiles, Terrain.CITY_THREE, true);
        add(1, tiles, Terrain.CITY_THREE_ROAD, false);
        add(2, tiles, Terrain.CITY_THREE_ROAD, true);

        add(3, tiles, Terrain.CITY_TWO, false);
        add(2, tiles, Terrain.CITY_TWO, true);
        add(3, tiles, Terrain.CITY_TWO_ROAD, false);
        add(2, tiles, Terrain.CITY_TWO_ROAD, true);
        add(1, tiles, Terrain.CITY_TWO_ACROSS, false);
        add(2, tiles, Terrain.CITY_TWO_ACROSS, true);

        add(2, tiles, Terrain.TWO_CITY_TWO, false);
        add(3, tiles, Terrain.TWO_CITY_TWO_ACROSS, false);

        add(5, tiles, Terrain.CITY_ONE, false);
        add(3, tiles, Terrain.CITY_ONE_ROAD_RIGHT, false);
        add(3, tiles, Terrain.CITY_ONE_ROAD_LEFT, false);
        add(3, tiles, Terrain.CITY_ONE_ROAD_TEE, false);
        add(3, tiles, Terrain.CITY_ONE_ROAD_STRAIGHT, false);

        add(4, tiles, Terrain.CLOISTER_PLAIN, false);
        add(2, tiles, Terrain.CLOISTER_ROAD, false);

        add(1, tiles, Terrain.FOUR_WAY_ROAD, false);
        add(4, tiles, Terrain.THREE_WAY_ROAD, false);
        add(8, tiles, Terrain.STRAIGHT_ROAD, false);
        add(9, tiles, Terrain.CURVED_ROAD, false);

        return tiles;
    }

    protected static void add (int count, List<GameTile> tiles,
                               Terrain terrain, boolean hasShield) {
        for (int ii = 0; ii < count; ii++) {
            tiles.add(new GameTile(terrain, hasShield));
        }
    }
}
