//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Set;

import atlantis.shared.GameObject;

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

    /** The indices of players controlled by this client. */
    public final Set<Integer> localIdxs;

    /**
     * Creates a game screen for the supplied game object.
     * @param playerIdxs the indices of the players controlled by this client.
     */
    public GameScreen (GameObject gobj, Set<Integer> localIdxs) {
        this.localIdxs = localIdxs;
        board.init(gobj);
        scores.init(gobj);
        ctrl.init(gobj); // the controller will trigger the start of the game
    }
}
