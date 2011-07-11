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

    /** The piecen on this tile, or null if no piecen is placed. */
    public final Piecen piecen;

    /**
     * Creates a placement with the supplied configuration.
     */
    public Placement (GameTile tile, Orient orient, Location loc, Piecen piecen) {
        this.tile = tile;
        this.orient = orient;
        this.loc = loc;
        this.piecen = piecen;
        _claims = new HashMap<Feature,Integer>();
    }

    /**
     * Returns true if the placed tile has at least one unclaimed feature.
     */
    public boolean hasUnclaimedFeature () {
        return (getUnclaimedCount() > 0);
    }

    /**
     * Returns the count of unclaimed features on our placed tile.
     */
    public int getUnclaimedCount () {
        return tile.terrain.features.length - _claims.size();
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

    /**
     * Looks for a feature in this tile that matches the supplied feature edge mask and returns
     * the claim group to which that feature belongs (which may be zero).
     *
     * @return the claim group to which the feature that matches the supplied mask belongs, or
     * zero if no feature matched the supplied mask.
     */
    public int getFeatureGroup (int edgeMask) {
        Feature f = findFeature(edgeMask);
        return (f == null) ? 0 : getClaimGroup(f);
    }

    /**
     * Returns the claim group assigned to the specified feature, or zero if no claim group has
     * been assigned to said feature.
     */
    public int getClaimGroup (Feature f) {
        Integer group = _claims.get(f);
        return (group == null) ? 0 : group;
    }

    /**
     * Sets the claim group for the specified feature. This also updates the claim group for any
     * piecen that was placed on that feature as well.
     */
    public void setClaimGroup (Feature f, int claimGroup) {
        // update the claim group slot for this feature
        Integer ogroup = _claims.put(f, claimGroup);

        // if we have a piecen placed on the feature, we need to update its claim group as well
        if (piecen != null && piecen.featureIdx == getFeatureIndex(f)) {
            piecen.claimGroup = claimGroup;
        }
    }

    @Override
    public boolean equals (Object other) {
        // only one placement can ever exist at a given x,y so that's sufficient for equals
        return loc.equals(((Placement)other).loc);
    }

    @Override
    public String toString () {
        return Log.format("tile", tile, "orient", orient, "loc", loc,
                          "claims", _claims, "piecen", piecen);
    }

    /** A mapping from feature to claim group, for claimed features. */
    protected final Map<Feature,Integer> _claims;
}
