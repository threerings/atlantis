//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.Set;

import com.threerings.atlantis.shared.GameObject;

/**
 * Displays the interface for an actual game: board, scoreboard, etc.
 */
public class GameScreen extends AtlantisScreen
{
    /** Displays current scores, etc. */
    public final Scoreboard scores = new Scoreboard(this);

    /** Displays the game board. */
    public final Board board = new Board(this);

    /** Manages the flow of the game. */
    public final GameController ctrl = new GameController(this);

    /**
     * Creates a game screen for the supplied game object.
     * @param playerIdxs the indices of the players controlled by this client.
     */
    public GameScreen (GameObject gobj, Set<Integer> playerIdxs) {
        board.init(gobj);
        scores.init(gobj.players);
        // the controller will trigger the start of the game
        ctrl.init(gobj, playerIdxs);
    }
}
