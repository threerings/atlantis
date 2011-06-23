//
// $Id$

package com.threerings.atlantis.shared;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Implements various game logic.
 */
public class Logic
{
    /**
     * Sets the claim group for the specified feature in this placed tile and propagates that claim
     * group to all connected features.
     *
     * @param plays the existing tile placements.
     * @param tile the tile placement that contains the feature whose claim group is being set.
     * @param featureIdx the index of the feature.
     * @param claimGroup the claim group value to set.
     */
    public static void setClaimGroup (
        Placements plays, Placement tile, int featureIdx, int claimGroup)
    {
        // load up this feature group
        List<TileFeature> flist = Lists.newArrayList();
        enumerateGroup(plays, tile, featureIdx, flist);

        // and assign the claim number to all features in the group
        for (TileFeature feat : flist) {
            feat.play.claims[feat.featureIdx] = claimGroup;
        }
    }

    /**
     * Returns the next unused claim group value.
     */
    public static int nextClaimGroup ()
    {
        return ++_claimGroupCounter;
    }

    /**
     * Computes and returns the set of board positions where the supplied tile can be legally
     * played, given the supplied preexisting plays.
     */
    public static Set<Location> computeLegalPlays (Placements plays, GameTile tile)
    {
        Set<Location> locs = Sets.newHashSet();

        // compute the neighbors of all existing tiles
        for (Placement play : plays) {
            locs.addAll(play.loc.neighbors());
        }

        // now go back and remove the occupied tiles
        for (Placement play : plays) {
            locs.remove(play.loc);
        }

        // now remove any position that is not a legal play
      OUTER:
        for (Iterator<Location> iter = locs.iterator(); iter.hasNext(); ) {
            Location pos = iter.next();
          ORIENT:
            for (Orient orient : Orient.values()) {
                Placement play = new Placement(tile, orient, pos);
                for (Location npos : pos.neighbors()) {
                    Placement neighbor = plays.get(npos);
                    if (neighbor != null && !tilesMatch(neighbor, play)) {
                        continue ORIENT; // try next orientation
                    }
                }
                continue OUTER; // this orientation matches
            }
        }

        return locs;
    }

    /**
     * Returns true if the two supplied placements match up (represent a legal board position).
     */
    protected static boolean tilesMatch (Placement play1, Placement play2)
    {
        // based on the relative positions of the two placements, determine the "natural" edges to
        // be compared (east/west or north/south)
        Orient orient1 = play1.loc.directionTo(play2.loc), orient2 = orient1.opposite();

        // now rotate those "natural" edges based on the orientations of the placements
        orient1 = orient1.rotate(-play1.orient.index);
        orient2 = orient2.rotate(-play2.orient.index);

        // now make suer those two edges match
        return play1.tile.terrain.getEdge(orient1) == play2.tile.terrain.getEdge(orient2);
    }

    /**
     * Enumerates all of the features that are in the group of which the specified feature is a
     * member.
     *
     * @return true if the group is complete (has no unconnected features), false if it is not.
     */
    protected static boolean enumerateGroup (Placements plays, Placement play, int featureIdx,
                                             List<TileFeature> target)
    {
        // create a tilefeature for this feature
        TileFeature feat = new TileFeature(play, featureIdx);

        // determine whether or not this feature is already in the group
        if (target.contains(feat)) {
            return true;
        }

        // otherwise add this feature to the group and process this
        // feature's neighbors
        target.add(feat);

        boolean complete = true;
        int fmask = play.tile.features()[featureIdx].edgeMask;

        // iterate over all of the possible adjacency possibilities
        for (Edge.Adjacency adj : Edge.ADJACENCIES) {
            // if this feature doesn't have this edge, skip it
            if ((fmask & adj.edge) == 0) {
                continue;
            }

            // look up our neighbor in this direction
            Placement neighbor = plays.get(play.loc.neighbor(adj.dir.rotate(play.orient.index)));
            if (neighbor == null) {
                // if we don't have a neighbor in a direction that we need, we're incomplete
                complete = false;
                continue;
            }

            // translate the target mask into our orientation
            int mask = Edge.translateMask(adj.edge, play.orient);
            int opp_mask = Edge.translateMask(adj.opposite, play.orient);

            // obtain the index of the feature on the opposing tile
            int nFeatureIndex = neighbor.getFeatureIndex(opp_mask);
            if (nFeatureIndex < 0) {
                Log.warning("Tile mismatch while grouping",
                            "play", play, "featIdx", featureIdx, "neighbor", neighbor,
                            "nFeatIdx", nFeatureIndex, "srcEdge", mask, "destEdge", opp_mask);
                continue;
            }

            // add this feature and its neighbors to the group
            if (!enumerateGroup(plays, neighbor, nFeatureIndex, target)) {
                // if our neighbor was incomplete, we become incomplete
                complete = false;
            }
        }

        return complete;
    }

    /** Used to keep track of actual features on placed tiles. */
    protected static final class TileFeature
    {
        /** The placement that contains the feature. */
        public final Placement play;

        /** The index of the feature in the tile. */
        public final int featureIdx;

        /** Constructs a new tile feature. */
        public TileFeature (Placement play, int featureIdx) {
            this.play = play;
            this.featureIdx = featureIdx;
        }

        @Override public boolean equals (Object other) {
            TileFeature feat = (TileFeature)other;
            return (feat.play.equals(play) && feat.featureIdx == featureIdx);
        }

        @Override public String toString () {
            return Objects.toStringHelper(this).
                add("play", play).add("fidx", featureIdx).toString();
        }
    }

    /** Used to generate claim group values. */
    protected static int _claimGroupCounter;

    /** Used to iterate through a tile's neighbors. */
    protected static final Location[] NEIGHBORS = {
        new Location(-1, -1), new Location(-1, +1), new Location(+1, +1), new Location(+1, -1)
    };
}
