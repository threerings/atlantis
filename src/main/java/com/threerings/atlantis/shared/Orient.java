//
// $Id$

package com.threerings.atlantis.shared;

/**
 * The four cardinal directions.
 */
public enum Orient
{
    NORTH(0, 0, -1),
    EAST(1, 1, 0),
    SOUTH(2, 0, 1),
    WEST(3, -1, 0);

    /** This orient's index into by-orientation arrays. */
    public final int index;

    /** The delta by which x changes when moving one tile in this direction. */
    public final int dx;

    /** The delta by which y changes when moving one tile in this direction. */
    public final int dy;

    /** Returns the direction the specified number of clockwise (if positive, counterclockwise if
     * negative) turns from this direction. */
    public Orient rotate (int ticks) {
        while (ticks < 0) ticks += ROTATIONS.length;
        return ROTATIONS[(index + ticks) % ROTATIONS.length];
    }

    /** Returns the direction opposite this one. */
    public Orient opposite () {
        return rotate(2);
    }

    Orient (int index, int dx, int dy) {
        this.index = index;
        this.dx = dx;
        this.dy = dy;
    }

    private static Orient[] ROTATIONS = { NORTH, EAST, SOUTH, WEST };
}
