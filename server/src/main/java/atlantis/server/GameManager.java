//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.server;

import java.util.Random;

import com.threerings.nexus.distrib.Keyed;
import com.threerings.nexus.distrib.Nexus;

import tripleplay.util.Randoms;

import atlantis.shared.AbstractGameManager;
import atlantis.shared.Factory_GameService;
import atlantis.shared.GameObject;

/**
 * Manages client/server games.
 */
public class GameManager extends AbstractGameManager
    implements Keyed
{
    public GameManager (Nexus nexus, int gameId, String[] players) {
        super(Randoms.with(new Random()));
        init(new GameObject(players, Factory_GameService.createDispatcher(this)));

        // register as a keyed entity, and our object as a child in our same execution context
        _gameId = gameId;
        nexus.registerKeyed(this);
        nexus.register(_gobj, this);
    }

    public GameObject gameObject () {
        return _gobj;
    }

    @Override // from interface Keyed
    public Comparable<?> getKey () {
        return _gameId;
    }

    protected final int _gameId;
}
