//
// $Id$

package com.threerings.atlantis.data;

import static com.threerings.atlantis.data.Edge.*;

/**
 * Contains metadata for the various tile types.
 */
public enum TerrainTile
{
    /** A four-sided city tile. */
    CITY_FOUR(1, CITY, CITY, CITY, CITY),

    /** A three-sided city tile. */
    CITY_THREE(2, CITY, CITY, GRASS, CITY),

    /** A three-sided city tile with a road. */
    CITY_THREE_ROAD(3, CITY, CITY, ROAD, CITY),

    /** A two-sided city tile with city openings adjacent to one
     * another. */
    CITY_TWO(4, CITY, GRASS, GRASS, CITY),

    /** A two-sided city tile with city openings adjacent to one another
     * and a road connecting the other two sides. */
    CITY_TWO_ROAD(5, CITY, ROAD, ROAD, CITY),

    /** A two-sided city tile with city openings on opposite sides of the
     * tile. */
    CITY_TWO_ACROSS(6, GRASS, CITY, GRASS, CITY),

    /** A two-sided city tile with two separate city arcs adjacent to one
     * another and not connected to each other. */
    TWO_CITY_TWO(7, CITY, CITY, GRASS, GRASS),

    /** A two-sided city tile with two separate city arcs on opposite
     * sides of the tile. */
    TWO_CITY_TWO_ACROSS(8, GRASS, CITY, GRASS, CITY),

    /** A one-sided city tile. */
    CITY_ONE(9, CITY, GRASS, GRASS, GRASS),

    /** A one-sided city tile with a city arc on top and a right facing
     * curved road segment beneath it. */
    CITY_ONE_ROAD_RIGHT(10, CITY, ROAD, ROAD, GRASS),

    /** A one-sided city tile with a city arc on top and a left facing
     * curved road segment beneath it. */
    CITY_ONE_ROAD_LEFT(11, CITY, GRASS, ROAD, ROAD),

    /** A one-sided city tile with a city arc on top and a road tee
     * beneath it. */
    CITY_ONE_ROAD_TEE(12, CITY, ROAD, ROAD, ROAD),

    /** A one-sided city tile with a city arc on top and straight road
     * segment beneath it. */
    CITY_ONE_ROAD_STRAIGHT(13, CITY, ROAD, GRASS, ROAD),

    /** A cloister tile. */
    CLOISTER_PLAIN(14, GRASS, GRASS, GRASS, GRASS),

    /** A cloister tile with a road extending from the cloister. */
    CLOISTER_ROAD(15, GRASS, GRASS, ROAD, GRASS),

    /** A four-way road intersection. */
    FOUR_WAY_ROAD(16, ROAD, ROAD, ROAD, ROAD),

    /** A three-way road intersection. */
    THREE_WAY_ROAD(17, GRASS, ROAD, ROAD, ROAD),

    /** A straight road segment. */
    STRAIGHT_ROAD(18, ROAD, GRASS, ROAD, GRASS),

    /** A curved road segment. */
    CURVED_ROAD(19, GRASS, GRASS, ROAD, ROAD);

    /** The index of this tile in the tiles bitmap. */
    public final int tileIdx;

    /** This types f this tile's edges. */
    public final Edge[] edges;

    TerrainTile (int tileIdx, Edge north, Edge east, Edge south, Edge west)
    {
        this.tileIdx = tileIdx;
        this.edges = new Edge[] { north, east, south, west };
    }
}
