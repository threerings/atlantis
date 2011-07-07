//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Mouse;
import forplay.core.Transform;
import static forplay.core.ForPlay.*;

import pythagoras.f.AffineTransform;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;

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
        _origin.set(graphics().width()/2, graphics().height()/2);
        _ctrl = ctrl;
        mouse().setListener(this);
        tiles.setTranslation(_origin.x, _origin.y);
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
        tiles.add(new Glyphs.Play(play).layer);
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
        Transform tf = tiles.transform();
        AffineTransform xform = new AffineTransform(
            tf.m00(), tf.m01(), tf.m10(), tf.m11(), tf.tx(), tf.ty());
        Point m = new Point(x, y);
        xform.inverseTransform(m, m);
        // float vx = x - tiles.transform().tx(), vy = y - tiles.transform().ty();

        // if the click isn't consumed by tile placement, let it start a drag
        if (_placer == null || !_placer.onMouseDown(m.x, m.y)) {
            _drag = new Point(x, y);
        }
    }

    protected AffineTransform _xform = new AffineTransform();

    @Override // from interface Mouse.Listener
    public void onMouseMove (float x, float y) {
        _current.set(x, y);
        if (_drag != null) {
            tiles.setTranslation(tiles.transform().tx() + (x - _drag.x),
                                 tiles.transform().ty() + (y - _drag.y));
            _drag.set(x, y);
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
    protected Point _origin = new Point();
    protected Point _current = new Point(), _drag;
    protected Placer _placer;

    /** The x and y coordinate of the tile over which the mouse is hovering. */
    protected int _hoverX, _hoverY;

    /** Computes the quadrant occupied by the supplied point (which must be in the supplied
     * rectangle's bounds): up-left=0, up-right=1, low-left=2, low-right=3. */
    protected static int quad (IRectangle rect, float x, float y) {
        int quad = 0;
        quad += (x - rect.getX() < rect.getWidth()/2) ? 0 : 1;
        quad += (y - rect.getY() < rect.getHeight()/2) ? 0 : 2;
        return quad;
    }

    /** Handles the interaction of placing a new tile on the board. */
    protected class Placer {
        public Placer (Placements plays, GameTile placing) {
            _placing = placing;
            _plays = plays;
            _glyph = new Glyphs.Play(placing);
            _glyph.setLocation(new Location(1, 1), false, null);
            turnInfo.add(_glyph.layer);

            // compute the legal placement positions for this tile
            Set<Location> canPlay = Logic.computeLegalPlays(plays, placing);
            if (canPlay.isEmpty()) {
                Log.warning("Pants! Impossible tile! " + placing);
                // TODO: freak out, end the game, something useful?
            }

            // display targets for all legal moves
            for (Location ploc : canPlay) {
                Glyphs.Target target = new Glyphs.Target(Atlantis.tiles.getTargetTile(), ploc);
                tiles.add(target.layer);
                _targets.add(target);
            }
        }

        public boolean onMouseDown (float vx, float vy) {
            return checkActiveClick(vx, vy) || checkTargetClick(vx, vy);
        }

        public void clear () {
            for (Glyphs.Target target : _targets) {
                target.layer.destroy();
            }
            _targets.clear();
            _active = null;
            _placing = null;

            if (_glyph != null) {
                _glyph.layer.destroy();
                _glyph = null;
            }

            if (_ctrls != null) {
                _ctrls.layer.destroy(); // destroys all children
                _ctrls = null;
                _rotate = _placep = _commit = null;
            }
        }

        protected boolean checkActiveClick (float vx, float vy) {
            if (_active == null || !_active.hitTest(vx, vy)) return false;

            // if we're in the middle of animating, the controls will not be visible and we should
            // swallow any clicks on the target tile for now
            if (!_ctrls.layer.visible()) return true;

            switch (quad(_active.bounds(), vx, vy)) {
            // if they were in the upper-left quadrant, (possibly) rotate
            case 0:
                // only rotate if we have more than one orientation
                if (_orients.size() > 1) {
                    int cidx = _orients.indexOf(_glyph.getOrient());
                    _glyph.setOrient(_orients.get((cidx + 1) % _orients.size()), true);
                }
                break;

            // if they were in the lower-right quadrant, move to confirm/piecen placement
            case 3:
                if (_commit.visible()) {
                    // TODO: confirm placement first
                    _ctrl.place(new Placement(_placing, _glyph.getOrient(), _active.loc));

                    // zoom back out and scroll to our original translation
                    if (_savedTrans != null) {
                        Atlantis.anim.tweenScale(tiles).in(1000f).easeInOut().to(1f);
                        Atlantis.anim.tweenXY(tiles).in(1000f).easeInOut().to(
                            _savedTrans.x, _savedTrans.y);
                        _savedTrans = null;
                    }

                } else /* if (_placep.visible()) */ {
                    // hide the "place piecen" control
                    _placep.setVisible(false);
                    // store our current translation
                    _savedTrans = new Point(tiles.transform().tx(), tiles.transform().ty());
                    // zoom into the to-be-placed tile
                    float scale = 5f;
                    Atlantis.anim.tweenScale(tiles).in(1000f).easeInOut().to(scale);
                    Atlantis.anim.tweenXY(tiles).in(1000f).easeInOut().to(
                        _origin.x - scale * _active.bounds().getCenterX(),
                        _origin.y - scale * _active.bounds().getCenterY());

                    // TODO: create piecen placement targets
                    _commit.setVisible(true);
                }
                break;
            }
            return true;
        }

        protected boolean checkTargetClick (float vx, float vy) {
            // check whether they've clicked a non-active target tile (and activate it)
            Glyphs.Target clicked;
            if ((clicked = checkHitTarget(vx, vy)) == null) return false;
            _active = clicked;

            // if this is the first placement, we need to...
            if (_ctrls == null) {
                // ...move our placing glyph from the (non-scrolling) turn info layer, to the
                // (scrolling) tiles layer
                turnInfo.remove(_glyph.layer);
                tiles.add(_glyph.layer);
                // we also need to update its position so that it can be animated into place
                _glyph.layer.transform().translate(
                    turnInfo.transform().tx() - tiles.transform().tx(),
                    turnInfo.transform().ty() - tiles.transform().ty());

                // ...create our controls UI and icons
                _ctrls = new Glyphs.Tile();
                float quadw = GameTiles.TERRAIN_WIDTH/2, quadh = GameTiles.TERRAIN_HEIGHT/2;
                _ctrls.layer.add(_rotate = Atlantis.tiles.getActionTile(GameTiles.ROTATE_ACTION));
                _rotate.setTranslation((quadw - GameTiles.ACTION_WIDTH)/2,
                                       (quadh - GameTiles.ACTION_HEIGHT)/2);
                _ctrls.layer.add(_commit = Atlantis.tiles.getActionTile(GameTiles.OK_ACTION));
                _commit.setTranslation(quadw + (quadw - GameTiles.ACTION_WIDTH)/2,
                                       quadh + (quadh - GameTiles.ACTION_HEIGHT)/2);
                _ctrls.layer.add(_placep = Atlantis.tiles.getPiecenTile(0)); // TODO: use pidx
                _placep.setTranslation(quadw + (quadw - GameTiles.PIECEN_WIDTH)/2,
                                       quadh + (quadh - GameTiles.PIECEN_HEIGHT)/2);
                _placep.setAlpha(0.5f);
                tiles.add(_ctrls.layer);
            }

            // hide the controls, we'll show them again when the location animation has completed
            _ctrls.layer.setVisible(false);

            // compute the valid orientations for the placing tile at this location
            _orients = Logic.computeLegalOrients(_plays, _placing, _active.loc);
            // if whatever orient we happen to be at is not valid...
            if (!_orients.contains(_glyph.getOrient())) {
                // ...start in the first orientation
                _glyph.setOrient(_orients.get(0), true);
            }
            _glyph.setLocation(_active.loc, true, new Runnable() {
                public void run () {
                    _ctrls.layer.setVisible(true);
                    _placep.setVisible(true);
                }
            });

            // update the controls, and move them to this location
            _ctrls.setLocation(_active.loc, false, null);

            boolean canRotate = (_orients.size() > 1);
            _rotate.setVisible(canRotate);

            boolean havePiecens = true; // TODO: use real data
            _placep.setVisible(false); // havePiecens);
            _commit.setVisible(!havePiecens);

            return true;
        }

        protected Glyphs.Target checkHitTarget (float x, float y) {
            for (Glyphs.Target target : _targets) {
                if (target.hitTest(x, y)) {
                    return target;
                }
            }
            return null;
        }

        protected GameTile _placing;
        protected Placements _plays;
        protected Glyphs.Play _glyph;
        protected List<Orient> _orients;
        protected List<Glyphs.Target> _targets = new ArrayList<Glyphs.Target>();
        protected Glyphs.Target _active;
        protected Glyphs.Tile _ctrls;
        protected ImageLayer _rotate, _placep, _commit;
        protected Point _savedTrans;
    }
}
