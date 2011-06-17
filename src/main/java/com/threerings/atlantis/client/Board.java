//
// $Id$

package com.threerings.atlantis.client;

import java.util.EnumSet;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import static forplay.core.ForPlay.*;

import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Terrain;
import static com.threerings.atlantis.client.AtlantisClient.*;

/**
 * Manages the layer that displays the game board.
 */
public class Board
{
    /** The layer that contains the tiles. */
    public final GroupLayer layer = graphics().createGroupLayer();

    /**
     * Loads up our resources and performs other one-time initialization tasks.
     */
    public void init ()
    {
        // _placing will be populated with children only when we have an in-progress placement
        layer.add(_placing);

        // temp: add some random placements
        EnumSet<Terrain> tiles = EnumSet.allOf(Terrain.class);
        EnumSet<Orient> orients = EnumSet.allOf(Orient.class);
        for (int xx = 0; xx < 10; xx++) {
            Orient orient = Orient.NORTH;
            for (int yy = 0; yy < 10; yy++) {
                addPlacement(new Placement(Atlantis.rands.pick(tiles, null),
                                           false, orient, xx, yy));
                orient = orient.rotate(1);
            }
        }
    }

    public void addPlacement (Placement play)
    {
        float hwid = AtlantisTiles.TERRAIN_WIDTH/2, hhei = AtlantisTiles.TERRAIN_HEIGHT/2;
        GroupLayer group = graphics().createGroupLayer();
        group.setOrigin(hwid, hhei);
        group.setRotation((float)Math.PI * play.orient.index / 2);
        group.setTranslation(play.x * AtlantisTiles.TERRAIN_WIDTH + hwid,
                             play.y * AtlantisTiles.TERRAIN_HEIGHT + hhei);
        group.add(Atlantis.tiles.getTerrainTile(play.tile.tileIdx));
        // TODO: shield and piecen
        layer.add(group);
    }

    public void setPlacing (Placement play)
    {
    }

    protected final GroupLayer _placing = graphics().createGroupLayer();
}
