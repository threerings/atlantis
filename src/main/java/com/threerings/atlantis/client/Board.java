//
// $Id$

package com.threerings.atlantis.client;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;

import static forplay.core.ForPlay.*;
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
        // temp: add some random tiles
        for (int xx = 0; xx < 10; xx++) {
            for (int yy = 0; yy < 10; yy++) {
                addTile(xx, yy, Atlantis.rando.nextInt(AtlantisTiles.TERRAIN_COUNT));
            }
        }
    }

    protected void addTile (int xx, int yy, int tileIdx)
    {
        ImageLayer tile = Atlantis.tiles.getTerrainTile(tileIdx);
        // TODO: use tile.width(), tile.height() once those exist
        tile.setTranslation(xx * AtlantisTiles.TERRAIN_WIDTH, yy * AtlantisTiles.TERRAIN_HEIGHT);
        layer.add(tile);
    }
}
