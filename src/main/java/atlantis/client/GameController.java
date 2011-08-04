//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import com.threerings.nexus.distrib.DSet;
import com.threerings.nexus.distrib.DValue;

import atlantis.shared.GameObject;
import atlantis.shared.GameTile;
import atlantis.shared.Location;
import atlantis.shared.Logic;
import atlantis.shared.Orient;
import atlantis.shared.Piecen;
import atlantis.shared.Placement;

/**
 * Manages the game flow. Listens for distributed state changes, handles submitting a player's
 * moves, etc.
 */
public class GameController
{
    public final Logic logic = new Logic();

    public GameController (GameScreen screen) {
        _screen = screen;
    }

    public void init  (GameObject gobj) {
        _gobj = gobj;

        // initialize our logic with the current game state
        logic.init(gobj);

        // listen for game state changes
        _gobj.state.addListener(new DValue.Listener<GameObject.State>() {
            public void valueChanged (GameObject.State value, GameObject.State oldValue) {
                switch (value) {
                case IN_PLAY:
                    gameDidStart();
                    break;
                case GAME_OVER:
                    gameDidEnd();
                    break;
                }
            }
        });

        // listen for plays and keep the logic and board up to date
        _gobj.plays.addListener(new DSet.AddedListener<Placement>() {
            public void elementAdded (Placement play) {
                logic.addPlacement(play);
                _screen.board.addPlacement(play);
            }
        });
        gobj.piecens.addListener(new DSet.Listener<Piecen>() {
            public void elementAdded (Piecen piecen) {
                logic.addPiecen(piecen);
                _screen.board.addPiecen(piecen);
            }
            public void elementRemoved (Piecen piecen) {
                logic.clearPiecen(piecen);
                _screen.board.clearPiecen(piecen);
            }
        });

        // do something appropriate if the game is already in play
        switch (_gobj.state.get()) {
        case PRE_GAME:
            // TODO: display "waiting for players" UI?
            break;
        case IN_PLAY:
            gameDidStart();
            break;
        case GAME_OVER:
            gameDidStart(); // triggers the initial board display
            gameDidEnd(); // triggers the display of game over bits
            break;
        }

        // let the server know that all the players we control are ready to go!
        for (Integer pidx : _screen.localIdxs) {
            _gobj.gameSvc.get().playerReady(pidx);
        }
    }

    public void place (Placement play, Piecen piecen) {
        // send our play up to the server; it will verify the play and we'll see a new play added
        // to the game object which will trigger the proper bits
        _gobj.gameSvc.get().play(_gobj.turnHolder.get(), play, piecen);
    }

    protected void gameDidStart () {
        // prepare the board
        _screen.board.reset();
    }

    protected void gameDidEnd () {
        // figure out the highest score
        int maxScore = 0;
        for (int score : _gobj.scores.values()) {
            maxScore = Math.max(score, maxScore);
        }

        // now report the winner(s)
        List<String> winners = Lists.newArrayList();
        for (int ii = 0; ii < _gobj.players.length; ii++) {
            if (_gobj.getScore(ii) == maxScore) {
                winners.add(_gobj.players[ii]);
            }
        }
        _screen.board.reportWinners(winners);
    }

    protected final GameScreen _screen;
    protected GameObject _gobj;
}
