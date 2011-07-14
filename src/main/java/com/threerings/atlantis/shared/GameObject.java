//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import java.util.HashMap;
import java.util.HashSet;

import com.threerings.nexus.distrib.DAttribute;
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
    /** The different game states. */
    public enum State { PRE_GAME, IN_PLAY, GAME_OVER };

    /** The names of the players (which are otherwise identified by index). */
    public final String[] players;

    /** Allows the client to communicate to the server. */
    public final DService<GameService> gameSvc;

    /** The current state of the game .*/
    public final DValue<State> state = DValue.create(State.PRE_GAME);

    /** The players' scores keyed on player index. */
    public final DMap<Integer,Integer> scores = DMap.create(new HashMap<Integer,Integer>());

    /** Contains the list of all plays. */
    public final DSet<Placement> plays = DSet.create(new HashSet<Placement>());

    /** The index of the current turn holder, or -1. */
    public final DValue<Integer> turnHolder = DValue.create(-1);

    /** Indicates the number of tiles remaining in the "bag". */
    public final DValue<Integer> tilesRemaining = DValue.create(0);

    /** The tile being placed by the current turn holder, or null. */
    public final DValue<GameTile> placing = DValue.create(null);

    /** Wraps the current plays map in a {@link Placements} instance for convenience. */
    public Placements placements () {
        return new Placements(plays);
    }

    public GameObject (String[] players, DService<GameService> gameSvc) {
        this.players = players;
        this.gameSvc = gameSvc;
    }

    @Override
    protected DAttribute getAttribute (int index) {
        switch (index) {
        case 0: return gameSvc;
        case 1: return state;
        case 2: return scores;
        case 3: return plays;
        case 4: return turnHolder;
        case 5: return tilesRemaining;
        case 6: return placing;
        default: throw new IndexOutOfBoundsException("Invalid attribute index " + index);
        }
    }

    @Override
    protected int getAttributeCount () {
        return 7;
    }
}
