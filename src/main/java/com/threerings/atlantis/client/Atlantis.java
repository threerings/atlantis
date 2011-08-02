//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Random;

import com.threerings.anim.Animator;
import com.threerings.game.ScreenStack;
import com.threerings.util.Randoms;

import atlantis.shared.Log;

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

    /** Manages our game screens. */
    public static final ScreenStack screens = new ScreenStack() {
        protected void handleError (RuntimeException error) {
            Log.warning("Screen error", error);
        }
    };
}
