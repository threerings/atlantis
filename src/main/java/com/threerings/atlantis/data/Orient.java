//
// $Id$

package com.threerings.atlantis.data;

/**
 * An enumeration of the four cardinal directions.
 */
public enum Orient
{
    NORTH(0), EAST(1), SOUTH(2), WEST(3);

    /** This orient's index into by-orientation arrays. */
    public final int index;

    Orient (int index) {
        this.index = index;
    }
}
