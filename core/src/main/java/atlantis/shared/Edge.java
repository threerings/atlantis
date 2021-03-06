//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

/**
 * Enumerates the potential edge types of a tile.
 */
public enum Edge
{
    /** A city edge. */
    CITY,

    /** A road edge. */
    ROAD,

    /** A grass edge. */
    GRASS;

    /** Bit mask for a north connecting feature. */
    public static final int NORTH_F = 0x1 << 0;

    /** Bit mask for an east connecting feature. */
    public static final int EAST_F = 0x1 << 1;

    /** Bit mask for a south connecting feature. */
    public static final int SOUTH_F = 0x1 << 2;

    /** Bit mask for a west connecting feature. */
    public static final int WEST_F = 0x1 << 3;

    /** Bit mask for a north by northeast connecting feature. */
    public static final int NNE_F = 0x1 << 4;

    /** Bit mask for an east by northeast connecting feature. */
    public static final int ENE_F = 0x1 << 5;

    /** Bit mask for an east by southeast connecting feature. */
    public static final int ESE_F = 0x1 << 6;

    /** Bit mask for a south by southeast connecting feature. */
    public static final int SSE_F = 0x1 << 7;

    /** Bit mask for a south by southwest connecting feature. */
    public static final int SSW_F = 0x1 << 8;

    /** Bit mask for a west by southwest connecting feature. */
    public static final int WSW_F = 0x1 << 9;

    /** Bit mask for a west by northwest connecting feature. */
    public static final int WNW_F = 0x1 << 10;

    /** Bit mask for a north by northwest connecting feature. */
    public static final int NNW_F = 0x1 << 11;

    /** Used to express edge mask adjacencies. */
    public static class Adjacency {
        /** The mask for the edge in question. */
        public final int edge;

        /** The direction in which the opposite edge lies. */
        public final Orient dir;

        /** The mask for the opposite edge. */
        public final int opposite;

        public Adjacency (int edge, Orient dir, int opposite) {
            this.edge = edge;
            this.dir = dir;
            this.opposite = opposite;
        }

        @Override public String toString () {
            return dir.toString();
        }
    }

    /** The adjacencies of edge masks in the natural direction. */
    public static Adjacency[] ADJACENCIES = new Adjacency[] {
        new Adjacency(NORTH_F, Orient.NORTH, SOUTH_F),
        new Adjacency(EAST_F, Orient.EAST, WEST_F ),
        new Adjacency(SOUTH_F, Orient.SOUTH, NORTH_F ),
        new Adjacency(WEST_F, Orient.WEST, EAST_F ),
        new Adjacency(NNW_F, Orient.NORTH, SSW_F ),
        new Adjacency(NNE_F, Orient.NORTH, SSE_F ),
        new Adjacency(ENE_F, Orient.EAST, WNW_F ),
        new Adjacency(ESE_F, Orient.EAST, WSW_F ),
        new Adjacency(SSE_F, Orient.SOUTH, NNE_F ),
        new Adjacency(SSW_F, Orient.SOUTH, NNW_F ),
        new Adjacency(WSW_F, Orient.WEST, ESE_F ),
        new Adjacency(WNW_F, Orient.WEST, ENE_F ),
    };

    /**
     * Returns the supplied edge mask, rotated by the specified number of ticks. For a clockwise
     * rotation, provide a positive number of ticks. For a counter-clockwise rotation, provide a
     * negative number of ticks.
     */
    public static int translateMask (int edgeMask, int ticks) {
        int[] map = FEATURE_ORIENT_MAP[0];
        if ((edgeMask & (NNE_F|ESE_F|SSW_F|WNW_F)) != 0) {
            map = FEATURE_ORIENT_MAP[1];
        } else if ((edgeMask & (ENE_F|SSE_F|WSW_F|NNW_F)) != 0) {
            map = FEATURE_ORIENT_MAP[2];
        }
        return xlateMask(map, edgeMask, ticks);
    }

    /**
     * Returns a human-readable representation of the supplied edge mask.
     */
    public static String maskToString (int edgeMask) {
        StringBuilder buf = new StringBuilder();
        for (int ii = 0; ii < EDGE_NAMES.length; ii++) {
            if ((edgeMask & (1 << ii)) != 0) {
                if (buf.length() > 0) buf.append("|");
                buf.append(EDGE_NAMES[ii]);
            }
        }
        return buf.toString();
    }

    /** {@link #translateMask} helper function. */
    protected static int xlateMask (int[] map, int edgeMask, int ticks) {
        for (int ii = 0; ii < map.length; ii++) {
            if (map[ii] == edgeMask) {
                return map[(ii + 4 + ticks) % 4];
            }
        }
        return edgeMask;
    }

    /** Mapping table used to rotate feature facements. */
    protected static final int[][] FEATURE_ORIENT_MAP = new int[][] {
        // orientations rotate through one of three four-cycles
        { NORTH_F, EAST_F, SOUTH_F, WEST_F },
        { NNE_F,   ESE_F,  SSW_F,   WNW_F },
        { ENE_F,   SSE_F,  WSW_F,   NNW_F },
    };

    /** String names for the edge bit masks, in bit position order. */
    protected static final String[] EDGE_NAMES = {
        "NORTH", "EAST", "SOUTH", "WEST", "NNE", "ENE", "ESE", "SSE", "SSW", "WSW", "WNW", "NNW",
    };
}
