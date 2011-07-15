//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.List;
import java.util.ArrayList;

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
        List<GameTile> tiles = new ArrayList<GameTile>();
        add(tiles, Terrain.CITY_FOUR, true, 1);

        add(tiles, Terrain.CITY_THREE, false, 3);
        add(tiles, Terrain.CITY_THREE, true, 1);
        add(tiles, Terrain.CITY_THREE_ROAD, false, 1);
        add(tiles, Terrain.CITY_THREE_ROAD, true, 2);

        add(tiles, Terrain.CITY_TWO, false, 3);
        add(tiles, Terrain.CITY_TWO, true, 2);
        add(tiles, Terrain.CITY_TWO_ROAD, false, 3);
        add(tiles, Terrain.CITY_TWO_ROAD, true, 2);
        add(tiles, Terrain.CITY_TWO_ACROSS, false, 1);
        add(tiles, Terrain.CITY_TWO_ACROSS, true, 2);

        add(tiles, Terrain.TWO_CITY_TWO, false, 2);
        add(tiles, Terrain.TWO_CITY_TWO_ACROSS, false, 3);

        add(tiles, Terrain.CITY_ONE, false, 5);
        add(tiles, Terrain.CITY_ONE_ROAD_RIGHT, false, 3);
        add(tiles, Terrain.CITY_ONE_ROAD_LEFT, false, 3);
        add(tiles, Terrain.CITY_ONE_ROAD_TEE, false, 3);
        add(tiles, Terrain.CITY_ONE_ROAD_STRAIGHT, false, 3);

        add(tiles, Terrain.CLOISTER_PLAIN, false, 4);
        add(tiles, Terrain.CLOISTER_ROAD, false, 2);

        add(tiles, Terrain.FOUR_WAY_ROAD, false, 1);
        add(tiles, Terrain.THREE_WAY_ROAD, false, 4);
        add(tiles, Terrain.STRAIGHT_ROAD, false, 8);
        add(tiles, Terrain.CURVED_ROAD, false, 9);

        return tiles;
    }

    protected static void add (List<GameTile> tiles, Terrain terrain, boolean hasShield,
                               int count) {
        for (int ii = 0; ii < count; ii++) {
            tiles.add(new GameTile(terrain, hasShield));
        }
    }
}
