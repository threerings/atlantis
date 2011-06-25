//
// $Id$

package com.threerings.atlantis.client;

import java.util.List;
import java.util.Set;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Mouse;
import static forplay.core.ForPlay.*;

import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import com.google.common.collect.Lists;

import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Log;
import com.threerings.atlantis.shared.Logic;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Placements;
import static com.threerings.atlantis.client.AtlantisClient.*;

/**
 * Manages the layer that displays the game board.
 */
public class Board
    implements Mouse.Listener
{
    /** The layer that contains the tiles. */
    public final GroupLayer tiles = graphics().createGroupLayer();

    /** The layer that contains the current turn info. */
    public final GroupLayer turnInfo = graphics().createGroupLayer();

    /**
     * Loads up our resources and performs other one-time initialization tasks.
     */
    public void init (GameController ctrl) {
        _ctrl = ctrl;
        mouse().setListener(this);
        tiles.setTranslation((graphics().width() - GameTiles.TERRAIN_WIDTH)/2,
                             (graphics().height() - GameTiles.TERRAIN_HEIGHT)/2);
    }

    /**
     * Resets the board, and prepares for a new game.
     */
    public void reset (Placements plays) {
        // TODO: clear out previously placed tiles
        clearPlacing();

        for (Placement play : plays) {
            addPlacement(play);
        }
    }

    public void addPlacement (Placement play) {
        clearPlacing();
        tiles.add(new PlayGlyph(play).layer);
    }

    public void setPlacing (Placements plays, GameTile tile) {
        clearPlacing();

        _placing = tile;
        _placingPlays = plays;
        _placingGlyph = new PlayGlyph(tile);
        _placingGlyph.setLocation(new Location(0, 0));
        turnInfo.add(_placingGlyph.layer);

        // compute the legal placement positions for this tile
        Set<Location> canPlay = Logic.computeLegalPlays(plays, tile);
        if (canPlay.isEmpty()) {
            Log.warning("Pants! Impossible tile! " + tile);
            // TODO: freak out, end the game, something useful?
        }

        // display targets for all legal moves
        for (Location ploc : canPlay) {
            TargetGlyph target = new TargetGlyph(Atlantis.tiles.getTargetTile(), ploc);
            tiles.add(target.layer);
            _targets.add(target);
        }
    }

    protected void clearPlacing () {
        for (TargetGlyph target : _targets) {
            target.layer.destroy();
        }
        _targets.clear();
        _activeTarget = null;
        _placing = null;
        if (_placingGlyph != null) {
            _placingGlyph.layer.destroy();
            _placingGlyph = null;
        }
    }

    @Override // from interface Mouse.Listener
    public void onMouseDown (float x, float y, int button) {
        // translate the click into (translated) view coordinates
        float vx = x - tiles.transform().tx(), vy = y - tiles.transform().ty();
        TargetGlyph clicked;

        // if they clicked on the active target tile...
        if (_activeTarget != null && _activeTarget.hitTest(vx, vy)) {
            // ...rotate the placing glyph upon't
            int cidx = _placingOrients.indexOf(_placingOrient);
            _placingOrient = _placingOrients.get((cidx + 1) % _placingOrients.size());
            _placingGlyph.setOrient(_placingOrient);
        }

        // check whether they've clicked a non-active target tile (and activate it)
        else if ((clicked = checkHitTarget(vx, vy)) != null) {
            _activeTarget = clicked;
            // if this is the first placement, we need to move our placing glyph from the
            // (non-scrolling) turn info layer, to the (scrolling) tiles layer
            if (_placingGlyph.layer.parent() == turnInfo) {
                turnInfo.remove(_placingGlyph.layer);
                tiles.add(_placingGlyph.layer);
            }
            // compute the valid orientations for the placing tile at this location
            _placingOrients = Logic.computeLegalOrients(_placingPlays, _placing, _activeTarget.loc);
            // TODO: animate!
            _placingOrient = _placingOrients.get(0); // start in the first orientation
            _placingGlyph.setLocation(_activeTarget.loc);
            _placingGlyph.setOrient(_placingOrient);
        }

        // otherwise, let them drag the display around
        else {
            _drag = new Point(x, y);
        }
    }

    @Override // from interface Mouse.Listener
    public void onMouseMove (float x, float y) {
        _current.move(x, y);
        if (_drag != null) {
            tiles.setTranslation(tiles.transform().tx() + (x - _drag.x),
                                 tiles.transform().ty() + (y - _drag.y));
            _drag.move(x, y);
        }
        // updateHover(x, y);
    }

    @Override // from interface Mouse.Listener
    public void onMouseUp (float x, float y, int button) {
        _drag = null;
    }

    @Override // from interface Mouse.Listener
    public void onMouseWheelScroll (float velocity) {
        // nada
    }

    // protected void updateHover (float mx, float my)
    // {
    //     // TODO: we'd like to just apply the layer transform to the mouse coords
    //     float lx = mx - tiles.transform().tx();
    //     float ly = my - tiles.transform().ty();

    //     int hx = (int)Math.floor(lx / GameTiles.TERRAIN_WIDTH);
    //     int hy = (int)Math.floor(ly / GameTiles.TERRAIN_HEIGHT);
    //     if (hx == _hoverX && hy == _hoverY) return;

    //     _hoverX = hx;
    //     _hoverY = hy;

    //     Log.info("New hover " + Points.pointToString(hx, hy));

    //     if (_placing != null) {
    //     }
    // }

    protected TargetGlyph checkHitTarget (float x, float y) {
        for (TargetGlyph target : _targets) {
            if (target.hitTest(x, y)) {
                return target;
            }
        }
        return null;
    }

    protected GameController _ctrl;

    protected GameTile _placing;
    protected Placements _placingPlays;
    protected PlayGlyph _placingGlyph;
    protected Orient _placingOrient;
    protected List<Orient> _placingOrients;
    protected List<TargetGlyph> _targets = Lists.newArrayList();
    protected TargetGlyph _activeTarget;

    /** The x and y coordinate of the tile over which the mouse is hovering. */
    protected int _hoverX, _hoverY;

    protected Point _current = new Point(), _drag;

    protected static abstract class TileGlyph {
        public final GroupLayer layer;

        protected TileGlyph (ImageLayer tile) {
            layer = graphics().createGroupLayer();
            layer.add(tile);

            _bounds = new Rectangle(0, 0, GameTiles.TERRAIN_WIDTH, GameTiles.TERRAIN_HEIGHT);
            layer.setOrigin(_bounds.width/2, _bounds.height/2);
        }

        public IRectangle bounds () {
            return _bounds;
        }

        public void setOrient (Orient orient) {
            layer.setRotation((float)Math.PI * orient.index / 2);
        }

        public void setLocation (Location loc) {
            _bounds.setLocation((loc.x + 0.5f) * GameTiles.TERRAIN_WIDTH,
                                (loc.y + 0.5f) * GameTiles.TERRAIN_HEIGHT);
            layer.setTranslation(_bounds.x, _bounds.y);
        }

        public boolean hitTest (float x, float y) {
            return _bounds.contains(x + _bounds.width/2, y + _bounds.height/2);
        }

        protected Rectangle _bounds;
    }

    protected static class TargetGlyph extends TileGlyph {
        public final Location loc;

        public TargetGlyph (ImageLayer tile, Location loc) {
            super(tile);
            this.loc = loc;
            setLocation(loc);
        }
    }

    // TODO: piecen?
    protected static class PlayGlyph extends TileGlyph {
        public PlayGlyph (Placement play) {
            this(play.tile);
            setOrient(play.orient);
            setLocation(play.loc);
        }

        public PlayGlyph (GameTile tile) {
            super(Atlantis.tiles.getTerrainTile(tile.terrain.tileIdx));
            // TODO: add shield glyph if requested
        }
    }
}
