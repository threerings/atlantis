//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Random;

import com.threerings.nexus.client.NexusClient;

import tripleplay.anim.Animator;
import tripleplay.game.ScreenStack;
import tripleplay.util.Randoms;

import static atlantis.shared.Log.log;

/**
 * Holds static references to all the services for a game.
 */
public class Atlantis
{
    /** Provides images and other media. */
    public static final Media media = new Media();

    /** For great randomization. */
    public static final Random rando = new Random();

    /** For even greater randomization. */
    public static final Randoms rands = Randoms.with(rando);

    /** Communicates with the server. */
    public static NexusClient client () {
        return _client;
    }

    /** Manages our game screens. */
    public static final ScreenStack screens = new ScreenStack() {
        protected void handleError (RuntimeException error) {
            log.warning("Screen error", error);
        }
    };

    /** Called by the per-platform bootstrap class to configure our client. */
    static void setClient (NexusClient client) {
        _client = client;
    }

    protected static NexusClient _client;
}
