//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.DistribUtil;
import com.threerings.nexus.distrib.EventSink;
import com.threerings.nexus.distrib.NexusEvent;
import com.threerings.nexus.distrib.NexusObject;

import com.threerings.atlantis.shared.GameObject;
import com.threerings.atlantis.shared.GameService;
import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Log;
import com.threerings.atlantis.shared.Logic;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Piecen;
import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Rules;

/**
 * Implements the server-side of game management for use in non-networked games.
 */
public class LocalGameService extends DService<GameService> implements GameService
{
    public static GameObject createLocalGame (String[] players) {
        LocalGameService svc = new LocalGameService();
        GameObject gobj = new GameObject(players, svc);
        DistribUtil.init(gobj, 1, new EventSink() {
            public String getHost () {
                return "loopback";
            }
            public void postEvent (NexusObject source, NexusEvent event) {
                event.applyTo(source);
            }
            public void postCall (int objectId, short attrIndex, short methodId, Object[] args) {
                throw new UnsupportedOperationException();
            }
        });
        svc.init(gobj);
        return gobj;
    }

    // from interface GameService
    public void playerReady (int playerIdx) {
        checkState(_gobj.state.get() == GameObject.State.PRE_GAME,
                   "Player reported ready and we're not in pregame", "pidx", playerIdx);

        boolean added = _ready.add(playerIdx);
        checkState(added, "Player reported ready, but they're already ready", "pidx", playerIdx);

        if (_ready.size() == _gobj.players.length) {
            startGame();
        }
    }

    // from interface GameService
    public void play (int playerIdx, Placement play, Piecen piecen) {
        checkState(playerIdx == _gobj.turnHolder.get(), "Refusing play from non-turnholder",
                   "thidx", _gobj.turnHolder.get(), "pidx", playerIdx);

        // if the play includes a piecen, make sure the player has a piecen to play
        if (piecen != null) {
            checkState(_gobj.piecensInPlay(playerIdx) < Rules.STARTING_PIECENS,
                       "Player with no piecens tried to place with piecen",
                       "pidx", playerIdx, "play", play);
        }

        // add this play to the game state
        _gobj.plays.add(play);
        if (piecen != null) {
            _gobj.piecens.add(piecen);
        }

        // TODO: if the play completes a feature, score it and return the piecens involved

        // if we're out of tiles, end the game, otherwise start the next turn
        if (_tileBag.isEmpty()) {
            _gobj.turnHolder.update(-1);
            _gobj.state.update(GameObject.State.GAME_OVER);
        } else {
            startTurn((_gobj.turnHolder.get() + 1) % _gobj.players.length);
        }
    }

    @Override // from DService<GameService>
    public GameService get () {
        return this;
    }

    protected void init (GameObject gobj) {
        _gobj = gobj;
        _logic = new Logic(_gobj);
    }

    protected void startGame () {
        // prepare our game tiles
        _tileBag = Rules.standardTiles();
        Atlantis.rands.shuffle(_tileBag);

        // place the starting tile on the board
        _tileBag.remove(Rules.STARTER);
        _gobj.plays.add(new Placement(Rules.STARTER, Orient.NORTH, new Location(0, 0)));

        // note that the game is in play and choose the first turn-holder
        _gobj.state.update(GameObject.State.IN_PLAY);
        startTurn(Atlantis.rands.getInt(_gobj.players.length));
    }

    protected void startTurn (int turnHolder) {
        _gobj.placing.update(_tileBag.remove(0));
        _gobj.tilesRemaining.update(_tileBag.size());
        _gobj.turnHolder.update(turnHolder);
    }

    protected static void checkState (boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalStateException(Log.format(message, args));
        }
    }

    protected GameObject _gobj;
    protected Logic _logic;
    protected Set<Integer> _ready = new HashSet<Integer>();
    protected List<GameTile> _tileBag;
}
