//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Pointer;
import static forplay.core.ForPlay.*;

import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import com.threerings.atlantis.shared.Feature;
import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Log;
import com.threerings.atlantis.shared.Logic;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Piecen;
import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Placements;
import static com.threerings.atlantis.client.AtlantisClient.*;

/**
 * Manages the layer that displays the game board.
 */
public class Board
{
    /** The layer that contains the tiles. */
    public final GroupLayer tiles = graphics().createGroupLayer();

    /** Whether or not feature debugging info should be rendered. */
    public final boolean FEATURE_DEBUG = false;

    /**
     * Loads up our resources and performs other one-time initialization tasks.
     */
    public void init (GameController ctrl) {
        _ctrl = ctrl;

        // wire up a dragger that is triggered for presses that don't touch anything else
        Rectangle sbounds = new Rectangle(0, 0, graphics().width(), graphics().height());
        Atlantis.input.register(sbounds, new Pointer.Listener() {
            @Override public void onPointerStart (float x, float y) {
                _drag = new Point(x, y);
            }
            @Override public void onPointerDrag (float x, float y) {
                if (_drag != null) {
                    tiles.setTranslation(tiles.transform().tx() + (x - _drag.x),
                                         tiles.transform().ty() + (y - _drag.y));
                    _drag.set(x, y);
                }
            }
            @Override public void onPointerEnd (float x, float y) {
                _drag = null;
            }
            protected Point _drag;
        });

        // translate (0,0) to the center of the screen
        _origin = sbounds.getCenter();
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
        Glyphs.Play glyph = new Glyphs.Play(play);
        tiles.add(glyph.layer);
        _pglyphs.add(glyph);

        if (FEATURE_DEBUG) {
            for (Glyphs.Play pg : _pglyphs) {
                pg.updateFeatureDebug();
            }
        }
    }

    public void setPlacing (Placements plays, GameTile tile, Glyphs.Play glyph) {
        clearPlacing();
        _placer = new Placer(plays, tile, glyph);
    }

    protected void clearPlacing () {
        if (_placer != null) {
            _placer.clear();
            _placer = null;
        }
    }

    protected void zoomInOn (Glyphs.Target target) {
        // save our current translation
        _savedTrans = new Point(tiles.transform().tx(), tiles.transform().ty());

        float scale = 5f;
        Atlantis.anim.tweenScale(tiles).in(1000f).easeInOut().to(scale);
        float x = target.loc.x * Media.TERRAIN_WIDTH, y = target.loc.y * Media.TERRAIN_HEIGHT;
        Atlantis.anim.tweenXY(tiles).in(1000f).easeInOut().to(
            _origin.x - scale * x, _origin.y - scale * y);
    }

    protected void restoreZoom () {
        if (_savedTrans != null) {
            Atlantis.anim.tweenScale(tiles).in(1000f).easeInOut().to(1f);
            Atlantis.anim.tweenXY(tiles).in(1000f).easeInOut().to(_savedTrans.x, _savedTrans.y);
            _savedTrans = null;
        }
    }

    /** Handles the interaction of placing a new tile on the board. */
    protected class Placer {
        public Placer (Placements plays, GameTile placing, Glyphs.Play glyph) {
            _placing = placing;
            _plays = plays;
            _glyph = glyph;

            // compute the legal placement positions for this tile
            Set<Location> canPlay = Logic.computeLegalPlays(plays, placing);
            if (canPlay.isEmpty()) {
                Log.warning("Pants! Impossible tile! " + placing);
                // TODO: freak out, end the game, something useful?
            }

            // display targets for all legal moves
            Rectangle tbounds = new Rectangle(Media.TERRAIN_SIZE);
            for (Location ploc : canPlay) {
                final Glyphs.Target target =
                    new Glyphs.Target(Atlantis.media.getTargetTile(), ploc);
                tiles.add(target.layer);
                _targets.add(target);

                Atlantis.input.register(target.layer, tbounds, new Input.Action() {
                    public void onTrigger () {
                        activateTarget(target);
                    }
                });
            }
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

            if (_piecens != null) {
                _piecens.destroy(); // destroys all children
                _piecens = null;
            }
        }

        protected void activateTarget (Glyphs.Target target) {
            // when a target becomes active, we hide it because the placing tile will be moved into
            // the place previously occupied by the target
            if (_active != null) {
                _active.layer.setVisible(true);
            }
            _active = target;
            _active.layer.setVisible(false);

            int mypidx = 0; // TODO

            // if we were zoomed in and they clicked somewhere else, zoom back out
            restoreZoom();

            // if this is the first placement, we need to...
            if (_ctrls == null) {
                // ...move our placing glyph from the (non-scrolling) turn info layer, to the
                // (scrolling) tiles layer
                GroupLayer scores = _glyph.layer.parent();
                scores.remove(_glyph.layer);
                tiles.add(_glyph.layer);
                // we also need to update its position so that it can be animated into place
                _glyph.layer.transform().translate(
                    scores.transform().tx() - tiles.transform().tx(),
                    scores.transform().ty() - tiles.transform().ty());

                // ...create our controls UI and icons
                _ctrls = new Glyphs.Tile();
                float quadw = Media.TERRAIN_WIDTH/2, quadh = Media.TERRAIN_HEIGHT/2;
                _ctrls.layer.add(_rotate = Atlantis.media.getActionTile(Media.ROTATE_ACTION));
                _rotate.setTranslation(quadw/2, quadh/2);
                _ctrls.layer.add(_commit = Atlantis.media.getActionTile(Media.OK_ACTION));
                _commit.setTranslation(3*quadw/2, 3*quadh/2);
                _ctrls.layer.add(_placep = Atlantis.media.getPiecenTile(mypidx));
                _placep.setTranslation(3*quadw/2, 3*quadh/2);
                _placep.setAlpha(0.5f);
                tiles.add(_ctrls.layer);

                // create our piecen targets as well
                _piecens = graphics().createGroupLayer();
                Rectangle pbounds = new Rectangle(Media.PIECEN_SIZE);
                for (int fidx = 0; fidx < _placing.terrain.features.length; fidx++) {
                    Feature f = _placing.terrain.features[fidx];
                    ImageLayer pimg = Atlantis.media.getPiecenTile(mypidx);
                    pimg.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
                    _piecens.add(pimg);

                    final Piecen p = new Piecen(Piecen.Color.values()[mypidx], _active.loc, fidx);
                    Atlantis.input.register(new Input.LayerReactor(pimg, pbounds) {
                        @Override public boolean hitTest (IPoint p) {
                            return _piecens.visible() && super.hitTest(p);
                        }
                        public void onTrigger () {
                            commitPlacement(p);
                        }
                    });
                }
                _glyph.layer.add(_piecens);

                // create our controls reactors
                IRectangle abounds = new Rectangle(Media.ACTION_SIZE);
                Atlantis.input.register(_rotate, abounds, new Input.Action() {
                    public void onTrigger () {
                        int cidx = _orients.indexOf(_glyph.getOrient());
                        _glyph.setOrient(_orients.get((cidx + 1) % _orients.size()), true);
                    }
                });

                Atlantis.input.register(_commit, abounds, new Input.Action() {
                    public void onTrigger () {
                        commitPlacement(null); // place with no piecen
                    }
                });

                Atlantis.input.register(_placep, pbounds, new Input.Action() {
                    public void onTrigger () {
                        // hide the "place piecen" control
                        _placep.setVisible(false);
                        // zoom into the to-be-placed tile
                        zoomInOn(_active);
                        // make the piecen buttons visible
                        _piecens.setVisible(true);
                        // enable/disable piecens based on legal placements
                        int idx = 0;
                        for (Feature f : _placing.terrain.features) {
                            int claim = Logic.computeClaim(
                                _plays, _placing, _glyph.getOrient(), _active.loc, f);
                            _piecens.get(idx).setVisible(claim == 0);
                            idx++;
                        }

                        // TODO: don't display commit yet
                        _commit.setVisible(true);
                    }
                });
            }

            // hide the controls, we'll show them again when the location animation has completed
            _ctrls.layer.setVisible(false);
            _piecens.setVisible(false);

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
        }

        protected void commitPlacement (Piecen piecen) {
            _ctrl.place(new Placement(_placing, _glyph.getOrient(), _active.loc, piecen));
            // zoom back out and scroll to our original translation
            restoreZoom();
        }

        protected GameTile _placing;
        protected Placements _plays;
        protected Glyphs.Play _glyph;
        protected List<Orient> _orients;
        protected List<Glyphs.Target> _targets = new ArrayList<Glyphs.Target>();
        protected Glyphs.Target _active;
        protected Glyphs.Tile _ctrls;
        protected GroupLayer _piecens;
        protected ImageLayer _rotate, _placep, _commit;
    }

    protected GameController _ctrl;
    protected Point _origin;
    protected Placer _placer;
    protected Point _savedTrans;
    protected List<Glyphs.Play> _pglyphs = new ArrayList<Glyphs.Play>();

    /** Computes the quadrant occupied by the supplied point (which must be in the supplied
     * rectangle's bounds): up-left=0, up-right=1, low-left=2, low-right=3. */
    protected static int quad (IRectangle rect, float x, float y) {
        int quad = 0;
        quad += (x - rect.getX() < rect.getWidth()/2) ? 0 : 1;
        quad += (y - rect.getY() < rect.getHeight()/2) ? 0 : 2;
        return quad;
    }
}
