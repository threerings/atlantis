//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import com.threerings.nexus.distrib.DSet;

import forplay.core.Asserts;

/**
 * Implements various game logic.
 */
public class Logic
{
    /** Used to track claims on tile placements. */
    public class Claim {
        /** The placement for which we're tracking claims. */
        public final Placement play;

        /**
         * Returns true if the placed tile has at least one unclaimed feature.
         */
        public boolean hasUnclaimedFeature () {
            return (getUnclaimedCount() > 0);
        }

        /** Returns the count of unclaimed features on our tile. */
        public int getUnclaimedCount () {
            return play.tile.terrain.features.length - _claims.size();
        }

        /**
         * Looks for a feature on our tile that matches the supplied feature edge mask and returns
         * the claim group to which that feature belongs (which may be zero).
         *
         * @return the claim group to which the feature that matches the supplied mask belongs, or
         * zero if no feature matched the supplied mask.
         */
        public int getFeatureGroup (int edgeMask) {
            Feature f = play.findFeature(edgeMask);
            return (f == null) ? 0 : getClaimGroup(f);
        }

        /**
         * Returns the claim group assigned to the specified feature, or zero if no claim group has
         * been assigned to said feature.
         */
        public int getClaimGroup (Feature f) {
            Integer group = _claims.get(f);
            return (group == null) ? 0 : group;
        }

        /**
         * Sets the claim group for the specified feature. This also updates the claim group for
         * any piecen that was placed on that feature as well.
         */
        public void setClaimGroup (Feature f, int claimGroup) {
            // update the claim group slot for this feature
            _claims.put(f, claimGroup);

            // if we have a piecen placed on the feature, we need to update its claim group as well
            Piecen piecen = _piecens.get(play.loc);
            if (piecen != null && piecen.featureIdx == play.getFeatureIndex(f)) {
                _piecenGroups.put(play.loc, claimGroup);
            }
        }

        Claim (Placement play) {
            this.play = play;
        }

        /** A mapping from feature to claim group, for claimed features. */
        protected final Map<Feature,Integer> _claims = new HashMap<Feature,Integer>();
    }

    /**
     * Creates a new logic instance with the existing game state.
     */
    public Logic (GameObject gobj) {
        // note our existing game state
        for (Placement play : gobj.plays) {
            addPlacement(play);
        }
        for (Piecen p : gobj.piecens) {
            addPiecen(p);
        }

        // listen for additional game state changes
        gobj.plays.addListener(new DSet.AddedListener<Placement>() {
            public void elementAdded (Placement play) {
                addPlacement(play);
            }
        });
        gobj.piecens.addListener(new DSet.AddedListener<Piecen>() {
            public void elementAdded (Piecen piecen) {
                addPiecen(piecen);
            }
        });
    }

    /**
     * Creates a blank logic, for use when testing.
     */
    public Logic () {
    }

    /**
     * Notes the specified play, and inherits claim information onto it from its neighbors.
     */
    public void addPlacement (Placement play) {
        _plays.put(play.loc, play);

        // inherit claim groups for the features on this tile
        Claim claim = getClaim(play);
        for (Feature f : play.tile.terrain.features) {
            int ogroup = claim.getClaimGroup(f);
            int ngroup = computeClaim(play.tile, play.orient, play.loc, f);
            claim.setClaimGroup(f, Math.max(ogroup, ngroup));
        }
    }

    /**
     * Notes the specified piecement placement, assigns it a claim group and propagates its claim
     * information to connected tiles.
     */
    public void addPiecen (Piecen piecen) {
        _piecens.put(piecen.loc, piecen);

        // make sure a play exists at the appropriate location
        Placement play = Asserts.checkNotNull(
            _plays.get(piecen.loc), "Piecen played at location where no tile exists? %s", piecen);

        List<TileFeature> flist = new ArrayList<TileFeature>();
        enumerateGroup(play, play.getFeature(piecen.featureIdx), flist);
        int claimGroup = ++_claimGroupCounter;
        for (TileFeature feat : flist) {
            // one of these calls will result in this piecen's claim group being mapped
            getClaim(feat.play).setClaimGroup(feat.feature, claimGroup);
        }
    }

    /**
     * Returns the claim metadata for the specified location. New metadata will be created if none
     * already exists.
     */
    public Claim getClaim (Placement play) {
        Claim claim = _claims.get(play.loc);
        if (claim == null) {
            _claims.put(play.loc, claim = new Claim(play));
        }
        return claim;
    }

    /**
     * Returns the set of board positions where the supplied tile can be legally played.
     */
    public Set<Location> computeLegalPlays (GameTile tile) {
        Set<Location> locs = new HashSet<Location>();

        // compute the neighbors of all existing tiles
        for (Placement play : _plays.values()) {
            locs.addAll(play.loc.neighbors());
        }

        // now go back and remove the occupied tiles
        for (Placement play : _plays.values()) {
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
                    Placement neighbor = _plays.get(npos);
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
    public List<Orient> computeLegalOrients (GameTile tile, Location loc) {
        List<Orient> orients = new ArrayList<Orient>();

        // fetch the neighbors of this tile
        List<Placement> neighbors = new ArrayList<Placement>();
        for (Location nloc : loc.neighbors()) {
            Placement nplay = _plays.get(nloc);
            if (nplay != null) {
                neighbors.add(nplay);
            }
        }

        // and validate each candidate orientation against them
      ORIENT:
        for (Orient orient : Orient.values()) {
            Placement play = new Placement(tile, orient, loc);
            for (Placement nplay : neighbors) {
                if (!tilesMatch(nplay, play)) continue ORIENT;
            }
            orients.add(orient);
        }

        return orients;
    }

    /**
     * Computes the claim groups for the specified feature of the specified potential placement. In
     * cases where multiple claim groups abut a single feature, the higher valued group will be
     * chosen.
     */
    public int computeClaim (GameTile tile, Orient orient, Location loc, Feature f) {
        int claim = 0;
        for (Edge.Adjacency adj : Edge.ADJACENCIES) {
            // if this feature doesn't have this edge, skip it
            if ((f.edgeMask & adj.edge) == 0) continue;

            // look up our neighbor in this direction
            Placement neighbor = _plays.get(loc.neighbor(adj.dir.rotate(orient.index)));
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
            claim = Math.max(claim, getClaim(neighbor).getClaimGroup(nf));
        }
        return claim;
    }

    /**
     * Returns true if the two supplied placements match up (represent a legal board position).
     */
    protected boolean tilesMatch (Placement play1, Placement play2) {
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
    protected boolean enumerateGroup (Placement play, Feature f, List<TileFeature> target) {
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
            Placement neighbor = _plays.get(play.loc.neighbor(adj.dir.rotate(play.orient.index)));
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
            if (!enumerateGroup(neighbor, nf, target)) {
                // if our neighbor was incomplete, we become incomplete
                complete = false;
            }
        }

        return complete;
    }

    /** Used to generate claim group values. */
    protected int _claimGroupCounter;

    /** A mapping of currently placed tiles by placement location. */
    protected final Map<Location, Placement> _plays = new HashMap<Location, Placement>();

    /** A mapping of currently placed piecens by placement location. */
    protected final Map<Location, Piecen> _piecens = new HashMap<Location, Piecen>();

    /** Tracks the claim group assigned to every piecen on the board. */
    protected final Map<Location, Integer> _piecenGroups = new HashMap<Location, Integer>();

    /** Maintains a mapping of claim metadata by location. */
    protected Map<Location, Claim> _claims = new HashMap<Location, Claim>();

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

    /** Used to iterate through a tile's neighbors. */
    protected static final Location[] NEIGHBORS = {
        new Location(-1, -1), new Location(-1, +1), new Location(+1, +1), new Location(+1, -1)
    };

    /** Features that must be checked for completion via graph traversal. */
    protected static final Set<Feature.Type> COMPLETABLES = new HashSet<Feature.Type>();
    static {
        COMPLETABLES.add(Feature.Type.ROAD);
        COMPLETABLES.add(Feature.Type.CITY);
    }
}
