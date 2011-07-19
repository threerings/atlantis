//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
        protected final Map<Feature,Integer> _claims = Maps.newHashMap();
    }

    /** Used to report score information following a tile placement. */
    public static class FeatureScore {
        public final Feature feature;
        public final boolean complete;
        public final Set<Integer> scorers;
        public final int score;
        public final Iterable<Piecen> piecens;

        public FeatureScore (Feature feature, Set<Integer> scorers, int score,
                             Iterable<Piecen> piecens) {
            this.feature = feature;
            this.complete = (score > 0);
            this.scorers = scorers;
            this.score = Math.abs(score);
            this.piecens = piecens;
        }
    }

    /**
     * Initializes this logic instance with the existing game state.
     */
    public void init (GameObject gobj) {
        // note our existing game state
        for (Placement play : gobj.plays) {
            addPlacement(play);
        }
        for (Piecen p : gobj.piecens) {
            addPiecen(p);
        }
    }

    /**
     * Notes the specified play, and inherits claim information onto it from its neighbors.
     */
    public void addPlacement (Placement play) {
        _plays.put(play.loc, play);

        // inherit claim groups for the features on this tile; this may result in the merging of
        // two previously unconnected claim groups, so we enumerate the full group in each case and
        // reassign the claim group as needed
        List<TileFeature> tfs = Lists.newArrayList();
        for (Feature f : play.tile.terrain.features) {
            tfs.clear();
            enumerateGroup(play, f, tfs);

            // first find the first non-zero claim group
            int group = 0;
            for (TileFeature tf : tfs) {
                int tgroup = getClaim(tf.play).getClaimGroup(tf.feature);
                if (tgroup != 0 && group == 0) {
                    group = tgroup;
                    break;
                }
            }

            // if we found no non-zero group, we have nothing to inherit
            if (group == 0) continue;

            // otherwise reassign that group to all tiles
            for (TileFeature tf : tfs) {
                getClaim(tf.play).setClaimGroup(tf.feature, group);
            }
        }
    }

    /**
     * Notes the specified piecement placement, assigns it a claim group and propagates its claim
     * information to connected tiles. The tile placement associated with this piecen must have
     * already been added via {@link #addPlacement}.
     */
    public void addPiecen (Piecen piecen) {
        _piecens.put(piecen.loc, piecen);

        // make sure a play exists at the appropriate location
        Placement play = Asserts.checkNotNull(
            _plays.get(piecen.loc), "Piecen played at location where no tile exists? %s", piecen);

        List<TileFeature> flist = Lists.newArrayList();
        enumerateGroup(play, play.getFeature(piecen.featureIdx), flist);
        int claimGroup = ++_claimGroupCounter;
        for (TileFeature feat : flist) {
            // one of these calls will result in this piecen's claim group being mapped
            getClaim(feat.play).setClaimGroup(feat.feature, claimGroup);
        }
    }

    /**
     * Clears the metadata from the specified piecen (due to it being reclaimed).
     */
    public void clearPiecen (Piecen piecen) {
        _piecens.remove(piecen.loc);
        _piecenGroups.remove(piecen.loc);
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
        Set<Location> locs = Sets.newHashSet();

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
        List<Orient> orients = Lists.newArrayList();

        // fetch the neighbors of this tile
        List<Placement> neighbors = Lists.newArrayList();
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
     * Computes the scores for all features involved in the supplied placement.
     */
    public List<FeatureScore> computeScores (Placement play) {
        List<FeatureScore> scores = Lists.newArrayList();
        Claim claim = getClaim(play);

        // check all of the features on this tile for potential scores
        for (Feature f : play.tile.terrain.features) {
            // ignore features that aren't completed in this way (e.g. GRASS)
            if (!COMPLETABLES.contains(f.type)) continue;

            // see who should score this feature
            int group = claim.getClaimGroup(f);
            Set<Integer> scorers = getScorers(group);
            // if we have no scorers, the feature is unclaimed; skip it
            if (scorers.isEmpty()) continue;

            // if we made it this far, we may have something to report
            int score = computeFeatureScore(play, f);
            if (score != 0) {
                scores.add(new FeatureScore(f, scorers, score, getPiecens(group)));
            }
        }

        // we may have also completed a cloister, so we check that as well
        for (Location nloc : play.loc.neighborhood()) {
            Placement nplay = _plays.get(nloc);
            if (nplay == null) continue;

            // check whether this tile has a piecen upon't
            Piecen p = _piecens.get(nloc);
            if (p == null) continue;

            // check whether this tile contains a cloister feature
            Feature cf = null;
            for (Feature f : nplay.tile.terrain.features) {
                if (f.type == Feature.Type.CLOISTER) {
                    cf = f;
                    break;
                }
            }
            if (cf == null) continue;

            // make sure the piecen is on the cloister
            if (_piecenGroups.get(p.loc) != getClaim(nplay).getClaimGroup(cf)) continue;

            // finally, score the cloister, which will always have only one scorer, one involved
            // piecen, and a non-zero score (simple!)
            scores.add(new FeatureScore(cf, Collections.singleton(p.ownerIdx),
                                        computeFeatureScore(nplay, cf),
                                        Collections.singletonList(p)));
        }

        return scores;
    }

    /**
     * Computes the scores for all features on the board.
     */
    public List<FeatureScore> computeFinalScores () {
        // we use this set to track which claim groups we've already processed
        Set<Integer> processedClaims = Sets.newHashSet();

        // check all features on all tiles for potential scores
        List<FeatureScore> scores = Lists.newArrayList();
        for (Placement play : _plays.values()) {
            Claim claim = getClaim(play);
            for (Feature f : play.tile.terrain.features) {
                // ignore features that aren't completable (e.g. GRASS)
                if (!COMPLETABLES.contains(f.type)) continue;

                // determine whether or not we've already processed this claim
                int group = claim.getClaimGroup(f);
                if (processedClaims.contains(group)) continue;
                processedClaims.add(group);

                // determine who will earn points for this claim
                Set<Integer> scorers = getScorers(group);
                // if we have no scorers, the feature is unclaimed; skip it
                if (scorers.isEmpty()) continue;

                // if we made it this far, we may have something to report
                int score = computeFeatureScore(play, f);
                if (score != 0) {
                    scores.add(new FeatureScore(f, scorers, score, getPiecens(group)));
                }
            }
        }

        return scores;
    }

    /**
     * Computes the scores for the claimed farms.
     */
    public List<FeatureScore> computeFarmScores () {
        // compute fake claim groups for all cities: >0 for complete, <0 for incomplete
        Map<Location,Claim> cityClaims = Maps.newHashMap();
        for (Placement play : _plays.values()) {
            cityClaims.put(play.loc, new Claim(play));
        }
        List<TileFeature> tfs = Lists.newArrayList();

        // also compute and store the size of all cities
        int nextCityClaimGroup = 0;
        for (Placement play : _plays.values()) {
            for (Feature f : play.tile.terrain.features) {
                if (f.type != Feature.Type.CITY) continue; // only care about cities
                if (cityClaims.get(play.loc).getClaimGroup(f) != 0) continue; // already handled

                tfs.clear();
                boolean completed = enumerateGroup(play, f, tfs);
                int group = ++nextCityClaimGroup;
                if (!completed) group = -group;
                for (TileFeature tf : tfs) {
                    cityClaims.get(tf.play.loc).setClaimGroup(tf.feature, group);
                }
            }
        }

        // now iterate over all claimed farms and score each of them in turn
        Set<Integer> handledClaims = Sets.newHashSet();
        List<FeatureScore> scores = Lists.newArrayList();
        for (Placement play : _plays.values()) {
            for (Feature f : play.tile.terrain.features) {
                if (f.type != Feature.Type.GRASS) continue; // only care about grass

                // skip unclaimed or already processed farms
                int group = getClaim(play).getClaimGroup(f);
                if (group == 0 || handledClaims.contains(group)) continue;
                handledClaims.add(group);

                // enumerate all the tiles in this farm, and count adjacent (complete) cities
                Set<Integer> scoringCities = Sets.newHashSet();
                tfs.clear();
                enumerateGroup(play, f, tfs);
                for (TileFeature tf : tfs) {
                    for (Feature tff : tf.play.tile.terrain.features) {
                        if (tff.type == Feature.Type.CITY) {
                            int cgroup = cityClaims.get(tf.play.loc).getClaimGroup(tff);
                            if (cgroup > 0) scoringCities.add(cgroup);
                        }
                    }
                }

                int score = scoringCities.size() * 3; // TODO: pass to rules?
                scores.add(new FeatureScore(f, getScorers(group), score, getPiecens(group)));
            }
        }

        return scores;
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

    /**
     * Computes the score for the specified feature of the specified placement. The feature is
     * assumed to be controlled by one or more players.
     * @return 0 if the feature has no intrinsic score (i.e. is GRASS), a positive score if the
     * feature is complete and a negative score if it is incomplete.
     */
    protected int computeFeatureScore (Placement play, Feature f) {
        int score = 0;
        boolean complete;

        switch (f.type) {
        default:
        case GRASS:
            return 0; // grass is scored later

        case CLOISTER:
            // cloisters score one for every tile in the 3x3 neighborhood
            for (Location nloc : play.loc.neighborhood()) {
                if (_plays.containsKey(nloc)) score++;
            }
            complete = (score == 9);
            break;

        case ROAD:
        case CITY:
            // score roads and cities by loading up the group and counting the number of tiles in it
            List<TileFeature> flist = Lists.newArrayList();
            complete = enumerateGroup(play, f, flist);

            // filter out multiple features on the same tile, scoring only counts tiles
            Map<Location, TileFeature> fmap = Maps.newHashMap();
            for (TileFeature feat : flist) {
                fmap.put(feat.play.loc, feat);
            }
            score = fmap.size();

            // when scoring city features, we add a bonus "tile" for every tile with a shield
            if (f.type == Feature.Type.CITY) {
                for (TileFeature feat : fmap.values()) {
                    if (feat.play.tile.hasShield) score++;
                }
            }

            // TODO: relegate the following to a Rules instance

            // complete city features of size greater than two score double
            if (f.type == Feature.Type.CITY && complete && score > 2) {
                score *= 2;
            }
            break;
        }

        // incomplete features are communicated via a negative score
        return complete ? score : -score;
    }

    /** Returns a set containing the index of every player that should earn points for the
     * specified claim group. The player with the most piecens on a claim group gets points for the
     * group. In the case of ties, all tying players score. */
    protected Set<Integer> getScorers (int claimGroup) {
        Map<Integer, Integer> piecens = Maps.newHashMap();

        // count the piecens in this group by player
        int max = 0;
        for (Piecen p : getPiecens(claimGroup)) {
            Integer ocount = piecens.get(p.ownerIdx);
            if (ocount == null) ocount = 0;
            piecens.put(p.ownerIdx, ocount+1);
            max = Math.max(max, ocount+1);
        }

        // now delete anyone who's score is less than the max
        for (Iterator<Integer> iter = piecens.values().iterator(); iter.hasNext(); ) {
            if (iter.next() < max) iter.remove();
        }

        return piecens.keySet();
    }

    /**
     * Returns a list of all piecens with the specified claim group.
     */
    protected Iterable<Piecen> getPiecens (final int claimGroup) {
        return Iterables.filter(_piecens.values(), new Predicate<Piecen>() {
            public boolean apply (Piecen p) {
                return _piecenGroups.get(p.loc) == claimGroup;
            }
        });
    }

    /** Used to generate claim group values. */
    protected int _claimGroupCounter;

    /** A mapping of currently placed tiles by placement location. */
    protected final Map<Location, Placement> _plays = Maps.newHashMap();

    /** A mapping of currently placed piecens by placement location. */
    protected final Map<Location, Piecen> _piecens = Maps.newHashMap();

    /** Tracks the claim group assigned to every piecen on the board. */
    protected final Map<Location, Integer> _piecenGroups = Maps.newHashMap();

    /** Maintains a mapping of claim metadata by location. */
    protected Map<Location, Claim> _claims = Maps.newHashMap();

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
    protected static final Set<Feature.Type> COMPLETABLES = ImmutableSet.of(
        Feature.Type.ROAD, Feature.Type.CITY, Feature.Type.CLOISTER);
}
