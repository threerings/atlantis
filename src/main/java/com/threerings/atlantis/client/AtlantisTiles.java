//
// $Id$

package com.threerings.atlantis.client;

import com.threerings.atlantis.client.util.Tiles;

import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ImageLayer;

/**
 * Provides easy access to Atlantis tiles.
 */
public class AtlantisTiles extends Tiles
{
    public static final int TERRAIN_WIDTH = 64, TERRAIN_HEIGHT = 64, TERRAIN_COUNT = 19;
    public static final int PIECEN_WIDTH  = 16, PIECEN_HEIGHT  = 16, PIECEN_COUNT  = 6;
    public static final int SHIELD_WIDTH  = 17, SHIELD_HEIGHT  = 17, SHIELD_COUNT  = 1;

    public void init ()
    {
        _terrain = ForPlay.assetManager().getImage("images/tiles.png");
        _piecens = ForPlay.assetManager().getImage("images/piecens.png");
        _shield = ForPlay.assetManager().getImage("images/shield.png");
        _table = ForPlay.assetManager().getImage("images/table.png");
        // TODO: delay initialization completion until images are loaded
    }

    /**
     * Creates a terrain tile for the specified index.
     */
    public ImageLayer getTerrainTile (int tileIdx)
    {
        return createTile(_terrain, TERRAIN_WIDTH, TERRAIN_HEIGHT, tileIdx);
    }

    /**
     * Creates a piecen tile for the specified index.
     */
    public ImageLayer getPiecenTile (int tileIdx)
    {
        return createTile(_piecens, PIECEN_WIDTH, PIECEN_HEIGHT, tileIdx);
    }

    /**
     * Creates a shield tile.
     */
    public ImageLayer getShieldTile ()
    {
        return createTile(_shield, SHIELD_WIDTH, SHIELD_HEIGHT, 0); // only one tile for now
    }

    /**
     * Returns the (tileable) table background image.
     */
    public Image getTableImage ()
    {
        return _table;
    }

    protected Image _terrain, _piecens, _shield, _table;
}
