//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.Pointer;
import playn.core.SurfaceLayer;
import static playn.core.PlayN.*;

import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import react.Slot;

import tripleplay.util.PointerInput;

import com.threerings.nexus.distrib.DValue;

import atlantis.client.util.TextGlyph;
import atlantis.shared.Feature;
import atlantis.shared.GameObject;
import atlantis.shared.GameTile;
import atlantis.shared.Location;
import atlantis.shared.Orient;
import atlantis.shared.Piecen;
import atlantis.shared.Placement;

/**
 * Manages the layer that displays the game board.
 */
public class Board
{
    /** The layer that contains the tiles. */
    public final GroupLayer tiles = graphics().createGroupLayer();

    /** A layer used when flying a tile from the score display into position on the board. We keep
     * this layer translated the same as the tiles layer so that we can reuse the tile movement
     * logic, but this layer renders above the score layer rather than below (like tiles). */
    public final GroupLayer flight = graphics().createGroupLayer();

    /** Whether or not feature debugging info should be rendered. */
    public final boolean FEATURE_DEBUG = false;

    public Board (GameScreen screen) {
        _screen = screen;

        // create a background layer that will tile a pattern
        float width = graphics().width(), height = graphics().height();
        SurfaceLayer bground = graphics().createSurfaceLayer((int)width, (int)height);
        bground.surface().setFillPattern(graphics().createPattern(Atlantis.media.getTableImage()));
        bground.surface().fillRect(0, 0, width, height);

        // set the z-order of our layers appropriately
        bground.setDepth(-1);
        tiles.setDepth(0);
        // Scoreboard.layer is at +1
        flight.setDepth(+2);

        // add everything to the screen layer
        screen.layer.add(bground);
        screen.layer.add(tiles);
        screen.layer.add(flight);

        // // TEMP: draw a grid over the board for debugging
        // SurfaceLayer grid = graphics().createSurfaceLayer((int)width, (int)height);
        // grid.surface().drawLine(0f, height/2, width, height/2, 1f);
        // grid.surface().drawLine(width/2, 0f, width/2, height, 1f);
        // grid.setDepth(+3);
        // screen.layer.add(grid);
    }

    /**
     * Loads up our resources and performs other one-time initialization tasks.
     */
    public void init (GameObject gobj) {
        _gobj = gobj;

        // when the turn-holder changes, update all of the other bits
        _gobj.turnHolder.connect(new DValue.Listener<Integer>() {
            @Override public void onChange (Integer turnHolder) {
                GameTile placing = _gobj.placing.get();
                if (placing != null) {
                    Glyphs.Play pglyph = new Glyphs.Play(_screen.anim, placing);
                    setPlacing(placing, pglyph);
                    _screen.scores.setNextTile(pglyph);
                }
            }
        });

        // listen for score notifications
        _gobj.scoreSignal.connect(new Slot<GameObject.Score>() {
            public void onEmit (GameObject.Score event) {
                showPiecenScoreAnimation(event.piecen, event.score);
            }
        });

        // wire up a dragger that is triggered for presses that don't touch anything else
        _screen.input.register(new Pointer.Listener() {
            @Override public void onPointerStart (Pointer.Event event) {
                _drag = new Point(event.x(), event.y());
            }
            @Override public void onPointerDrag (Pointer.Event event) {
                if (_drag != null) {
                    float x = event.x(), y = event.y();
                    float ntx = tiles.transform().tx() + (x - _drag.x);
                    float nty = tiles.transform().ty() + (y - _drag.y);
                    tiles.setTranslation(ntx, nty);
                    flight.setTranslation(ntx, nty);
                    _drag.set(x, y);
                }
            }
            @Override public void onPointerEnd (Pointer.Event event) {
                _drag = null;
            }
            protected Point _drag;
        });

        // translate (0,0) to the center of the screen
        _origin = new Point(graphics().width()/2, graphics().height()/2);
        tiles.setTranslation(_origin.x, _origin.y);
        flight.setTranslation(_origin.x, _origin.y);
    }

    /**
     * Resets the board, and prepares for a new game.
     */
    public void reset () {
        // TODO: clear out previously placed tiles
        clearPlacing();

        for (Placement play : _gobj.plays) {
            addPlacement(play);
        }
        for (Piecen p : _gobj.piecens) {
            addPiecen(p);
        }
    }

    public void addPlacement (Placement play) {
        // TODO: if this client didn't originate this play, we need to animate it going from the
        // scoreboard to the correct position on the board
        clearPlacing();

        Glyphs.Play glyph = new Glyphs.Play(_screen.anim, play);
        tiles.add(glyph.layer);
        _pglyphs.put(play.loc, glyph);

        // delay subsequent animations until our placement animation has completed; generally there
        // are scoring and piecen removal animations queued up, but we want to complete our zooming
        // out and the sliding of this tile from the scoreboard onto the board before we allow
        // those other animations to proceed
        _screen.anim.addBarrier();

        if (FEATURE_DEBUG) {
            for (Glyphs.Play pg : _pglyphs.values()) {
                pg.updateFeatureDebug(_screen.ctrl.logic);
            }
        }
    }

    public void addPiecen (Piecen p) {
        _pglyphs.get(p.loc).setPiecen(p);
    }

    public void clearPiecen (final Piecen p) {
        // queue this up as an instant animation so that we honor barriers
        _screen.anim.action(new Runnable() {
            public void run () {
                _pglyphs.get(p.loc).clearPiecen();
            }
        });
    }

    public void setPlacing (GameTile tile, Glyphs.Play glyph) {
        clearPlacing();
        _placer = new Placer(tile, glyph);
    }

    public void reportWinners (List<String> winners) {
        StringBuffer text = new StringBuffer();
        if (winners.size() == 1) {
            text.append(winners.get(0)).append(" wins!");
        } else {
            text.append("Winners: ");
            for (int ii = 0; ii < winners.size(); ii++) {
                if (ii != 0) text.append(", ");
                text.append(winners.get(ii));
            }
            text.append("!");
        }

        TextGlyph sglyph = TextGlyph.forText(text.toString(), UI.scoreFormat(0xFFFFFFFF)).
            setOriginBottomCenter();
        sglyph.layer.setDepth(1);
        _screen.anim.add(tiles, sglyph.layer);
        _screen.anim.tweenXY(sglyph.layer).in(2000f).easeOut().
            from(0, sglyph.layer.canvas().height()).to(0, -sglyph.layer.canvas().height());
        _screen.anim.tweenAlpha(sglyph.layer).in(2000f).easeOut().from(1f).to(0f).
            then().destroy(sglyph.layer);
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
        _screen.anim.tweenScale(tiles).in(1000f).easeInOut().to(scale);
        float x = target.loc.x * Media.TERRAIN_WIDTH, y = target.loc.y * Media.TERRAIN_HEIGHT;
        _screen.anim.tweenXY(tiles).in(1000f).easeInOut().to(
            _origin.x - scale * x, _origin.y - scale * y);
    }

    protected void restoreZoom () {
        if (_savedTrans != null) {
            _screen.anim.tweenScale(tiles).in(1000f).easeInOut().to(1f);
            _screen.anim.tweenXY(tiles).in(1000f).easeInOut().to(_savedTrans.x, _savedTrans.y);
            _savedTrans = null;
        }
    }

    protected void showPiecenScoreAnimation (Piecen p, int score) {
        // find the tile that contains the piecen in question
        Glyphs.Play pglyph = _pglyphs.get(p.loc);

        // create a score glyph that we'll animate to alert the player of the score
        TextGlyph sglyph = TextGlyph.
            forText(""+score, UI.scoreFormat(Media.PIECEN_COLORS[p.ownerIdx])).
            setOriginBottomCenter();
        sglyph.layer.setDepth(1);

        // position it at the piecen to start and float it up and fade it out
        Feature f = pglyph.tile.terrain.features[p.featureIdx];
        Point start = Layer.Util.layerToParent(pglyph.layer, tiles, f.piecenSpot, new Point());
        _screen.anim.add(tiles, sglyph.layer);
        _screen.anim.tweenXY(sglyph.layer).in(2000f).easeOut().from(start.x, start.y).
            to(start.x, start.y-sglyph.layer.canvas().height());
        _screen.anim.tweenAlpha(sglyph.layer).in(2000f).easeOut().from(1f).to(0f).
            then().destroy(sglyph.layer);

        // delay subsequent score (or other) animations for one second so that the player can
        // notice and mentally process this scoring
        _screen.anim.addBarrier(1000f);
    }

    /** Handles the interaction of placing a new tile on the board. */
    protected class Placer {
        public Placer (GameTile placing, Glyphs.Play glyph) {
            _placing = placing;
            _glyph = glyph;

            // if the current turn holder is controlled by this client, compute the legal
            // placements for this tile and display targets for all legal moves (the server will
            // have ensured that the tile has at least one legal play)
            if (_screen.localIdxs.contains(_gobj.turnHolder.get())) {
                Rectangle tbounds = new Rectangle(Media.TERRAIN_SIZE);
                for (Location ploc : _screen.ctrl.logic.computeLegalPlays(placing)) {
                    final Glyphs.Target target = new Glyphs.Target(
                        _screen.anim, Atlantis.media.getTargetTile(), ploc);
                    target.layer.setDepth(-1); // targets render below tiles
                    tiles.add(target.layer);
                    target.layer.setAlpha(0);
                    _screen.anim.tweenAlpha(target.layer).easeOut().to(0.75f).in(500f);
                    _targets.add(target);

                    _screen.input.register(target.layer, tbounds, new PointerInput.Action() {
                        public void onTrigger () {
                            activateTarget(target);
                        }
                    });
                }
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
                _rotate = _placep = _nopiecen = _cancel = null;
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

            final int mypidx = _gobj.turnHolder.get();

            // if we were zoomed in and they clicked somewhere else, zoom back out
            restoreZoom();

            // if this is the first placement, we need to...
            if (_ctrls == null) {
                // ...move our placing glyph from the (non-scrolling) turn info layer, to the
                // (scrolling) tiles layer
                GroupLayer slayer = _glyph.layer.parent();
                slayer.remove(_glyph.layer);
                _glyph.layer.setDepth(+1); // placing goes above other tiles
                flight.add(_glyph.layer);
                // we also need to update its position so that it can be animated into place
                _glyph.layer.transform().translate(
                    slayer.transform().tx() - tiles.transform().tx(),
                    slayer.transform().ty() - tiles.transform().ty());

                // ...create our controls UI and icons
                _ctrls = new Glyphs.Tile(_screen.anim);
                _ctrls.layer.setDepth(+2); // ctrls go above tiles/placing
                float twidth = Media.TERRAIN_WIDTH, theight = Media.TERRAIN_HEIGHT;
                float awidth = Media.ACTION_WIDTH, aheight = Media.ACTION_HEIGHT;
                _ctrls.layer.add(_rotate = Atlantis.media.getActionTile(Media.ROTATE_ACTION));
                _ctrls.layer.add(_nopiecen = Atlantis.media.getActionTile(Media.NOPIECEN_ACTION));
                _nopiecen.setTranslation(twidth, theight);
                _ctrls.layer.add(_cancel = Atlantis.media.getActionTile(Media.CANCEL_ACTION));
                _cancel.setTranslation(0, theight);
                _ctrls.layer.add(_placep = Atlantis.media.getPiecenTile(mypidx));
                _placep.setTranslation(twidth, theight);
                _placep.setAlpha(0.5f);
                tiles.add(_ctrls.layer);

                // create our piecen targets as well
                _piecens = graphics().createGroupLayer();
                Rectangle pbounds = new Rectangle(Media.PIECEN_SIZE);
                for (final Feature f : _placing.terrain.features) {
                    ImageLayer pimg = Atlantis.media.getPiecenTile(mypidx);
                    pimg.setTranslation(f.piecenSpot.x(), f.piecenSpot.y());
                    _piecens.add(pimg);

                    _screen.input.register(new PointerInput.LayerRegion(pimg, pbounds) {
                        @Override public boolean hitTest (IPoint p) {
                            return _piecens.visible() && super.hitTest(p);
                        }
                    }, new PointerInput.Action() {
                        public void onTrigger () {
                            commitPlacement(f);
                        }
                    });
                }
                _glyph.layer.add(_piecens);

                // create our controls reactors
                IRectangle abounds = new Rectangle(Media.ACTION_SIZE);
                _screen.input.register(_rotate, abounds, new PointerInput.Action() {
                    public void onTrigger () {
                        int cidx = _orients.indexOf(_glyph.getOrient());
                        _glyph.setOrient(_orients.get((cidx + 1) % _orients.size()), true);
                    }
                });

                _screen.input.register(_nopiecen, abounds, new PointerInput.Action() {
                    public void onTrigger () {
                        commitPlacement(null); // place with no piecen
                    }
                });

                _screen.input.register(_cancel, abounds, new PointerInput.Action() {
                    public void onTrigger () {
                        restoreZoom();          // zoom back out
                        showConsiderControls(); // and return to "consider" controls
                    }
                });

                _screen.input.register(_placep, pbounds, new PointerInput.Action() {
                    public void onTrigger () {
                        zoomInOn(_active);    // zoom into the to-be-placed tile
                        showCommitControls(); // put the controls in "commit placement" mode
                    }
                });
            }

            // move the controls to this location, and hide them; we'll show them again when the
            // location animation has completed
            _ctrls.layer.setVisible(false);
            _ctrls.setLocation(_active.loc, false, null);

            // compute the valid orientations for the placing tile at this location
            _orients = _screen.ctrl.logic.computeLegalOrients(_placing, _active.loc);
            // if whatever orient we happen to be at is not valid...
            if (!_orients.contains(_glyph.getOrient())) {
                // ...start in the first orientation
                _glyph.setOrient(_orients.get(0), true);
            }
            _glyph.setLocation(_active.loc, true, new Runnable() {
                public void run () {
                    // if this was our first move, we need to hop from the flight layer to the
                    // tiles layer when we arrive
                    if (_glyph.layer.parent() == flight) {
                        flight.remove(_glyph.layer);
                        tiles.add(_glyph.layer);
                    }
                    // make our controls visible
                    _ctrls.layer.setVisible(true);
                }
            });

            // put the controls in "consider this placement" mode
            showConsiderControls();
        }

        protected void showConsiderControls () {
            _piecens.setVisible(false);
            _cancel.setVisible(false);
            _rotate.setVisible(_orients.size() > 1);
            boolean havePiecens = _gobj.piecensAvailable(_gobj.turnHolder.get()) > 0;
            _placep.setVisible(havePiecens);
            _nopiecen.setVisible(!havePiecens);
        }

        protected void showCommitControls () {
            // make the piecen buttons visible
            _piecens.setVisible(true);
            // hide the "place piecen" control
            _placep.setVisible(false);
            // make the nopiecen and cancel buttons visible
            _nopiecen.setVisible(true);
            _cancel.setVisible(true);
            // enable/disable piecens based on legal placements
            int idx = 0;
            for (Feature f : _placing.terrain.features) {
                int claim = _screen.ctrl.logic.computeClaim(
                    _placing, _glyph.getOrient(), _active.loc, f);
                _piecens.get(idx).setVisible(claim == 0);
                idx++;
            }
        }

        protected void commitPlacement (Feature f) {
            // zoom back out and scroll to our original translation
            restoreZoom();
            // and submit our play to the server
            Placement play = new Placement(_placing, _glyph.getOrient(), _active.loc);
            Piecen piecen = (f == null) ? null : new Piecen(
                _gobj.turnHolder.get(), _active.loc, play.getFeatureIndex(f));
            _screen.ctrl.place(play, piecen);
        }

        protected GameTile _placing;
        protected Glyphs.Play _glyph;
        protected List<Orient> _orients;
        protected List<Glyphs.Target> _targets = Lists.newArrayList();
        protected Glyphs.Target _active;
        protected Glyphs.Tile _ctrls;
        protected GroupLayer _piecens;
        protected ImageLayer _rotate, _placep, _nopiecen, _cancel;
    }

    protected GameScreen _screen;
    protected GameObject _gobj;
    protected Point _origin;
    protected Placer _placer;
    protected Point _savedTrans;
    protected Map<Location, Glyphs.Play> _pglyphs = Maps.newHashMap();
}
