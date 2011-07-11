//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

/**
 * A piecen is a piece and a person all rolled into one!
 *
 * <p>Players can play a single piecen on a tile feature as a part of their turn and that piecen
 * then claims that feature and all of the features that are connected to it, forming a claim
 * group. Once a group of features has been claimed, no further piecens can be placed on the group.
 * Note, however, that two or more separately claimed feature groups can be joined by placing a
 * tile that connects the previously disconnected groups together. In that case, the groups are
 * merged into a single claim group and all the piecens that were part of the disparate groups
 * become claimees in the new group.</p>
 *
 * <p>At scoring time, the player with the most piecens in a group gets the points for the group.
 * If two or more players have equal numbers of piecens in a group, they each get the points for
 * the group.</p>
 */
public class Piecen
{
    /** The piecen colors. (Order is important, don't rearrange!) */
    public enum Color { RED, BLACK, BLUE, YELLOW, GREEN };

    /** The owner of this piecen. */
    public final Color owner;

    /** The coordinates of the tile on which this piecen is placed. */
    public final Location loc;

    /** The index in the tile's feature array of the feature on which this piecen is placed. */
    public final int featureIdx;

    /** The claim group to which this piecen belongs. */
    public int claimGroup;

    /**
     * Constructs a piecen with the specified configuration.
     */
    public Piecen (Color owner, Location loc, int featureIdx) {
        this.owner = owner;
        this.loc = loc;
        this.featureIdx = featureIdx;
    }

    @Override
    public boolean equals (Object other) {
        // only one piecen can ever exist at a given x,y so that's sufficient for equals
        return loc.equals(((Piecen)other).loc);
    }

    @Override
    public String toString () {
        return Log.format("owner", owner, "loc", loc, "fidx", featureIdx, "claim", claimGroup);
    }
}
