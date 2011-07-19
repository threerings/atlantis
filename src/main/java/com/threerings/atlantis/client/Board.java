//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import forplay.core.Font;
import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import forplay.core.Pointer;
import forplay.core.SurfaceLayer;
import forplay.core.TextFormat;
import static forplay.core.ForPlay.*;

import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import com.threerings.nexus.distrib.DCustom;
import com.threerings.nexus.distrib.DMap;
import com.threerings.nexus.distrib.DSet;
import com.threerings.nexus.distrib.DValue;

import com.threerings.atlantis.client.util.TextGlyph;
import com.threerings.atlantis.shared.Feature;
import com.threerings.atlantis.shared.GameObject;
import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Log;
import com.threerings.atlantis.shared.Logic;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Piecen;
import com.threerings.atlantis.shared.Placement;
import static com.threerings.atlantis.client.AtlantisClient.*;

/**
 * Manages the layer that displays the game board.
 */
public class Board
{
    /** Displays current scores, etc. */
    public final Scoreboard scores = new Scoreboard();

    /** The layer that contains the tiles. */
    public final GroupLayer tiles = graphics().createGroupLayer();

    /** A layer used when flying a tile from the score display into position on the board. We keep
     * this layer translated the same as the tiles layer so that we can reuse the tile movement
     * logic, but this layer renders above the score layer rather than below (like tiles). */
    public final GroupLayer flight = graphics().createGroupLayer();

    /** Whether or not feature debugging info should be rendered. */
    public final boolean FEATURE_DEBUG = false;

    public Board () {
        // create a background layer that will tile a pattern
        float width = graphics().width(), height = graphics().height();
        SurfaceLayer bground = graphics().createSurfaceLayer((int)width, (int)height);
        bground.surface().setFillPattern(graphics().createPattern(Atlantis.media.getTableImage()));
        bground.surface().fillRect(0, 0, width, height);

        // set the z-order of our layers appropriately
        bground.setDepth(-1);
        tiles.setDepth(0);
        scores.layer.setDepth(+1);
        flight.setDepth(+2);

        // add everything to the root layer
        graphics().rootLayer().add(bground);
        graphics().rootLayer().add(tiles);
        graphics().rootLayer().add(scores.layer);
        graphics().rootLayer().add(flight);

        // TEMP: draw a grid over the board for debugging
        SurfaceLayer grid = graphics().createSurfaceLayer((int)width, (int)height);
        grid.surface().drawLine(0f, height/2, width, height/2, 1f);
        grid.surface().drawLine(width/2, 0f, width/2, height, 1f);
        grid.setDepth(+3);
        graphics().rootLayer().add(grid);
    }

    /**
     * Loads up our resources and performs other one-time initialization tasks.
     */
    public void init (GameController ctrl, GameObject gobj) {
        _ctrl = ctrl;
        _gobj = gobj;

        // when the turn-holder changes, update all of the other bits
        _gobj.turnHolder.addListener(new DValue.Listener<Integer>() {
            public void valueChanged (Integer turnHolder, Integer oldTurnHolder) {
                GameTile placing = _gobj.placing.get();
                if (placing != null) {
                    Glyphs.Play pglyph = new Glyphs.Play(placing);
                    setPlacing(placing, pglyph);
                    scores.setNextTile(pglyph);
                }
                scores.setTurnInfo(_gobj.turnHolder.get(), _gobj.tilesRemaining.get());
                // TODO: enable or disable interactivity based on whether this client controls the
                // turn holder
            }
        });

        // listen for piecen count and score changes
        _gobj.piecens.addListener(new DSet.Listener<Piecen>() {
            public void elementAdded (Piecen piecen) {
                scores.setPiecenCount(piecen.ownerIdx, _gobj.piecensAvailable(piecen.ownerIdx));
            }
            public void elementRemoved (Piecen piecen) {
                scores.setPiecenCount(piecen.ownerIdx, _gobj.piecensAvailable(piecen.ownerIdx));
            }
        });
        _gobj.scores.addListener(new DMap.PutListener<Integer,Integer>() {
            public void entryPut (Integer pidx, Integer score, Integer oscore) {
                scores.setScore(pidx, score);
            }
        });

        // listen for score notifications
        _gobj.scoreEvent.addListener(new DCustom.Listener<GameObject.ScoreEvent>() {
            public void onEvent (GameObject.ScoreEvent event) {
                showPiecenScoreAnimation(event.piecen, event.score);
            }
        });

        // wire up a dragger that is triggered for presses that don't touch anything else
        Rectangle sbounds = new Rectangle(0, 0, graphics().width(), graphics().height());
        Atlantis.input.register(sbounds, new Pointer.Listener() {
            @Override public void onPointerStart (float x, float y) {
                _drag = new Point(x, y);
            }
            @Override public void onPointerDrag (float x, float y) {
                if (_drag != null) {
                    float ntx = tiles.transform().tx() + (x - _drag.x);
                    float nty = tiles.transform().ty() + (y - _drag.y);
                    tiles.setTranslation(ntx, nty);
                    flight.setTranslation(ntx, nty);
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

        for (int ii = 0; ii < _gobj.players.length; ii++) {
            scores.setPiecenCount(ii, _gobj.piecensAvailable(ii));
            scores.setScore(ii, _gobj.getScore(ii));
        }
    }

    public void addPlacement (Placement play) {
        // TODO: if this client didn't originate this play, we need to animate it going from the
        // scoreboard to the correct position on the board
        clearPlacing();

        Glyphs.Play glyph = new Glyphs.Play(play);
        tiles.add(glyph.layer);
        _pglyphs.put(play.loc, glyph);

        // delay subsequent animations until our placement animation has completed; generally there
        // are scoring and piecen removal animations queued up, but we want to complete our zooming
        // out and the sliding of this tile from the scoreboard onto the board before we allow
        // those other animations to proceed
        Atlantis.anim.addBarrier();

        if (FEATURE_DEBUG) {
            for (Glyphs.Play pg : _pglyphs.values()) {
                pg.updateFeatureDebug(_ctrl.logic);
            }
        }
    }

    public void addPiecen (Piecen p) {
        _pglyphs.get(p.loc).setPiecen(p);
    }

    public void clearPiecen (final Piecen p) {
        // queue this up as an instant animation so that we honor barriers
        Atlantis.anim.action(new Runnable() {
            public void run () {
                _pglyphs.get(p.loc).clearPiecen();
            }
        });
    }

    public void setPlacing (GameTile tile, Glyphs.Play glyph) {
        clearPlacing();
        _placer = new Placer(tile, glyph);
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

    protected void showPiecenScoreAnimation (Piecen p, int score) {
        // find the tile that contains the piecen in question
        Glyphs.Play pglyph = _pglyphs.get(p.loc);

        // create a score glyph that we'll animate to alert the player of the score
        final TextGlyph sglyph = TextGlyph.forText(
            ""+score, SCORE_FORMAT.withTextColor(Media.PIECEN_COLORS[p.ownerIdx]));
        float swidth = sglyph.layer.canvas().width(), sheight = sglyph.layer.canvas().height();
        sglyph.layer.setOrigin(swidth/2f, sheight);
        sglyph.layer.setDepth(1);

        // position it at the piecen to start and float it up and fade it out
        Feature f = pglyph.tile.terrain.features[p.featureIdx];
        Point start = Input.layerToParent(pglyph.layer, tiles, f.piecenSpot, new Point());
        Atlantis.anim.add(tiles, sglyph.layer);
        Atlantis.anim.tweenXY(sglyph.layer).in(2000f).easeOut().from(start.x, start.y).
            to(start.x, start.y-sheight);
        Atlantis.anim.tweenAlpha(sglyph.layer).in(2000f).easeOut().from(1f).to(0f).
            then().destroy(sglyph.layer);

        // delay subsequent score (or other) animations for one second so that the player can
        // notice and mentally process this scoring
        Atlantis.anim.addBarrier(1000f);
    }

    /** Handles the interaction of placing a new tile on the board. */
    protected class Placer {
        public Placer (GameTile placing, Glyphs.Play glyph) {
            _placing = placing;
            _glyph = glyph;

            // compute the legal placements for this tile and display targets for all legal moves
            // (the server will have ensured that the tile has at least one legal play)
            Rectangle tbounds = new Rectangle(Media.TERRAIN_SIZE);
            for (Location ploc : _ctrl.logic.computeLegalPlays(placing)) {
                final Glyphs.Target target =
                    new Glyphs.Target(Atlantis.media.getTargetTile(), ploc);
                target.layer.setDepth(-1); // targets render below tiles
                tiles.add(target.layer);
                target.layer.setAlpha(0);
                Atlantis.anim.tweenAlpha(target.layer).easeOut().to(0.75f).in(500f);
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
                GroupLayer scores = _glyph.layer.parent();
                scores.remove(_glyph.layer);
                _glyph.layer.setDepth(+1); // placing goes above other tiles
                flight.add(_glyph.layer);
                // we also need to update its position so that it can be animated into place
                _glyph.layer.transform().translate(
                    scores.transform().tx() - tiles.transform().tx(),
                    scores.transform().ty() - tiles.transform().ty());

                // ...create our controls UI and icons
                _ctrls = new Glyphs.Tile();
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
                    pimg.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
                    _piecens.add(pimg);

                    Atlantis.input.register(new Input.LayerReactor(pimg, pbounds) {
                        @Override public boolean hitTest (IPoint p) {
                            return _piecens.visible() && super.hitTest(p);
                        }
                        public void onTrigger () {
                            commitPlacement(f);
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

                Atlantis.input.register(_nopiecen, abounds, new Input.Action() {
                    public void onTrigger () {
                        commitPlacement(null); // place with no piecen
                    }
                });

                Atlantis.input.register(_cancel, abounds, new Input.Action() {
                    public void onTrigger () {
                        restoreZoom();          // zoom back out
                        showConsiderControls(); // and return to "consider" controls
                    }
                });

                Atlantis.input.register(_placep, pbounds, new Input.Action() {
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
            _orients = _ctrl.logic.computeLegalOrients(_placing, _active.loc);
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
                int claim = _ctrl.logic.computeClaim(_placing, _glyph.getOrient(), _active.loc, f);
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
            _ctrl.place(play, piecen);
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

    protected GameController _ctrl;
    protected GameObject _gobj;
    protected Point _origin;
    protected Placer _placer;
    protected Point _savedTrans;
    protected Map<Location, Glyphs.Play> _pglyphs = Maps.newHashMap();

    protected final TextFormat SCORE_FORMAT = new TextFormat().
        withFont(graphics().createFont("Helvetica", Font.Style.BOLD, 24)).
        withEffect(TextFormat.Effect.outline(0xFF000000));
}
