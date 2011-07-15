//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the placement of a tile.
 */
public class Placement
{
    /** The metadata for the tile that was placed. */
    public final GameTile tile;

    /** The terra[in tile's orientation. */
    public final Orient orient;

    /** The terrain tile's x and y coordinates. */
    public final Location loc;

    /**
     * Creates a placement with the supplied configuration.
     */
    public Placement (GameTile tile, Orient orient, Location loc) {
        this.tile = tile;
        this.orient = orient;
        this.loc = loc;
    }

    /**
     * Returns the feature at the specified index on this tile.
     */
    public Feature getFeature (int featureIdx) {
        return tile.terrain.features[featureIdx];
    }

    /**
     * Returns the index of the specified feature in this tile.
     * @throws IllegalArgumentException if the feature is not part of this tile.
     */
    public int getFeatureIndex (Feature f) {
        for (int ii = 0, ll = tile.terrain.features.length; ii < ll; ii++) {
            if (f == tile.terrain.features[ii]) return ii;
        }
        throw new IllegalArgumentException("Feature " + f + " not part of " + tile.terrain);
    }

    /**
     * Returns the feature that matches the supplied edge mask or null if no feature matches.
     * @param edgeMask the desired edge mask, in canonical orientation.
     */
    public Feature findFeature (int edgeMask) {
        // translate the feature mask into our orientation
        edgeMask = Edge.translateMask(edgeMask, -orient.index);
        for (Feature f : tile.terrain.features) {
            if ((f.edgeMask & edgeMask) != 0) return f;
        }
        return null;
    }

    @Override
    public boolean equals (Object other) {
        // only one placement can ever exist at a given x,y so that's sufficient for equals
        return loc.equals(((Placement)other).loc);
    }

    @Override
    public String toString () {
        return Log.format("tile", tile, "orient", orient, "loc", loc);
    }
}
