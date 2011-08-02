//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import forplay.core.ImageLayer;
import forplay.core.Asserts;
import forplay.core.GroupLayer;
import static forplay.core.ForPlay.*;

import pythagoras.f.FloatMath;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import com.threerings.anim.Animation;
import com.threerings.anim.Animator;

import atlantis.shared.Feature;
import atlantis.shared.GameTile;
import atlantis.shared.Location;
import atlantis.shared.Logic;
import atlantis.shared.Orient;
import atlantis.shared.Piecen;
import atlantis.shared.Placement;

/**
 * Various glyphs that we render on the board.
 */
public class Glyphs
{
    /** Displays a tile-sized graphic. */
    public static class Tile {
        public final GroupLayer layer;

        /** The bounds of this tile, in the layer's coordinate system (i.e. origin is reflected in
         * x, y but not translation. */
        public final IRectangle bounds;

        public Orient getOrient () {
            return _orient;
        }

        public void setOrient (Orient orient, boolean animate) {
            if (animate) {
                float toOrient;
                Animation.One rotA = _anim.tweenRotation(layer).easeInOut().
                    to(toOrient = orient.rotation()).in(1000);
                // if we're going from east (pi/2) to south (-pi) wrap the other way and go to pi
                // to avoid needlessly going the long way round
                if (_orient == Orient.EAST && orient == Orient.SOUTH) {
                    rotA.to(toOrient = FloatMath.PI);
                }
                // TEMP: if we're not in the middle of another animation, set our from angle to
                // avoid funny business that occurs when we extract our current angle from the
                // transform matrix
                if (_rotA == null || !_rotA.cancel()) {
                    rotA.from(_orient.rotation());
                }
                _rotA = rotA;
            } else {
                if (_rotA != null) _rotA.cancel();
                layer.transform().setRotation(orient.rotation());
            }
            _orient = orient;
        }

        public void setLocation (Location loc, boolean animate, Runnable onComplete) {
            // TODO: disable hit testing while animating
            if (_moveA != null) _moveA.cancel();
            float x = loc.x * Media.TERRAIN_WIDTH, y = loc.y * Media.TERRAIN_HEIGHT;
            if (animate) {
                _moveA = _anim.tweenXY(layer).easeInOut().to(x, y).in(1000);
                if (onComplete != null) {
                    _moveA.then().action(onComplete);
                }
            } else {
                layer.transform().setTranslation(x, y);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }

        protected Tile (Animator anim) {
            _anim = anim;
            layer = graphics().createGroupLayer();
            float w = Media.TERRAIN_WIDTH, h = Media.TERRAIN_HEIGHT;
            layer.setOrigin(w/2, h/2);
            bounds = new Rectangle(-w/2, -h/2, w, h);
        }

        protected Animator _anim;
        protected Animation _rotA, _moveA;
        protected Orient _orient = Orient.NORTH;
    }

    /** Displays a potential-move click-target. */
    public static class Target extends Tile {
        public final Location loc;

        public Target (Animator anim, ImageLayer tile, Location loc) {
            super(anim);
            this.loc = loc;
            layer.add(tile);
            setLocation(loc, false, null);
        }
    }

    /** Displays a played (or pending) tile. */
    public static class Play extends Tile {
        public final GameTile tile;

        public Play (Animator anim, Placement play) {
            this(anim, play.tile);
            _play = play;
            setOrient(play.orient, false);
            setLocation(play.loc, false, null);
        }

        public Play (Animator anim, GameTile tile) {
            super(anim);
            this.tile = tile;
            layer.add(Atlantis.media.getTerrainTile(tile.terrain.tileIdx));
            if (tile.hasShield) {
                ImageLayer shield = Atlantis.media.getShieldTile();
                shield.setTranslation(Media.TERRAIN_WIDTH/8f+2, Media.TERRAIN_WIDTH/8f+2);
                layer.add(shield);
            }
        }

        public void setPiecen (Piecen piecen) {
            Asserts.checkState(_pimg == null, "Play already has piecen image: %s", _play);
            Feature f = _play.getFeature(piecen.featureIdx);
            _pimg = Atlantis.media.getPiecenTile(piecen.ownerIdx);
            _pimg.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
            layer.add(_pimg);
        }

        public void clearPiecen () {
            Asserts.checkState(_pimg != null, "Play has no piecen image: %s", _play);
            _pimg.destroy();
            _pimg = null;
        }

        public void updateFeatureDebug (Logic logic) {
            if (_debug == null) {
                _debug = graphics().createGroupLayer();
                layer.add(_debug);
            } else {
                _debug.clear();
            }
            for (Feature f : _play.tile.terrain.features) {
                int group = logic.getClaim(_play).getClaimGroup(f);
                if (group > 0) {
                    ImageLayer img = Atlantis.media.getPiecenTile(group % Media.PIECEN_COUNT);
                    img.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
                    img.setAlpha(0.5f);
                    _debug.add(img);
                }
            }
        }

        protected Placement _play;
        protected ImageLayer _pimg;
        protected GroupLayer _debug;
    }
}
