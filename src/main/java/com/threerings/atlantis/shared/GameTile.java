//
// $Id$

package com.threerings.atlantis.shared;

/**
 * Contains the metadata for a single game tile. Most of the metadata is encapsulated in the {@link
 * Terrain} enum, but tiles optionally have a shield which is easier to model with this
 * encapsulating class.
 */
public class GameTile
{
    /** The tile used to start a game. */
    public static final GameTile STARTER = new GameTile(Terrain.CITY_ONE_ROAD_STRAIGHT, false);

    /** This tile's terrain and features. */
    public final Terrain terrain;

    /** Whether the city on this tile (if any) contains a shield. */
    public final boolean hasShield;

    public GameTile (Terrain terrain, boolean hasShield) {
        this.terrain = terrain;
        this.hasShield = hasShield;
    }

    /**
     * Returns the edges of this game tile.
     */
    public Edge[] edges () {
        return terrain.edges;
    }

    /**
     * Returns the features on this game tile.
     */
    public Feature[] features () {
        return terrain.features;
    }

    @Override
    public String toString () {
        return terrain + " (" + (hasShield ? "" : "no") + "shield)";
    }
}
