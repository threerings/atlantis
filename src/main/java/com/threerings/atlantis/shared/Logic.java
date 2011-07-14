//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import forplay.core.Asserts;

/**
 * Implements various game logic.
 */
public class Logic
{
    /**
     * Computes and returns the set of board positions where the supplied tile can be legally
     * played, given the supplied preexisting plays.
     */
    public static Set<Location> computeLegalPlays (Placements plays, GameTile tile) {
        Set<Location> locs = new HashSet<Location>();

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
                Placement play = new Placement(tile, orient, pos, null);
                for (Location npos : pos.neighbors()) {
                    Placement neighbor = plays.get(npos);
                    if (neighbor != null && !tilesMatch(neighbor, play)) {
                        continue ORIENT; // try next orientation
                    }
                }
                continue OUTER; // this orientation matches
            }
            iter.remove(); // no matches, remove the location
        }

        return locs;
    }

    /**
     * Computes the legal orientations in which the specified tile can be placed at the supplied
     * location.
     */
    public static List<Orient> computeLegalOrients (Placements plays, GameTile tile, Location loc) {
        List<Orient> orients = new ArrayList<Orient>();

        // fetch the neighbors of this tile
        List<Placement> neighbors = new ArrayList<Placement>();
        for (Location nloc : loc.neighbors()) {
            Placement nplay = plays.get(nloc);
            if (nplay != null) {
                neighbors.add(nplay);
            }
        }

        // and validate each candidate orientation against them
      ORIENT:
        for (Orient orient : Orient.values()) {
            Placement play = new Placement(tile, orient, loc, null);
            for (Placement nplay : neighbors) {
                if (!tilesMatch(nplay, play)) continue ORIENT;
            }
            orients.add(orient);
        }

        return orients;
    }

    /**
     * Configures claim groups for the supplied placement. Inherits claim groups from adjacent
     * features for the features on the placement that do not contain a piecen. Assigns and
     * propagates a claim group for any feature on the tile that contains a piecen.
     */
    public static void propagateClaims (Placements plays, Placement play) {
        // make sure the play in question has been added to the plays
        Asserts.checkArgument(plays.get(play.loc) == play);

        // inherit claim groups for the features on this tile
        for (Feature f : play.tile.terrain.features) {
            int ogroup = play.getClaimGroup(f);
            int ngroup = computeClaim(plays, play.tile, play.orient, play.loc, f);
            play.setClaimGroup(f, Math.max(ogroup, ngroup));
        }

        // if we have a piecen on this tile, assign it a claim group and propagate that to all
        // other connected placements
        if (play.piecen != null) {
            List<TileFeature> flist = new ArrayList<TileFeature>();
            enumerateGroup(plays, play, play.getFeature(play.piecen.featureIdx), flist);
            int claimGroup = ++_claimGroupCounter;
            for (TileFeature feat : flist) {
                feat.play.setClaimGroup(feat.feature, claimGroup);
            }
        }
    }

    /**
     * Computes the claim groups for the specified feature of the specified potential placement. In
     * cases where multiple claim groups abut a single feature, the higher valued group will be
     * chosen.
     */
    public static int computeClaim (Placements plays, GameTile tile, Orient orient,
                                    Location loc, Feature f) {
        int claim = 0;
        for (Edge.Adjacency adj : Edge.ADJACENCIES) {
            // if this feature doesn't have this edge, skip it
            if ((f.edgeMask & adj.edge) == 0) continue;

            // look up our neighbor in this direction
            Placement neighbor = plays.get(loc.neighbor(adj.dir.rotate(orient.index)));
            if (neighbor == null) continue;

            // translate the target mask into our orientation
            int mask = Edge.translateMask(adj.edge, orient.index);
            int opp_mask = Edge.translateMask(adj.opposite, orient.index);

            // obtain the index of the feature on the opposing tile
            Feature nf = neighbor.findFeature(opp_mask);
            if (nf == null) {
                Log.warning("Tile mismatch while inheriting", "tile", tile, "orient", orient,
                            "loc", loc, "feat", f, "neighbor", neighbor,
                            "srcEdge", mask, "destEdge", opp_mask);
                continue;
            }

            // inherit this feature's group; we use max() here to ensure that if a feature
            // abuts an unclaimed feature (0) and a claimed feature (>0) that we always inherit
            // the claimed feature's group
            claim = Math.max(claim, neighbor.getClaimGroup(nf));
        }
        return claim;
    }

    /**
     * Returns true if the two supplied placements match up (represent a legal board position).
     */
    protected static boolean tilesMatch (Placement play1, Placement play2) {
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
    protected static boolean enumerateGroup (Placements plays, Placement play, Feature f,
                                             List<TileFeature> target) {
        // create a tile-feature for this feature
        TileFeature feat = new TileFeature(play, f);

        // determine whether or not this feature is already in the group
        if (target.contains(feat)) {
            return true;
        }

        // otherwise add this feature to the group and process this feature's neighbors
        target.add(feat);

        // iterate over all of the possible adjacency possibilities
        boolean complete = true;
        for (Edge.Adjacency adj : Edge.ADJACENCIES) {
            // if this feature doesn't have this edge, skip it
            if ((f.edgeMask & adj.edge) == 0) {
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
            int mask = Edge.translateMask(adj.edge, play.orient.index);
            int opp_mask = Edge.translateMask(adj.opposite, play.orient.index);

            // obtain the index of the feature on the opposing tile
            Feature nf = neighbor.findFeature(opp_mask);
            if (nf == null) {
                Log.warning("Tile mismatch while propagating", "play", play, "feat", f,
                            "neighbor", neighbor, "srcEdge", mask, "destEdge", opp_mask);
                continue;
            }

            // add this feature and its neighbors to the group
            if (!enumerateGroup(plays, neighbor, nf, target)) {
                // if our neighbor was incomplete, we become incomplete
                complete = false;
            }
        }

        return complete;
    }

    /** Used to keep track of actual features on placed tiles. */
    protected static final class TileFeature {
        /** The placement that contains the feature. */
        public final Placement play;

        /** The feature of the tile. */
        public final Feature feature;

        /** Constructs a new tile feature. */
        public TileFeature (Placement play, Feature feature) {
            this.play = play;
            this.feature = feature;
        }

        @Override public boolean equals (Object other) {
            TileFeature feat = (TileFeature)other;
            return (feat.play.equals(play) && feat.feature.equals(feature));
        }

        @Override public String toString () {
            return Log.format("play", play, "feat", feature);
        }
    }

    /** Used to generate claim group values. */
    protected static int _claimGroupCounter;

    /** Used to iterate through a tile's neighbors. */
    protected static final Location[] NEIGHBORS = {
        new Location(-1, -1), new Location(-1, +1), new Location(+1, +1), new Location(+1, -1)
    };
}
