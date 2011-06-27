//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * A mapping from coordinates to tile placements. Implemented such that looking up a placement by x
 * and y coordinate is fast.
 */
public class Placements implements Iterable<Placement>
{
    /**
     * Adds a placement to this mapping.
     */
    public void add (Placement placement)
    {
        _map.put(placement.loc, placement);
    }

    /**
     * Returns the placement at the specified coordinates, or null if no placement has been made at
     * said coordinates.
     */
    public Placement get (Location loc)
    {
        return _map.get(loc);
    }

    @Override // from interface Iterable<Placment>
    public Iterator<Placement> iterator ()
    {
        return _map.values().iterator();
    }

    protected Map<Location, Placement> _map = new HashMap<Location, Placement>();
}
