//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.server;

import react.SignalView;
import react.Value;

import com.threerings.nexus.server.Session;
import com.threerings.nexus.server.SessionLocal;

import static atlantis.shared.Log.log;

/**
 * Contains server-only information for a player.
 */
public class Player
{
    /**
     * Returns the currently "active" player.
     *
     * @throws IllegalStateException if called when not processing an invocation service request
     * made by a player.
     */
    public static Player getPlayer () {
        Session sess = SessionLocal.requireSession();
        Player player = sess.getLocal(Player.class);
        if (player == null) {
            log.info("New player " + sess.getIPAddress());
            SessionLocal.set(Player.class, player = new Player(sess.onDisconnect()));
        }
        return player;
    }

    /** This player's name. */
    public final Value<String> name = Value.create("<anonymous>");

    /** A signal that's emitted when this player disconnects from the server. */
    public final SignalView<Void> onDisconnect;

    protected Player (SignalView<Void> onDisconnect) {
        this.onDisconnect = onDisconnect;
    }
}
