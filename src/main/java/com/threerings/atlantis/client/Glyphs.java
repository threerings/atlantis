//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import forplay.core.ImageLayer;
import forplay.core.GroupLayer;
import static forplay.core.ForPlay.*;

import pythagoras.f.Rectangle;
import pythagoras.f.IRectangle;

import com.threerings.anim.Animation;

import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.GameTile;

/**
 * Various glyphs that we render on the board.
 */
public class Glyphs
{
    /** Displays a tile-sized graphic. */
    public static class Tile {
        public final GroupLayer layer;

        public IRectangle bounds () {
            return _bounds;
        }

        public void setOrient (Orient orient, boolean animate) {
            float dur = animate ? 1000 : 0;
            if (_rotA != null) {
                _rotA.cancel();
            }
            _rotA = Atlantis.anim.tweenRotation(layer).easeInOut().to(orient.rotation()).in(dur);
        }

        public void setLocation (Location loc, boolean animate, Runnable onComplete) {
            // TODO: disable hit testing while animating
            _bounds.setLocation(loc.x * GameTiles.TERRAIN_WIDTH,
                                loc.y * GameTiles.TERRAIN_HEIGHT);
            if (_moveA != null) {
                _moveA.cancel();
            }
            float dur = animate ? 1000 : 0;
            _moveA = Atlantis.anim.tweenXY(layer).easeInOut().
                to(_bounds.getCenterX(), _bounds.getCenterY()).in(dur);
            if (onComplete != null) {
                _moveA.then().action(onComplete);
            }
        }

        public boolean hitTest (float x, float y) {
            return _bounds.contains(x, y);
        }

        protected Tile () {
            layer = graphics().createGroupLayer();
            _bounds = new Rectangle(0, 0, GameTiles.TERRAIN_WIDTH, GameTiles.TERRAIN_HEIGHT);
            layer.setOrigin(_bounds.width/2, _bounds.height/2);
        }

        protected Animation _rotA, _moveA;
        protected float _orient;
        protected Rectangle _bounds;
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
        // TODO: piecen?
        public Play (Placement play) {
            this(play.tile);
            setOrient(play.orient, false);
            setLocation(play.loc, false, null);
        }

        public Play (GameTile tile) {
            layer.add(Atlantis.tiles.getTerrainTile(tile.terrain.tileIdx));
            // TODO: add shield glyph if requested
        }
    }
}
