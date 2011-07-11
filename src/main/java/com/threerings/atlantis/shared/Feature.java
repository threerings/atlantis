//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.Arrays;

import pythagoras.f.FloatMath;
import pythagoras.f.IPoint;
import pythagoras.f.Path;
import pythagoras.f.Point;
import pythagoras.f.RigidTransform;

import static com.threerings.atlantis.shared.Edge.*;

/**
 * Contains all of the information for a particular feature of a particular tile.
 */
public class Feature
{
    /** Creates a city features with the specified metrics. */
    public static Feature city (int edgeMask, int piecenX, int piecenY, int... shape) {
        return new Feature(Type.CITY, edgeMask, piecenX, piecenY, shape);
    }

    /** Creates a grass features with the specified metrics. */
    public static Feature grass (int edgeMask, int piecenX, int piecenY, int... shape) {
        return new Feature(Type.GRASS, edgeMask, piecenX, piecenY, shape);
    }

    /** Creates a road features with the specified metrics. */
    public static Feature road (int edgeMask, int piecenX, int piecenY, int... shape) {
        return new Feature(Type.ROAD, edgeMask, piecenX, piecenY, shape);
    }

    /** Creates a cloister features with the specified metrics. */
    public static Feature cloister (int edgeMask, int piecenX, int piecenY, int... shape) {
        return new Feature(Type.CLOISTER, edgeMask, piecenX, piecenY, shape);
    }

    /** Enumerates the different types of features. */
    public enum Type { CITY, GRASS, ROAD, CLOISTER };

    /** The type of this feature. */
    public final Type type;

    /** The edge mask associated with this feature. */
    public final int edgeMask;

    /** The (tile-relative) position at which to render a piecen. */
    public final IPoint piecenSpot;

    /**
     * Returns true if the feature contains the supplied mouse coordinates (which should be
     * relative to the tile origin) given the supplied orientation information.
     */
    public boolean contains (int mouseX, int mouseY, Orient orient) {
        return _polys[orient.index].contains(mouseX, mouseY);
    }

    @Override public int hashCode () {
        return type.hashCode() ^ edgeMask;
    }

    @Override public boolean equals (Object other) {
        Feature ofeature = (Feature)other;
        return ofeature.type == type && ofeature.edgeMask == edgeMask;
    }

    @Override public String toString () {
        return type + "(" + Edge.maskToString(edgeMask) + ")";
    }

    /**
     * Creates a new feature with the supplied metadata.
     */
    protected Feature (Type type, int edgeMask, int piecenX, int piecenY, int[] shape) {
        this.type = type;
        this.edgeMask = edgeMask;

        // fetch our natural piecen spot, scale it and adjust for half units
        int px = (piecenX * Constants.TILE_WIDTH) / 4;
        if (px < 0) {
            px *= -1;
            px -= Constants.TILE_WIDTH/8;
        }
        int py = (piecenY * Constants.TILE_HEIGHT) / 4;
        if (py < 0) {
            py *= -1;
            py -= Constants.TILE_HEIGHT/8;
        }
        // // oh, just a teeny hack for aesthetic shield placement
        // if (type == -1) {
        //     px += 2; py += 2;
        // }
        piecenSpot = new Point(px, py);

        // create our natural feature polygon
        if (type == Type.ROAD) {
            // roads are handled specially; they are either one or two segment roads
            if (shape.length == 4) {
                _polys[Orient.NORTH.index] = roadSegmentToPolygon(
                    shape[0], shape[1], shape[2], shape[3]);

            } else if (shape.length == 6) {
                _polys[Orient.NORTH.index] = roadSegmentToPolygon(
                    shape[0], shape[1], shape[2], shape[3], shape[4], shape[5]);

            } else {
                throw new IllegalArgumentException("Feature constructed with bogus road geometry " +
                                                   "[shape=" + Arrays.asList(shape) + "]");
            }

        } else {
            Path poly = new Path();
            for (int ii = 0; ii < shape.length; ii += 2) {
                // scale the coords accordingly
                int fx = (shape[ii] * Constants.TILE_WIDTH) / 4;
                int fy = (shape[ii+1] * Constants.TILE_HEIGHT) / 4;
                if (ii == 0) {
                    poly.moveTo(fx, fy);
                } else {
                    poly.lineTo(fx, fy);
                }
            }
            poly.closePath();
            _polys[Orient.NORTH.index] = poly;
        }

        // now create the three other orientations
        RigidTransform xform = new RigidTransform();
        for (int orient = 1; orient < 4; orient++) {
            // rotate the xform into the next orientation
            xform.translate(Constants.TILE_WIDTH, 0);
            xform.rotate(FloatMath.HALF_PI);

            // transform the polygon
            _polys[orient] = _polys[Orient.NORTH.index].clone();
            _polys[orient].transform(xform);
        }
    }

    /**
     * Massages a road segment (specified in tile feature coordinates) into a polygon (in screen
     * coordinates) that can be used to render or hit test the road. The coordinates must obey the
     * following constraints:
     *
     * (x1 < x2 and y1 == y2) or (x1 == x2 and y1 < y2) or (x1 < x2 and y1 > y2).
     *
     * @return a polygon representing the road segment (with origin at 0, 0).
     */
    protected static Path roadSegmentToPolygon (int x1, int y1, int x2, int y2)
    {
        // first convert the coordinates into screen coordinates
        x1 = (x1 * Constants.TILE_WIDTH) / 4;
        y1 = (y1 * Constants.TILE_HEIGHT) / 4;
        x2 = (x2 * Constants.TILE_WIDTH) / 4;
        y2 = (y2 * Constants.TILE_HEIGHT) / 4;

        Path poly = new Path();
        int dx = 4, dy = 4;

        // figure out what sort of line segment it is
        if (x1 == x2) { // vertical
            // make adjustments to ensure that we stay inside the tile bounds
            if (y1 == 0) {
                y1 += dy;
            }
            if (y2 == Constants.TILE_HEIGHT) {
                y2 -= dy;
            }
            poly.moveTo(x1 - dx, y1 - dy);
            poly.lineTo(x1 + dx, y1 - dy);
            poly.lineTo(x2 + dx, y2 + dy);
            poly.lineTo(x2 - dx, y2 + dy);

        } else if (y1 == y2) { // horizontal
            // make adjustments to ensure that we stay inside the tile bounds
            if (x1 == 0) {
                x1 += dx;
            }
            if (x2 == Constants.TILE_WIDTH) {
                x2 -= dx;
            }
            poly.moveTo(x1 - dx, y1 - dy);
            poly.lineTo(x1 - dx, y1 + dy);
            poly.lineTo(x2 + dx, y2 + dy);
            poly.lineTo(x2 + dx, y2 - dy);

        } else { // diagonal
            poly.moveTo(x1 - dx, y1);
            poly.lineTo(x1 + dx, y1);
            poly.lineTo(x2, y2 + dy);
            poly.lineTo(x2, y2 - dy);
        }

        poly.closePath();
        return poly;
    }

    /**
     * Massages a road segment (specified in tile feature coordinates) into a polygon (in screen
     * coordinates) that can be used to render or hit test the road. The coordinates must obey the
     * following constraints: (y1 == y2) and (y2 > y3) and (x2 == x3).
     *
     * @return a polygon representing the road segment (with origin at 0, 0).
     */
    protected static Path roadSegmentToPolygon (int x1, int y1, int x2, int y2, int x3, int y3)
    {
        // first convert the coordinates into screen coordinates
        x1 = (x1 * Constants.TILE_WIDTH) / 4;
        y1 = (y1 * Constants.TILE_HEIGHT) / 4;
        x2 = (x2 * Constants.TILE_WIDTH) / 4;
        y2 = (y2 * Constants.TILE_HEIGHT) / 4;
        x3 = (x3 * Constants.TILE_WIDTH) / 4;
        y3 = (y3 * Constants.TILE_HEIGHT) / 4;
        
        Path poly = new Path();
        int dx = 4, dy = 4;

        // figure out what sort of road segment it is
        if (x1 < x2) { // left turn
            poly.moveTo(x1, y1-dy);
            poly.lineTo(x2+dx, y2-dy);
            poly.lineTo(x3+dx, y3);
            poly.lineTo(x3-dx, y3);
            poly.lineTo(x2-dx, y2+dy);
            poly.lineTo(x1, y1+dy);

        } else { // right turn
            poly.moveTo(x1, y1-dy);
            poly.lineTo(x2-dx, y2-dy);
            poly.lineTo(x3-dx, y3);
            poly.lineTo(x3+dx, y3);
            poly.lineTo(x2+dx, y2+dy);
            poly.lineTo(x1, y1+dy);
        }

        poly.closePath();
        return poly;
    }

    /** The polygons used to render and hit test this feature (one for each orientation). */
    protected final Path[] _polys = new Path[4];

    // multiply used features
    static final Feature NESW_CITY = city(NORTH_F|EAST_F|SOUTH_F|WEST_F, 2,2, 0,0, 4,0, 4,4, 0,4);
    static final Feature NEW_CITY = city(NORTH_F|EAST_F|WEST_F, 2,2, 0,0, 4,0, 4,4, 3,3, 1,3, 0,4);
    static final Feature EW_CITY = city(EAST_F|WEST_F, 2,2, 0,0, 1,1, 3,1, 4,0, 4,4, 3,3, 1,3, 0,4);
    static final Feature NW_CITY = city(NORTH_F|WEST_F, 1,2, 0,0, 4,0, 0,4);
    static final Feature N_CITY = city(NORTH_F, 2,-1, 0,0, 1,1, 3,1, 4,0);
    static final Feature E_CITY = city(EAST_F, -4,2, 4,0, 3,1, 3,3, 4,4);
    static final Feature W_CITY = city(WEST_F, -1,2, 0,0, 1,1, 1,3, 0,4);

    static final Feature E_ROAD = road(EAST_F, 3,2, 2,2, 4,2);
    static final Feature S_ROAD = road(SOUTH_F, 2,3, 2,2, 2,4);
    static final Feature W_ROAD = road(WEST_F, 1,2, 0,2, 2,2);

    static final Feature S_GRASS = grass(SOUTH_F, 2,-4, 0,4, 1,3, 3,3, 4,4);
    static final Feature SE_GRASS = grass(ESE_F|SSE_F, -4,-4, 2,2, 4,2, 4,4, 2,4);
    static final Feature SW_GRASS = grass(WSW_F|SSW_F, -1,-4, 0,2, 2,2, 2,4, 0,4);
}
