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
    public void init (AtlantisClient client)
    {
        for (int ii = 0; ii < AtlantisTiles.TERRAIN_COUNT; ii++) {
            ImageLayer tile = client.tiles.getTerrainTile(ii);
            tile.setTranslation(ii * (AtlantisTiles.TERRAIN_WIDTH + 5), 0);
            layer.add(tile);
        }
    }

}
