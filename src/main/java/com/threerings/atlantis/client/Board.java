//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

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
        _placer = new Placer(plays, tile);
    }

    protected void clearPlacing () {
        if (_placer != null) {
            _placer.clear();
            _placer = null;
        }
    }

    @Override // from interface Mouse.Listener
    public void onMouseDown (float x, float y, int button) {
        // translate the click into (translated) view coordinates
        float vx = x - tiles.transform().tx(), vy = y - tiles.transform().ty();

        // if the click isn't consumed by tile placement, let it start a drag
        if (_placer == null || _placer.onMouseDown(vx, vy)) {
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

    protected GameController _ctrl;
    protected Point _current = new Point(), _drag;
    protected Placer _placer;

    /** The x and y coordinate of the tile over which the mouse is hovering. */
    protected int _hoverX, _hoverY;

    /** Handles the interaction of placing a new tile on the board. */
    protected class Placer {
        public Placer (Placements plays, GameTile placing) {
            _placing = placing;
            _plays = plays;
            _glyph = new PlayGlyph(placing);
            _glyph.setLocation(new Location(0, 0));
            turnInfo.add(_glyph.layer);

            // compute the legal placement positions for this tile
            Set<Location> canPlay = Logic.computeLegalPlays(plays, placing);
            if (canPlay.isEmpty()) {
                Log.warning("Pants! Impossible tile! " + placing);
                // TODO: freak out, end the game, something useful?
            }

            // display targets for all legal moves
            for (Location ploc : canPlay) {
                TargetGlyph target = new TargetGlyph(Atlantis.tiles.getTargetTile(), ploc);
                tiles.add(target.layer);
                _targets.add(target);
            }
        }

        public boolean onMouseDown (float vx, float vy) {
            TargetGlyph clicked;
            // if they clicked on the active target tile...
            if (_active != null && _active.hitTest(vx, vy)) {
                // ...rotate the placing glyph upon't
                int cidx = _orients.indexOf(_orient);
                _orient = _orients.get((cidx + 1) % _orients.size());
                _glyph.setOrient(_orient);
                return true;
            }

            // check whether they've clicked a non-active target tile (and activate it)
            if ((clicked = checkHitTarget(vx, vy)) != null) {
                _active = clicked;
                // if this is the first placement, we need to move our placing glyph from the
                // (non-scrolling) turn info layer, to the (scrolling) tiles layer
                if (_glyph.layer.parent() == turnInfo) {
                    turnInfo.remove(_glyph.layer);
                    tiles.add(_glyph.layer);
                }
                // compute the valid orientations for the placing tile at this location
                _orients = Logic.computeLegalOrients(_plays, _placing, _active.loc);
                // TODO: animate!
                _orient = _orients.get(0); // start in the first orientation
                _glyph.setLocation(_active.loc);
                _glyph.setOrient(_orient);

                return true;
            }

            return false;
        }

        public void clear () {
            for (TargetGlyph target : _targets) {
                target.layer.destroy();
            }
            _targets.clear();
            _active = null;
            _placing = null;
            if (_glyph != null) {
                _glyph.layer.destroy();
                _glyph = null;
            }
        }

        protected TargetGlyph checkHitTarget (float x, float y) {
            for (TargetGlyph target : _targets) {
                if (target.hitTest(x, y)) {
                    return target;
                }
            }
            return null;
        }

        protected GameTile _placing;
        protected Placements _plays;
        protected PlayGlyph _glyph;
        protected Orient _orient;
        protected List<Orient> _orients;
        protected List<TargetGlyph> _targets = Lists.newArrayList();
        protected TargetGlyph _active;
    }

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
