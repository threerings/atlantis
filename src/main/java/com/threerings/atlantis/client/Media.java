//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import com.threerings.atlantis.client.util.Tiles;

import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ImageLayer;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * Provides images and other media.
 */
public class Media extends Tiles
{
    public static final int TERRAIN_WIDTH = 64, TERRAIN_HEIGHT = 64, TERRAIN_COUNT = 19;
    public static final IDimension TERRAIN_SIZE = new Dimension(TERRAIN_WIDTH, TERRAIN_HEIGHT);

    public static final int PIECEN_WIDTH  = 16, PIECEN_HEIGHT  = 16, PIECEN_COUNT  = 6;
    public static final IDimension PIECEN_SIZE = new Dimension(PIECEN_WIDTH, PIECEN_HEIGHT);

    public static final int SHIELD_WIDTH  = 17, SHIELD_HEIGHT  = 17;
    public static final IDimension SHIELD_SIZE = new Dimension(SHIELD_WIDTH, SHIELD_HEIGHT);

    public static final int ACTION_WIDTH  = 16, ACTION_HEIGHT  = 16;
    public static final IDimension ACTION_SIZE = new Dimension(ACTION_WIDTH, ACTION_HEIGHT);
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
     * Creates a piecen tile for the specified index. The origin of the tile will be configured to
     * the center of the image.
     */
    public ImageLayer getPiecenTile (int tileIdx)
    {
        ImageLayer img = createTile(_piecens, PIECEN_WIDTH, PIECEN_HEIGHT, tileIdx);
        img.setOrigin(PIECEN_WIDTH/2f, PIECEN_HEIGHT/2f);
        return img;
    }

    /**
     * Creates a shield tile. The origin of the tile will be configured to the center of the image.
     */
    public ImageLayer getShieldTile ()
    {
        ImageLayer img = createTile(_shield, SHIELD_WIDTH, SHIELD_HEIGHT, 0);
        img.setOrigin(SHIELD_WIDTH/2f, SHIELD_HEIGHT/2f);
        return img;
    }

    /**
     * Creates an action tile for the specified index. The origin of the tile will be configured to
     * the center of the image.
     */
    public ImageLayer getActionTile (int tileIdx)
    {
        ImageLayer img = createTile(_actions, ACTION_WIDTH, ACTION_HEIGHT, tileIdx);
        img.setOrigin(ACTION_WIDTH/2f, ACTION_HEIGHT/2f);
        return img;
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
