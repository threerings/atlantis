//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import pythagoras.i.Points;

/**
 * Represents a board location.
 */
public class Location
{
    /** The x-coordinate of this location. */
    public final int x;

    /** The y-coordinate of this location. */
    public final int y;

    public Location (int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the location that represents this location's neighbor in the specified direction.
     */
    public Location neighbor (Orient orient) {
        return new Location(x+orient.dx, y+orient.dy);
    }

    /**
     * Returns the neighbors of this location in the four cardinal directions.
     */
    public List<Location> neighbors () {
        return Arrays.asList(new Location[] {
            neighbor(Orient.NORTH),
            neighbor(Orient.EAST),
            neighbor(Orient.SOUTH),
            neighbor(Orient.WEST),
        });
    }

    /**
     * Returns all the locations in this location's 3x3 neighborhood (itself, and its neighbors in
     * the cardinal and intercardinal directions).
     */
    public List<Location> neighborhood () {
        List<Location> ns = Lists.newArrayListWithCapacity(9);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                ns.add(new Location(x+dx, y+dy));
            }
        }
        return ns;
    }

    /**
     * Returns the direction to the supplied other tile.
     */
    public Orient directionTo (Location other) {
        if (x == other.x) {
            switch (y - other.y) {
            case 1: return Orient.NORTH;
            case -1: return Orient.SOUTH;
            }
        } else if (y == other.y) {
            switch (x - other.x) {
            case 1: return Orient.WEST;
            case -1: return Orient.EAST;
            }
        }
        throw new IllegalArgumentException("Not neighbors: " + this + ", " + other);
    }

    @Override
    public boolean equals (Object other) {
        Location oloc = (Location)other;
        return oloc.x == x && oloc.y == y;
    }

    @Override
    public int hashCode () {
        return x ^ y;
    }

    @Override
    public String toString () {
        return Points.pointToString(x, y);
    }
}
