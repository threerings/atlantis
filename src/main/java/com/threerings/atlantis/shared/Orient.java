//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import pythagoras.f.FloatMath;

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
        while (ticks < 0) ticks += ORIENTS.length;
        return ORIENTS[(index + ticks) % ORIENTS.length];
    }

    /** Returns the direction opposite this one. */
    public Orient opposite () {
        return rotate(2);
    }

    /** Returns the rotation (in radians) to render this orientation. */
    public float rotation () {
        return ROTS[index];
    }

    Orient (int index, int dx, int dy) {
        this.index = index;
        this.dx = dx;
        this.dy = dy;
    }

    private static Orient[] ORIENTS = { NORTH, EAST, SOUTH, WEST };
    private static float[] ROTS = { 0, FloatMath.HALF_PI, -FloatMath.PI, -FloatMath.HALF_PI };
}
