//
// $Id$

package com.threerings.atlantis.client;

import java.util.EnumSet;
import java.util.Set;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Mouse;
import static forplay.core.ForPlay.*;

import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Points;

import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Log;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;
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
    public void init (GameController ctrl)
    {
        _ctrl = ctrl;
        mouse().setListener(_scroller);
        layer.setTranslation((graphics().width() - AtlantisTiles.TERRAIN_WIDTH)/2,
                             (graphics().height() - AtlantisTiles.TERRAIN_HEIGHT)/2);
    }

    /**
     * Resets the board, and prepares for a new game.
     */
    public void reset (Set<Placement> plays)
    {
        // TODO: the resetting part
        for (Placement play : plays) {
            addPlacement(play);
        }
    }

    public void addPlacement (Placement play)
    {
        layer.add(new PlayGlyph(play).layer);
    }

    public void setPlacing (GameTile tile)
    {
        if (_placingGlyph != null) {
            _placingGlyph.layer.destroy();
        }
        _placing = tile;
        _placingGlyph = new PlayGlyph(tile);
        _placingGlyph.layer.setAlpha(0.5f);
        layer.add(_placingGlyph.layer);
    }

    protected void updateHover (float mx, float my)
    {
        // TODO: we'd like to just apply the layer transform to the mouse coords
        float lx = mx - layer.transform().tx();
        float ly = my - layer.transform().ty();

        int hx = (int)Math.floor(lx / AtlantisTiles.TERRAIN_WIDTH);
        int hy = (int)Math.floor(ly / AtlantisTiles.TERRAIN_HEIGHT);
        if (hx == _hoverX && hy == _hoverY) return;

        _hoverX = hx;
        _hoverY = hy;

        Log.info("New hover " + Points.pointToString(hx, hy));

        if (_placing != null) {
        }
    }

    protected GameController _ctrl;
    protected GameTile _placing;
    protected PlayGlyph _placingGlyph;

    /** The x and y coordinate of the tile over which the mouse is hovering. */
    protected int _hoverX, _hoverY;

    protected final Mouse.Listener _scroller = new Mouse.Listener() {
        public void onMouseDown (float x, float y, int button) {
            _drag = new Point(x, y);
        }

        public void onMouseMove (float x, float y) {
            _current.move(x, y);
            if (_drag != null) {
                layer.setTranslation(layer.transform().tx() + (x - _drag.x),
                                     layer.transform().ty() + (y - _drag.y));
                _drag.move(x, y);
            }
            updateHover(x, y);
        }

        public void onMouseUp (float x, float y, int button) {
            _drag = null;
        }

        public void onMouseWheelScroll (float velocity) {
        }

        protected Point _current = new Point(), _drag;
    };

    // TODO: piecen?
    protected static class PlayGlyph {
        public final GroupLayer layer;

        public PlayGlyph (Placement play) {
            this(play.tile);
            setOrient(play.orient);
            setLocation(play.x, play.y);
        }

        public PlayGlyph (GameTile tile) {
            float hwid = AtlantisTiles.TERRAIN_WIDTH/2, hhei = AtlantisTiles.TERRAIN_HEIGHT/2;
            layer = graphics().createGroupLayer();
            layer.setOrigin(hwid, hhei);
            layer.add(Atlantis.tiles.getTerrainTile(tile.terrain.tileIdx));
            // TODO: add shield glyph if requested
        }

        public void setOrient (Orient orient) {
            layer.setRotation((float)Math.PI * orient.index / 2);
        }

        public void setLocation (int x, int y) {
            layer.setTranslation((x + 0.5f) * AtlantisTiles.TERRAIN_WIDTH,
                                 (y + 0.5f) * AtlantisTiles.TERRAIN_HEIGHT);
        }
    }
}
