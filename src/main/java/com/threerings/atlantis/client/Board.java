//
// $Id$

package com.threerings.atlantis.client;

import java.util.EnumSet;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Mouse;

import static forplay.core.ForPlay.*;

import pythagoras.f.Points;

import com.threerings.atlantis.shared.Log;
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
        // temp: add some random placements
        EnumSet<Terrain> tiles = EnumSet.allOf(Terrain.class);
        EnumSet<Orient> orients = EnumSet.allOf(Orient.class);
        for (int xx = 0; xx < 5; xx++) {
            Orient orient = Orient.NORTH;
            for (int yy = 0; yy < 5; yy++) {
                addPlacement(new Placement(Atlantis.rands.pick(tiles, null),
                                           false, orient, xx, yy));
                orient = orient.rotate(1);
            }
        }

        mouse().setListener(_placer);
    }

    public void addPlacement (Placement play)
    {
        layer.add(glyphFor(play));
    }

    public void setPlacing (Placement play)
    {
        if (_placing != null) {
            _placing.destroy();
        }
        _placing = glyphFor(play);
        _placing.setAlpha(0.5f);
        layer.add(_placing);
    }

    protected GroupLayer glyphFor (Placement play)
    {
        float hwid = AtlantisTiles.TERRAIN_WIDTH/2, hhei = AtlantisTiles.TERRAIN_HEIGHT/2;
        GroupLayer group = graphics().createGroupLayer();
        group.setOrigin(hwid, hhei);
        group.setRotation((float)Math.PI * play.orient.index / 2);
        group.setTranslation(play.x * AtlantisTiles.TERRAIN_WIDTH + hwid,
                             play.y * AtlantisTiles.TERRAIN_HEIGHT + hhei);
        group.add(Atlantis.tiles.getTerrainTile(play.tile.tileIdx));
        // TODO: shield and piecen
        return group;
    }

    protected GroupLayer _placing;

    protected final Mouse.Listener _placer = new Mouse.Listener() {
        public void onMouseDown (float x, float y, int button) {
            Log.info("Mouse down! " + Points.pointToString(x, y));
        }
        public void onMouseMove (float x, float y) {
        }
        public void onMouseUp (float x, float y, int button) {
        }
        public void onMouseWheelScroll (float velocity) {
        }
    };
}
