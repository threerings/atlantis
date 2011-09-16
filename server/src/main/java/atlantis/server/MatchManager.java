//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.server;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import react.Connection;
import react.UnitSlot;

import com.threerings.nexus.distrib.Action;
import com.threerings.nexus.distrib.Nexus;
import com.threerings.nexus.distrib.Singleton;
import com.threerings.nexus.util.Callback;

import atlantis.shared.Factory_MatchService;
import atlantis.shared.GameObject;
import atlantis.shared.MatchObject;
import atlantis.shared.MatchService;

/**
 * A super simple match maker that just matches people up as they come in.
 */
public class MatchManager implements MatchService, Singleton
{
    public MatchManager (Nexus nexus)
    {
        _nexus = nexus;

        // register ourselves as a singleton
        nexus.registerSingleton(this);

        // create and register our chat object as a child singleton in our same context
        MatchObject matchobj = new MatchObject(Factory_MatchService.createDispatcher(this));
        nexus.registerSingleton(matchobj, this);

        _nexus.invokeAfter(MatchManager.class, 1000L, new Action<MatchManager>() {
            public void invoke (MatchManager mmgr) {
                mmgr.maybeStartGame();
            }
        }).repeatEvery(1000L);
    }

    @Override // from interface MatchService
    public void matchMe (Callback<MatchService.GameInfo> callback) {
        Player player = Player.getPlayer();
        final Waiter waiter = new Waiter(player, callback);
        // if the player disconnects, remove their waiter record
        waiter.onDisconnect = player.onDisconnect.connect(_nexus.routed(this, new UnitSlot() {
            public void onEmit () {
                _waiters.remove(waiter);
            }
        }));
        _waiters.add(waiter);

        // if we have enough players for a full game, start it now
        if (_waiters.size() == MAX_PLAYERS) startGame();
    }

    @Override // from interface MatchService
    public void nevermind () {
        Player player = Player.getPlayer();
        // remove the requesting player from the list of waiters
        for (Iterator<Waiter> iter = _waiters.iterator(); iter.hasNext(); ) {
            if (iter.next().player == player) {
                iter.remove();
                break;
            }
        }
    }

    protected void maybeStartGame () {
        // make sure we have enough players to even start a game
        if (_waiters.size() < 2) return;

        // if the longest waiting player has waited long enough, start a game
        long longestWait = System.currentTimeMillis() - oldestWaitStart();
        if (longestWait > MAX_WAIT) startGame();
    }

    protected void startGame () {
        List<Waiter> players = _waiters.subList(0, Math.min(MAX_PLAYERS, _waiters.size()));
        String[] playerNames = new String[players.size()];
        for (int ii = 0, ll = players.size(); ii < ll; ii++) {
            playerNames[ii] = players.get(ii).player.name.get();
        }
        // the game manager will register with the nexus and kick everything off
        GameObject gobj = new GameManager(_nexus, ++_gameId, playerNames).gameObject();
        for (int ii = 0; ii < playerNames.length; ii++) {
            Waiter w = players.get(ii);
            w.callback.onSuccess(new MatchService.GameInfo(gobj, ii));
        }
        players.clear(); // removes players from _waiters
    }

    protected long oldestWaitStart () {
        long oldestStart = _waiters.get(0).waitStart;
        for (Waiter w : _waiters) {
            oldestStart = Math.min(oldestStart, w.waitStart);
        }
        return oldestStart;
    }

    protected static class Waiter {
        public final Player player;
        public final Callback<MatchService.GameInfo> callback;
        public final long waitStart = System.currentTimeMillis();
        public Connection onDisconnect;

        public Waiter (Player player, Callback<MatchService.GameInfo> callback) {
            this.player = player;
            this.callback = callback;
        }
    }

    protected final Nexus _nexus;
    protected final List<Waiter> _waiters = Lists.newArrayList();

    /** A monotonically increasing id given to each game manager. */
    protected int _gameId;

    protected static final int MAX_PLAYERS = 4;
    protected static final long MAX_WAIT = 30*1000L;
}
