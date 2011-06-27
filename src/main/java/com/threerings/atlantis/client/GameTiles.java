//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import com.threerings.atlantis.client.util.Tiles;

import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ImageLayer;

/**
 * Provides easy access to Atlantis tiles.
 */
public class GameTiles extends Tiles
{
    public static final int TERRAIN_WIDTH = 64, TERRAIN_HEIGHT = 64, TERRAIN_COUNT = 19;
    public static final int PIECEN_WIDTH  = 16, PIECEN_HEIGHT  = 16, PIECEN_COUNT  = 6;
    public static final int SHIELD_WIDTH  = 17, SHIELD_HEIGHT  = 17;
    public static final int ACTION_WIDTH  = 16, ACTION_HEIGHT  = 16;
    public static final int OK_ACTION = 0, CANCEL_ACTION = 1, ROTATE_ACTION = 2;

    public void init ()
    {
        _terrain = ForPlay.assetManager().getImage("images/tiles.png");
        _target = ForPlay.assetManager().getImage("images/target.png");
        _piecens = ForPlay.assetManager().getImage("images/piecens.png");
        _shield = ForPlay.assetManager().getImage("images/shield.png");
        _table = ForPlay.assetManager().getImage("images/table.png");
        _actions = ForPlay.assetManager().getImage("images/actions.png");
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
     * Creates a target tile (the tile shown for legal play positions).
     */
    public ImageLayer getTargetTile ()
    {
        return createTile(_target, TERRAIN_WIDTH, TERRAIN_HEIGHT, 0); // only one tile for now
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
     * Creates a action tile for the specified index.
     */
    public ImageLayer getActionTile (int tileIdx)
    {
        return createTile(_actions, ACTION_WIDTH, ACTION_HEIGHT, tileIdx);
    }

    /**
     * Returns the (tileable) table background image.
     */
    public Image getTableImage ()
    {
        return _table;
    }

    protected Image _terrain, _target, _piecens, _shield, _actions, _table;
}
