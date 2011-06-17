//
// $Id$

package com.threerings.atlantis.shared;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A mapping from coordinates to tile placements. Implemented such that looking up a placement by x
 * and y coordinate is fast.
 */
public class Placements
{
    /**
     * Adds a placement to this mapping.
     */
    public void add (int x, int y, Placement placement)
    {
        _map.put(toKey(x, y), placement);
    }

    /**
     * Returns the placement at the specified coordinates, or null if no placement has been made at
     * said coordinates.
     */
    public Placement get (int x, int y)
    {
        return _map.get(toKey(x, y));
    }

    protected static int toKey (int x, int y)
    {
        return (x << 16) | (y & 0xFFFF);
    }

    protected Map<Integer, Placement> _map = Maps.newHashMap();
}
