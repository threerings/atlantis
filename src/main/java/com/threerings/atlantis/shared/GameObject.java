//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.threerings.nexus.distrib.DAttribute;
import com.threerings.nexus.distrib.DCustom;
import com.threerings.nexus.distrib.DMap;
import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.DSet;
import com.threerings.nexus.distrib.DValue;
import com.threerings.nexus.distrib.NexusObject;

/**
 * Contains the shared state of a single game.
 */
public class GameObject extends NexusObject
{
    /** An event emitted when a feature is scored. */
    public static class ScoreEvent extends DCustom.Event {
        /** The number of points earned. */
        public final int score;

        /** The piecen that scored. */
        public final Piecen piecen;

        public ScoreEvent (int targetId, short index, int score, Piecen piecen) {
            super(targetId, index);
            this.score = score;
            this.piecen = piecen;
        }
    }

    /** Used to emit and listen for score events. */
    public static class ScoreEventSlot extends DCustom<ScoreEvent> {
        public void emit (int score, Piecen piecen) {
            postEvent(new ScoreEvent(_owner.getId(), _index, score, piecen));
        }
    }

    /** The different game states. */
    public enum State { PRE_GAME, IN_PLAY, GAME_OVER };

    /** The names of the players (which are otherwise identified by index). */
    public final String[] players;

    /** Allows the client to communicate to the server. */
    public final DService<GameService> gameSvc;

    /** The current state of the game .*/
    public final DValue<State> state = DValue.create(State.PRE_GAME);

    /** The players' scores, keyed on player index. */
    public final DMap<Integer,Integer> scores = DMap.create(new HashMap<Integer,Integer>());

    /** Contains the list of all plays. */
    public final DSet<Placement> plays = DSet.create(new HashSet<Placement>());

    /** The piecens currently in play. */
    public final DSet<Piecen> piecens = DSet.create(new HashSet<Piecen>());

    /** The index of the current turn holder, or -1. */
    public final DValue<Integer> turnHolder = DValue.create(-1);

    /** Indicates the number of tiles remaining in the "bag". */
    public final DValue<Integer> tilesRemaining = DValue.create(0);

    /** The tile being placed by the current turn holder, or null. */
    public final DValue<GameTile> placing = DValue.create(null);

    /** A slot by which score events can be listened for, and (on the server), emitted. */
    public final ScoreEventSlot scoreEvent = new ScoreEventSlot();

    public GameObject (String[] players, DService<GameService> gameSvc) {
        this.players = players;
        this.gameSvc = gameSvc;
    }

    /** Returns the number of piecens in play by the specified player. */
    public int piecensInPlay (int playerIdx) {
        int count = 0;
        for (Piecen p : piecens) {
            if (p.ownerIdx == playerIdx) count++;
        }
        return count;
    }

    /** Returns the number of piecens available for play by the specified player. */
    public int piecensAvailable (int playerIdx) {
        return Rules.STARTING_PIECENS - piecensInPlay(playerIdx);
    }

    /** Returns the piecen at the specified location, or null. */
    public Piecen piecenAt (Location loc) {
        for (Piecen p : piecens) {
            if (p.loc.equals(loc)) {
                return p;
            }
        }
        return null;
    }

    /** Returns the specified player's current score. */
    public int getScore (int playerIdx) {
        Integer score = scores.get(playerIdx);
        return (score == null) ? 0 : score;
    }

    @Override
    protected DAttribute getAttribute (int index) {
        switch (index) {
        case 0: return gameSvc;
        case 1: return state;
        case 2: return scores;
        case 3: return plays;
        case 4: return piecens;
        case 5: return turnHolder;
        case 6: return tilesRemaining;
        case 7: return placing;
        case 8: return scoreEvent;
        default: throw new IndexOutOfBoundsException("Invalid attribute index " + index);
        }
    }

    @Override
    protected int getAttributeCount () {
        return 9;
    }
}
