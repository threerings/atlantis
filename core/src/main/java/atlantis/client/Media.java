//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import atlantis.client.util.Tiles;

import playn.core.PlayN;
import playn.core.Image;
import playn.core.ImageLayer;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

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
    public static final int NOPIECEN_ACTION = 0, CANCEL_ACTION = 1, ROTATE_ACTION = 2;

    public static final int[] PIECEN_COLORS = {
        0xFF2C88F0, // blue
        0xFF30C94A, // green
        0xFFEC8C2D, // orange
        0xFFEE22D3, // pink
        0xFFE93030, // red
        0xFFEFC734, // yellow
    };

    public void init () {
        _terrain = PlayN.assets().getImage("tiles.png");
        _target = PlayN.assets().getImage("target.png");
        _piecens = PlayN.assets().getImage("piecens.png");
        _shield = PlayN.assets().getImage("shield.png");
        _table = PlayN.assets().getImage("table.png");
        _actions = PlayN.assets().getImage("actions.png");
        // TODO: delay initialization completion until images are loaded
    }

    /**
     * Creates a terrain tile for the specified index.
     */
    public ImageLayer getTerrainTile (int tileIdx) {
        return createTile(_terrain, TERRAIN_WIDTH, TERRAIN_HEIGHT, tileIdx);
    }

    /**
     * Creates a target tile (the tile shown for legal play positions).
     */
    public ImageLayer getTargetTile () {
        return createTile(_target, TERRAIN_WIDTH, TERRAIN_HEIGHT, 0); // only one tile for now
    }

    /**
     * Creates a piecen tile for the specified index. The origin of the tile will be configured to
     * the center of the image.
     */
    public ImageLayer getPiecenTile (int tileIdx) {
        ImageLayer img = createTile(_piecens, PIECEN_WIDTH, PIECEN_HEIGHT, tileIdx);
        img.setOrigin(PIECEN_WIDTH/2f, PIECEN_HEIGHT/2f);
        return img;
    }

    /**
     * Returns the bounds of the specified piecen in the piecen image.
     */
    public IRectangle getPiecenBounds (int tileIdx) {
        return new Rectangle(PIECEN_WIDTH*tileIdx, 0, PIECEN_WIDTH, PIECEN_HEIGHT);
    }

    /**
     * Returns the composite piecen image.
     */
    public Image getPiecensImage () {
        return _piecens;
    }

    /**
     * Creates a shield tile. The origin of the tile will be configured to the center of the image.
     */
    public ImageLayer getShieldTile () {
        ImageLayer img = createTile(_shield, SHIELD_WIDTH, SHIELD_HEIGHT, 0);
        img.setOrigin(SHIELD_WIDTH/2f, SHIELD_HEIGHT/2f);
        return img;
    }

    /**
     * Creates an action tile for the specified index. The origin of the tile will be configured to
     * the center of the image.
     */
    public ImageLayer getActionTile (int tileIdx) {
        ImageLayer img = createTile(_actions, ACTION_WIDTH, ACTION_HEIGHT, tileIdx);
        img.setOrigin(ACTION_WIDTH/2f, ACTION_HEIGHT/2f);
        return img;
    }

    /**
     * Returns the (tileable) table background image.
     */
    public Image getTableImage () {
        return _table;
    }

    protected Image _terrain, _target, _piecens, _shield, _actions, _table;
}
