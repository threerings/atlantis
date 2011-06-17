//
// $Id$

package com.threerings.atlantis.shared;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

import pythagoras.i.Points;

/**
 * Describes the placement of a tile.
 */
public class Placement
{
    /** The type of terrain tile that was placed. */
    public final Terrain tile;

    /** Whether the terrain tile has a shield on it. */
    public final boolean hasShield;

    /** The terrain tile's orientation. */
    public final Orient orient;

    /** The terrain tile's x and y coordinates. */
    public final int x, y;

    /** An array of claim group values that correspond to the features of this tile. If a piecen
     * has claimed a feature on this tile or that connects to this tile, it will be represented
     * here by a non-zero claim group in the array slot that corresponds to the claimed feature. */
    public final int[] claims;

    /** The piecen on this tile, or null if no piecen has been placed. */
    public Piecen piecen;

    /**
     * Creates a placement with the supplied configuration.
     */
    public Placement (Terrain tile, boolean hasShield, Orient orient, int x, int y)
    {
        this.tile = tile;
        this.hasShield = hasShield;
        this.orient = orient;
        this.x = x;
        this.y = y;
        this.claims = new int[tile.features.length];
    }

    /**
     * Returns true if the placed tile has at least one unclaimed feature.
     */
    public boolean hasUnclaimedFeature ()
    {
        return (getUnclaimedCount() > 0);
    }

    /**
     * Returns the count of unclaimed features on our placed tile.
     */
    public int getUnclaimedCount ()
    {
        int count = 0;
        for (int claim : claims) {
            if (claim == 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Looks for a feature in the placed tile that matches the supplied feature edge mask and
     * returns the index of that feature in this tile's {@link #claims} array.
     *
     * @return the index of the matching feature or -1 if no feature matched.
     */
    public int getFeatureIndex (int edgeMask)
    {
        // translate the feature mask into our orientation
        edgeMask = Edge.translateMask(edgeMask, orient); // TODO: -orient.index?

        // look for a feature with a matching edge mask
        for (int ii = 0; ii < tile.features.length; ii ++) {
            if ((tile.features[ii].edgeMask & edgeMask) != 0) {
                return ii;
            }
        }

        // no match
        return -1;
    }

    /**
     * Returns the index of the feature that contains the supplied mouse coordinates (which will
     * have been translated relative to the tile's origin).
     *
     * @return the index of the feature that contains the mouse coordinates. Some feature should
     * always contain the mouse.
     */
    public int getFeatureIndex (int mouseX, int mouseY)
    {
        // we search our features in reverse order because road features overlap grass features
        // geometrically and are known to be specified after the grass features
        for (int ii = tile.features.length-1; ii >= 0; ii--) {
            if (tile.features[ii].contains(mouseX, mouseY, orient)) {
                return ii;
            }
        }

        //  something is hosed; fake it
        Log.warning("Didn't find matching feature for mouse coordinates!?",
                    "tile", this, "mx", mouseX, "my", mouseY);
        return 0;
    }

    /**
     * Looks for a feature in this tile that matches the supplied feature edge mask and returns
     * the claim group to which that feature belongs (which may be zero).
     *
     * @return the claim group to which the feature that matches the supplied mask belongs, or
     * zero if no feature matched the supplied mask.
     */
    public int getFeatureGroup (int edgeMask)
    {
        int fidx = getFeatureIndex(edgeMask);
        return fidx < 0 ? 0 : claims[fidx];
    }

    /**
     * Sets the claim group for the feature with the specified index. This also updates the claim
     * group for any piecen that was placed on that feature as well.
     *
     * @param featureIndex the index of the feature to update.
     * @param claimGroup the claim group to associate with the feature.
     */
    public void setClaimGroup (int featureIndex, int claimGroup)
    {
        // update the claim group slot for this feature
        claims[featureIndex] = claimGroup;

        // if we have a piecen placed on the feature identified by this
        // feature index, we need to update its claim group as well
        if (piecen != null && piecen.featureIndex == featureIndex) {
            piecen.claimGroup = claimGroup;
        }
    }

    /**
     * Places the specified piecen on this tile. The {@link Piecen#featureIndex} field is assumed
     * to be initialized to the feature index of this tile on which the piecen is to be placed.
     *
     * <p> Note that this will call {@link Logic#setClaimGroup} to propagate the claiming of this
     * feature to all neighboring tiles if a non-null tiles array is supplied to the function.
     *
     * @param piecen the piecen to place on this tile (with an appropriately configured feature
     * index).
     * @param places the existing placements on the board, which we will use to propagate our new
     * claim group to all features connected to this newly claimed feature, or null if propagation
     * of the claim group is not desired at this time.
     */
    public void setPiecen (Piecen piecen, Placements places)
    {
        int claimGroup = 0;

        // if we're adding a piecen to a feature that's already claimed, we want to inherit the
        // claim number (this could happen when we show up in an in progress game)
        if (claims[piecen.featureIndex] != 0) {
            Log.warning("Requested to add a piecen to a feature that has already been claimed",
                        "tile", this, "piecen", piecen);
            claimGroup = claims[piecen.featureIndex];

        } else {
            // otherwise we generate a new claim group
            claimGroup = Logic.nextClaimGroup();
            Log.debug("Creating claim group", "cgroup", claimGroup, "tile", this,
                      "fidx", piecen.featureIndex);
        }

        // keep a reference to this piecen and configure its position
        this.piecen = piecen;
        piecen.x = x;
        piecen.y = y;

        // assign a brand spanking new claim group to the feature and the piecen and propagate it
        // to neighboring features
        if (places != null) {
            Logic.setClaimGroup(places, this, piecen.featureIndex, claimGroup);
            // update our piecen with the claim group as well
            piecen.claimGroup = claimGroup;
        }
    }

    /**
     * Clears out any piecen reference that was previously set (does not clear out its associated
     * claim group, however).
     */
    public void clearPiecen ()
    {
        piecen = null;
    }

    @Override
    public boolean equals (Object other)
    {
        Placement op = (Placement)other;
        // only one placement can ever exist at a given x,y so that's sufficient for equals
        return op.x == x && op.y == y;
    }

    @Override
    public String toString ()
    {
        return Objects.toStringHelper(this).
            add("tile", tile).
            add("shield", hasShield).
            add("orient", orient).
            add("pos", Points.pointToString(x, y)).
            add("claims", claims).
            add("piecen", piecen).
            toString();
    }
}
