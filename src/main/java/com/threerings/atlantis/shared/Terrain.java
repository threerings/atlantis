//
// $Id$

package com.threerings.atlantis.shared;

import static com.threerings.atlantis.shared.Edge.*;

/**
 * Contains metadata for the various terrain tile types.
 */
public enum Terrain
{
    /** A four-sided city tile. */
    CITY_FOUR(
        0, CITY, CITY, CITY, CITY,
        Feature.NESW_CITY),

    /** A three-sided city tile. */
    CITY_THREE(
        1, CITY, CITY, GRASS, CITY,
        Feature.NEW_CITY,
        Feature.S_GRASS),

    /** A three-sided city tile with a road. */
    CITY_THREE_ROAD(
        2, CITY, CITY, ROAD, CITY,
        Feature.NEW_CITY,
        Feature.grass(SSW_F, 1,-4, 0,4, 1,3, 2,3, 2,4),
        Feature.grass(SSE_F, 3,-4, 2,4, 2,3, 3,3, 4,4),
        Feature.road(SOUTH_F, 2,-4, 2,3, 2,4)),

    /** A two-sided city tile with city openings adjacent to one another. */
    CITY_TWO(
        3, CITY, GRASS, GRASS, CITY,
        Feature.NW_CITY,
        Feature.grass(EAST_F|SOUTH_F, 3,-4, 0,4, 4,0, 4,4)),

    /** A two-sided city tile with city openings adjacent to one another and a road connecting the
     * other two sides. */
    CITY_TWO_ROAD(
        4, CITY, ROAD, ROAD, CITY,
        Feature.NW_CITY,
        Feature.grass(ENE_F|SSW_F, -3,-2, 0,4, 4,0, 4,2, 2,4),
        Feature.grass(ESE_F|SSE_F, -4,-4, 2,4, 4,2, 4,4),
        Feature.road(EAST_F|SOUTH_F, 3,3, 2,4, 4,2)),

    /** A two-sided city tile with city openings on opposite sides of the tile. */
    CITY_TWO_ACROSS(
        5, GRASS, CITY, GRASS, CITY,
        Feature.EW_CITY,
        Feature.grass(NORTH_F, 2,-1, 0,0, 1,1, 3,1, 4,0),
        Feature.S_GRASS),

    /** A two-sided city tile with two separate city arcs adjacent to one another and not connected
     * to each other. */
    TWO_CITY_TWO(
        6, CITY, CITY, GRASS, GRASS,
        Feature.grass(WEST_F|SOUTH_F, -1,-3, 0,0, 4,0, 4,4, 0,4),
        Feature.city(NORTH_F, 2,-1, 0,0, 2,1, 4,0),
        Feature.city(EAST_F, -4,2, 4,0, 3,2, 4,4)),

    /** A two-sided city tile with two separate city arcs on opposite sides of the tile. */
    TWO_CITY_TWO_ACROSS(
        7, GRASS, CITY, GRASS, CITY,
        Feature.grass(NORTH_F|SOUTH_F, 2,-4, 0,0, 4,0, 3,1, 3,3, 4,4, 0,4, 1,3, 1,1),
        Feature.W_CITY, Feature.E_CITY),

    /** A one-sided city tile. */
    CITY_ONE(
        8, CITY, GRASS, GRASS, GRASS,
        Feature.grass(EAST_F|SOUTH_F|WEST_F, 2,-3, 0,0, 1,1, 3,1, 4,0, 4,4, 0,4),
        Feature.N_CITY),

    /** A one-sided city tile with a city arc on top and a right facing curved road segment beneath
     * it. */
    CITY_ONE_ROAD_RIGHT(
        9, CITY, ROAD, ROAD, GRASS,
        Feature.grass(ENE_F|SSW_F|WEST_F, 1,2, 0,0, 1,1, 3,1, 4,0, 4,2, 2,2, 2,4, 0,4),
        Feature.SE_GRASS,
        Feature.road(EAST_F|SOUTH_F, 3,2, 4,2, 2,2, 2,4),
        Feature.N_CITY),

    /** A one-sided city tile with a city arc on top and a left facing curved road segment below. */
    CITY_ONE_ROAD_LEFT(
        10, CITY, GRASS, ROAD, ROAD,
        Feature.grass(EAST_F|SSE_F|WNW_F, 3,2, 0,0, 1,1, 3,1, 4,0, 4,4, 2,4, 2,2, 0,2),
        Feature.SW_GRASS,
        Feature.road(SOUTH_F|WEST_F, 1,2, 0,2, 2,2, 2,4),
        Feature.N_CITY),

    /** A one-sided city tile with a city arc on top and a road tee below. */
    CITY_ONE_ROAD_TEE(
        11, CITY, ROAD, ROAD, ROAD,
        Feature.grass(ENE_F|WNW_F, -1,1, 0,0, 1,1, 3,1, 4,0, 4,2, 0,2),
        Feature.SE_GRASS, Feature.SW_GRASS,
        Feature.E_ROAD, Feature.S_ROAD, Feature.W_ROAD,
        Feature.N_CITY),

    /** A one-sided city tile with a city arc on top and straight road segment below. */
    CITY_ONE_ROAD_STRAIGHT(
        12, CITY, ROAD, GRASS, ROAD,
        Feature.grass(ENE_F|WNW_F, -1,1, 0,0, 1,1, 3,1, 4,0, 4,2, 0,2),
        Feature.grass(ESE_F|SOUTH_F|WSW_F, 2,3, 0,2, 4,2, 4,4, 0,4),
        Feature.road(EAST_F|WEST_F, 2,2, 0,2, 4,2),
        Feature.N_CITY),

    /** A cloister tile. */
    CLOISTER_PLAIN(
        13, GRASS, GRASS, GRASS, GRASS,
        Feature.grass(NORTH_F|EAST_F|SOUTH_F|WEST_F, -1,-1, 0,0, 4,0, 4,4, 0,4),
        Feature.cloister(0, 2,2, 1,1, 3,1, 3,3, 1,3)),

    /** A cloister tile with a road extending from the cloister. */
    CLOISTER_ROAD(
        14, GRASS, GRASS, ROAD, GRASS,
        Feature.grass(NORTH_F|EAST_F|WEST_F|SSE_F|SSW_F, -1,-1, 0,0, 4,0, 4,4, 0,4),
        Feature.cloister(0, 2,2, 1,1, 3,1, 3,3, 1,3),
        Feature.road(SOUTH_F, 2,-4, 2,3, 2,4)),

    /** A four-way road intersection. */
    FOUR_WAY_ROAD(
        15, ROAD, ROAD, ROAD, ROAD,
        Feature.grass(WNW_F|NNW_F, -1,-1, 0,0, 2,0, 2,2, 0,2),
        Feature.grass(NNE_F|ENE_F, -4,-1, 2,0, 4,0, 4,2, 2,2),
        Feature.SE_GRASS, Feature.SW_GRASS,
        Feature.road(NORTH_F, 2,1, 2,0, 2,2),
        Feature.E_ROAD, Feature.S_ROAD, Feature.W_ROAD),

    /** A three-way road intersection. */
    THREE_WAY_ROAD(
        16, GRASS, ROAD, ROAD, ROAD,
        Feature.grass(WNW_F|NORTH_F|ENE_F, 2,-1, 0,0, 4,0, 4,2, 0,2),
        Feature.SE_GRASS, Feature.SW_GRASS,
        Feature.E_ROAD, Feature.S_ROAD, Feature.W_ROAD),

    /** A straight road segment. */
    STRAIGHT_ROAD(
        17, ROAD, GRASS, ROAD, GRASS,
        Feature.grass(NNW_F|WEST_F|SSW_F, -1,2, 0,0, 2,0, 2,4, 0,4),
        Feature.grass(SSE_F|EAST_F|NNE_F, -4,2, 2,0, 4,0, 4,4, 2,4),
        Feature.road(NORTH_F|SOUTH_F, 2,2, 2,0, 2,4)),

    /** A curved road segment. */
    CURVED_ROAD(
        18, GRASS, GRASS, ROAD, ROAD,
        Feature.grass(WNW_F|NORTH_F|EAST_F|SSE_F, 3,1, 0,0, 4,0, 4,4, 2,4, 2,2, 0,2),
        Feature.SW_GRASS,
        Feature.road(SOUTH_F|WEST_F, 1,2, 0,2, 2,2, 2,4));

    /** The index of this tile in the tiles bitmap. */
    public final int tileIdx;

    /** The edges of this terrain tile. */
    public final Edge[] edges;

    /** The features on this terrain tile. */
    public final Feature[] features;

    Terrain (int tileIdx, Edge north, Edge east, Edge south, Edge west, Feature... features)
    {
        this.tileIdx = tileIdx;
        this.edges = new Edge[] { north, east, south, west };
        this.features = features;
    }
}
