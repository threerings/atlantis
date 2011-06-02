//
// $Id$

package com.threerings.atlantis.client;

import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.ImageLayer;

import static forplay.core.ForPlay.*;

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
        _tiles = assetManager().getImage("images/tiles.png");
        // temp
        layer.add(graphics().createImageLayer(_tiles));
    }

    protected Image _tiles;
}
