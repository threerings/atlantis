//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.List;
import java.util.Set;

import com.threerings.nexus.distrib.DValue;

import com.threerings.atlantis.shared.GameObject;
import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.GameTiles;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Logic;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Placements;

/**
 * Manages the game flow. Listens for distributed state changes, handles submitting a player's
 * moves, etc.
 */
public class GameController
{
    public GameController (GameObject gobj, Set<Integer> playerIdxs) {
        _gobj = gobj;
        _board = new Board();
        _board.init(this, gobj);
        _board.scores.init(gobj.players);

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
        for (Integer pidx : playerIdxs) {
            _gobj.gameSvc.get().playerReady(pidx);
        }
    }

    public void place (Placement play) {
        // send our play up to the server; it will verify the play and we'll see a new play added
        // to the game object which will trigger the proper bits
        _gobj.gameSvc.get().play(_gobj.turnHolder.get(), play);
    }

    protected void gameDidStart () {
        // prepare the board
        _board.reset(_gobj.placements());
    }

    protected void gameDidEnd () {
        // nada at the moment
    }

    protected GameObject _gobj;
    protected Board _board;
}
