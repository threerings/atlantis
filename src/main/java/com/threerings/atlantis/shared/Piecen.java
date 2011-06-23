//
// $Id$

package com.threerings.atlantis.shared;

import com.google.common.base.Objects;

/**
 * A piecen is a person and a piece all rolled into one! Players can play a single piecen on a tile
 * feature as a part of their turn and that piecen then claims that feature and all of the features
 * that are connected to it forming a claim group. Once a group of features has been claimed, no
 * further piecens can be placed on the group. Note, however, that two or more separately claimed
 * feature groups can be joined by placing a tile that connects the previously disconnected groups
 * together. In that case, the groups are merged into a single claim group and all the piecens that
 * were part of the disparate groups become claimees in the new group. At scoring time, the player
 * with the most piecens in a group gets the points for the group. If two or more players have
 * equal numbers of piecens in a group, they each get the points for the group.
 */
public class Piecen
{
    /** The piecen colors. (Order is important, don't rearrange!) */
    public enum Color { RED, BLACK, BLUE, YELLOW, GREEN };

    /** The owner of this piecen. */
    public Color owner;

    /** The coordinates of the tile on which this piecen is placed. */
    public Location loc;

    /** The index in the tile's feature array of the feature on which this piecen is placed. */
    public int featureIndex;

    /** The claim group to which this piecen belongs. */
    public int claimGroup;

    /**
     * Constructs a piecen with the specified configuration.
     */
    public Piecen (Color owner, Location loc, int featureIndex)
    {
        this.owner = owner;
        this.loc = loc;
        this.featureIndex = featureIndex;
    }

    @Override
    public boolean equals (Object other)
    {
        // only one piecen can ever exist at a given x,y so that's sufficient for equals
        return loc.equals(((Piecen)other).loc);
    }

    @Override
    public String toString ()
    {
        return Objects.toStringHelper(this).
            add("owner", owner).
            add("loc", loc).
            add("feat", featureIndex).
            add("claim", claimGroup).
            toString();
    }
}
