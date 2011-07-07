//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import forplay.core.Asserts;
import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Layer;
import forplay.core.Mouse;
import static forplay.core.ForPlay.*;

import pythagoras.f.AffineTransform;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Points;
import pythagoras.f.Rectangle;
import pythagoras.f.Transforms;

import com.threerings.atlantis.shared.Feature;
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

    @Override // from interface Mouse.Listener
    public void onMouseDown (float x, float y, int button) {
        Point p = new Point(x, y);
        // see if any of our reactors consume this click
        for (Reactor r : _reactors) {
            if (r.hitTest(p)) {
                r.onClick();
                return;
            }
        }
        // otherwise, let the click start a drag
        _drag = p;
    }

    @Override // from interface Mouse.Listener
    public void onMouseMove (float x, float y) {
        _current.set(x, y);
        if (_drag != null) {
            tiles.setTranslation(tiles.transform().tx() + (x - _drag.x),
                                 tiles.transform().ty() + (y - _drag.y));
            _drag.set(x, y);
        }
    }

    @Override // from interface Mouse.Listener
    public void onMouseUp (float x, float y, int button) {
        _drag = null;
    }

    @Override // from interface Mouse.Listener
    public void onMouseWheelScroll (float velocity) {
        // nada
    }

    protected void clearPlacing () {
        if (_placer != null) {
            _placer.clear();
            _placer = null;
        }

        // clear out our reactors
        _reactors.clear();
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
                final Glyphs.Target target =
                    new Glyphs.Target(Atlantis.media.getTargetTile(), ploc);
                tiles.add(target.layer);
                _targets.add(target);

                _reactors.add(new LayerReactor(target.layer, new Rectangle(Media.TERRAIN_SIZE)) {
                    public void onClick () {
                        activateTarget(target);
                    }
                    public String toString () {
                        return "target:" + target.loc;
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
                turnInfo.remove(_glyph.layer);
                tiles.add(_glyph.layer);
                // we also need to update its position so that it can be animated into place
                _glyph.layer.transform().translate(
                    turnInfo.transform().tx() - tiles.transform().tx(),
                    turnInfo.transform().ty() - tiles.transform().ty());

                // ...create our controls UI and icons
                _ctrls = new Glyphs.Tile();
                float quadw = Media.TERRAIN_WIDTH/2, quadh = Media.TERRAIN_HEIGHT/2;
                _ctrls.layer.add(_rotate = Atlantis.media.getActionTile(Media.ROTATE_ACTION));
                _rotate.setTranslation((quadw - Media.ACTION_WIDTH)/2,
                                       (quadh - Media.ACTION_HEIGHT)/2);
                _ctrls.layer.add(_commit = Atlantis.media.getActionTile(Media.OK_ACTION));
                _commit.setTranslation(quadw + (quadw - Media.ACTION_WIDTH)/2,
                                       quadh + (quadh - Media.ACTION_HEIGHT)/2);
                _ctrls.layer.add(_placep = Atlantis.media.getPiecenTile(mypidx));
                _placep.setTranslation(quadw + (quadw - Media.PIECEN_WIDTH)/2,
                                       quadh + (quadh - Media.PIECEN_HEIGHT)/2);
                _placep.setAlpha(0.5f);
                tiles.add(_ctrls.layer);

                // create our piecen targets as well
                _piecens = graphics().createGroupLayer();
                Rectangle pbounds = new Rectangle(Media.PIECEN_SIZE);
                for (Feature f : _placing.features()) {
                    ImageLayer pimg = Atlantis.media.getPiecenTile(mypidx);
                    pimg.setOrigin(Media.PIECEN_WIDTH/2, Media.PIECEN_HEIGHT/2);
                    pimg.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
                    _piecens.add(pimg);

                    _reactors.add(new LayerReactor(pimg, pbounds) {
                        @Override public boolean hitTest (IPoint p) {
                            return _piecens.visible() && super.hitTest(p);
                        }
                        public void onClick () {
                            System.out.println("TODO: piecen click");
                        }
                    });
                }
                _glyph.layer.add(_piecens);

                // create our controls reactors
                IRectangle abounds = new Rectangle(Media.ACTION_SIZE);
                _reactors.add(new LayerReactor(_rotate, abounds) {
                    public void onClick () {
                        int cidx = _orients.indexOf(_glyph.getOrient());
                        _glyph.setOrient(_orients.get((cidx + 1) % _orients.size()), true);
                    }
                    public String toString () {
                        return "rotate";
                    }
                });

                _reactors.add(new LayerReactor(_commit, abounds) {
                    public void onClick () {
                        // TODO: confirm placement first
                        _ctrl.place(new Placement(_placing, _glyph.getOrient(), _active.loc));
                        // zoom back out and scroll to our original translation
                        restoreZoom();
                    }
                    public String toString () {
                        return "commit";
                    }
                });

                _reactors.add(new LayerReactor(_placep, pbounds) {
                    public void onClick () {
                        // hide the "place piecen" control
                        _placep.setVisible(false);
                        // zoom into the to-be-placed tile
                        zoomInOn(_active);
                        // make the piecen buttons visible
                        _piecens.setVisible(true);
                        // TODO: enable/disable based on legal placements

                        // TODO: don't display commit yet
                        _commit.setVisible(true);
                    }
                    public String toString () {
                        return "place_piecen";
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

    /** A shape on the view that reacts to clicks. */
    protected abstract class Reactor {
        public Reactor (IRectangle bounds) {
            _bounds = bounds;
        }

        public boolean hitTest (IPoint p) {
            return _bounds.contains(p);
        }

        public abstract void onClick ();

        protected final IRectangle _bounds;
    };

    protected abstract class LayerReactor extends Reactor {
        public LayerReactor (Layer layer, IRectangle bounds) {
            super(bounds);
            _layer = layer;
        }

        public boolean hitTest (IPoint p) {
            // if the layer isn't in the scene graph, the code is broken
            Asserts.check(_layer.parent() != null);

            // require that the layer be visible, and account for rotation of the layer in question
            // before hit testing
            if (!_layer.visible()) return false;

            // compute the transform from screen coordinates to this layer's coordinates and then
            // check that the point falls in the (layer transform relative) bounds
            Point tp = inverseTransform(_layer, p, new Point());
            return super.hitTest(tp);
        }

        protected Layer _layer;
    }

    protected GameController _ctrl;
    protected Point _origin = new Point();
    protected Point _current = new Point(), _drag;
    protected Placer _placer;
    protected Point _savedTrans;
    protected List<Reactor> _reactors = new ArrayList<Reactor>();

    /** Computes the quadrant occupied by the supplied point (which must be in the supplied
     * rectangle's bounds): up-left=0, up-right=1, low-left=2, low-right=3. */
    protected static int quad (IRectangle rect, float x, float y) {
        int quad = 0;
        quad += (x - rect.getX() < rect.getWidth()/2) ? 0 : 1;
        quad += (y - rect.getY() < rect.getHeight()/2) ? 0 : 2;
        return quad;
    }

    protected static Point inverseTransform (Layer layer, IPoint point, Point into) {
        Layer parent = layer.parent();
        IPoint cur = (parent == null) ? point : inverseTransform(parent, point, into);
        forplay.core.Transform lt = layer.transform();
        _scratch.setTransform(lt.m00(), lt.m01(), lt.m10(), lt.m11(), lt.tx(), lt.ty());
        into = _scratch.inverseTransform(cur, into);
        into.x += layer.originX();
        into.y += layer.originY();
        return into;
    }
    protected static AffineTransform _scratch = new AffineTransform();
}
