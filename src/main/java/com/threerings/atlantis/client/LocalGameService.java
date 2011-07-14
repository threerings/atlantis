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
import com.threerings.atlantis.shared.GameTiles;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;

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
        if (_gobj.state.get() != GameObject.State.PRE_GAME) {
            throw new IllegalStateException(
                "Player reported ready and we're not in pregame [pidx=" + playerIdx + "]");
        }
        if (!_ready.add(playerIdx)) {
            throw new IllegalStateException(
                "Player reported ready, but they're already ready [pidx=" + playerIdx + "]");
        }
        if (_ready.size() == _gobj.players.length) {
            startGame();
        }
    }

    // from interface GameService
    public void play (int playerIdx, Placement play) {
        if (playerIdx != _gobj.turnHolder.get()) {
            throw new IllegalStateException(
                "Refusing play from non-turnholder [thidx=" + _gobj.turnHolder.get() +
                ", pidx=" + playerIdx + "]");
        }

        // in a local game, we don't validate the moves, we *are* the client so there's no question
        // of whether or not we trust the client

        _gobj.plays.add(play);
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
    }

    protected void startGame () {
        // prepare our game tiles
        _tileBag = GameTiles.standard();
        Atlantis.rands.shuffle(_tileBag);

        // place the starting tile on the board
        _tileBag.remove(GameTiles.STARTER);
        _gobj.plays.add(new Placement(GameTiles.STARTER, Orient.NORTH, new Location(0, 0), null));

        // note that the game is in play and choose the first turn-holder
        _gobj.state.update(GameObject.State.IN_PLAY);
        startTurn(Atlantis.rands.getInt(_gobj.players.length));
    }

    protected void startTurn (int turnHolder) {
        _gobj.placing.update(_tileBag.remove(0));
        _gobj.tilesRemaining.update(_tileBag.size());
        _gobj.turnHolder.update(turnHolder);
    }

    protected GameObject _gobj;
    protected Set<Integer> _ready = new HashSet<Integer>();
    protected List<GameTile> _tileBag;
}
