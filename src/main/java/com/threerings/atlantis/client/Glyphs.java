//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import forplay.core.ImageLayer;
import forplay.core.GroupLayer;
import static forplay.core.ForPlay.*;

import pythagoras.f.FloatMath;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import com.threerings.anim.Animation;

import com.threerings.atlantis.shared.Feature;
import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;

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
            float dur = animate ? 1000 : 0;
            float toOrient;
            Animation.One rotA = Atlantis.anim.tweenRotation(layer).easeInOut().
                to(toOrient = orient.rotation()).in(dur);
            // if we're going from east (pi/2) to south (-pi) wrap the other way and go to pi
            // to avoid needlessly going the long way round
            if (_orient == Orient.EAST && orient == Orient.SOUTH) {
                rotA.to(toOrient = FloatMath.PI);
            }
            // TEMP: if we're not in the middle of another animation, set our from angle to avoid
            // funny business that occurs when we extract our current angle from the transform
            // matrix
            if (_rotA == null || !_rotA.cancel()) {
                rotA.from(_orient.rotation());
            }
            _rotA = rotA;
            _orient = orient;
        }

        public void setLocation (Location loc, boolean animate, Runnable onComplete) {
            // TODO: disable hit testing while animating
            if (_moveA != null) {
                _moveA.cancel();
            }
            float dur = animate ? 1000 : 0;
            float x = loc.x * Media.TERRAIN_WIDTH, y = loc.y * Media.TERRAIN_HEIGHT;
            _moveA = Atlantis.anim.tweenXY(layer).easeInOut().to(x, y).in(dur);
            if (onComplete != null) {
                _moveA.then().action(onComplete);
            }
        }

        protected Tile () {
            layer = graphics().createGroupLayer();
            float w = Media.TERRAIN_WIDTH, h = Media.TERRAIN_HEIGHT;
            layer.setOrigin(w/2, h/2);
            bounds = new Rectangle(-w/2, -h/2, w, h);
        }

        protected Animation _rotA, _moveA;
        protected Orient _orient = Orient.NORTH;
    }

    /** Displays a potential-move click-target. */
    public static class Target extends Tile {
        public final Location loc;

        public Target (ImageLayer tile, Location loc) {
            this.loc = loc;
            layer.add(tile);
            setLocation(loc, false, null);
        }
    }

    /** Displays a played (or pending) tile. */
    public static class Play extends Tile {
        public Play (Placement play) {
            this(play.tile);
            _play = play;
            setOrient(play.orient, false);
            setLocation(play.loc, false, null);
            if (play.piecen != null) {
                Feature f = play.tile.terrain.features[play.piecen.featureIdx];
                ImageLayer pimg = Atlantis.media.getPiecenTile(play.piecen.owner.ordinal());
                pimg.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
                layer.add(pimg);
            }
        }

        public Play (GameTile tile) {
            layer.add(Atlantis.media.getTerrainTile(tile.terrain.tileIdx));
            if (tile.hasShield) {
                ImageLayer shield = Atlantis.media.getShieldTile();
                shield.setTranslation(Media.TERRAIN_WIDTH/8f+2, Media.TERRAIN_WIDTH/8f+2);
                layer.add(shield);
            }
        }

        public void updateFeatureDebug () {
            if (_debug == null) {
                _debug = graphics().createGroupLayer();
                layer.add(_debug);
            } else {
                _debug.clear();
            }
            for (Feature f : _play.tile.terrain.features) {
                int group = _play.getClaimGroup(f);
                if (group > 0) {
                    ImageLayer img = Atlantis.media.getPiecenTile(group % Media.PIECEN_COUNT);
                    img.setTranslation(f.piecenSpot.getX(), f.piecenSpot.getY());
                    img.setAlpha(0.5f);
                    _debug.add(img);
                }
            }
        }

        protected Placement _play;
        protected GroupLayer _debug;
    }
}
