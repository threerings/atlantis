//
// $Id$

package com.threerings.atlantis.shared;

/**
 * An enumeration of the four cardinal directions.
 */
public enum Orient
{
    NORTH(0), EAST(1), SOUTH(2), WEST(3);

    /** This orient's index into by-orientation arrays. */
    public final int index;

    /** Returns the orientation the specified number of clockwise (if positive, counterclockwise if
     * negative) turns from this orientation. */
    public Orient rotate (int ticks) {
        while (ticks < 0) ticks += ROTATIONS.length;
        return ROTATIONS[(index + ticks) % ROTATIONS.length];
    }

    Orient (int index) {
        this.index = index;
    }

    private static Orient[] ROTATIONS = { NORTH, EAST, SOUTH, WEST };
}
