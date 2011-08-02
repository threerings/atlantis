//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import forplay.core.Game;
import static forplay.core.ForPlay.*;

import atlantis.shared.Log;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
    /** Our target frames per second. */
    public static final int UPDATE_RATE = 30;

    // from interface Game
    public void init () {
        graphics().setSize(1024, 768);

        Log.setImpl(new Log.Impl() {
            public void debug (String message, Throwable t) {
                if (t != null) log().debug(message, t);
                else log().debug(message);
            }
            public void info (String message, Throwable t) {
                if (t != null) log().info(message, t);
                else log().info(message);
            }
            public void warning (String message, Throwable t) {
                if (t != null) log().warn(message, t);
                else log().warn(message);
            }
        });

        // start our various media a loadin'
        Atlantis.media.init();

        // push the main menu screen
        Atlantis.screens.push(new MainMenuScreen());
    }

    // from interface Game
    public void update (float delta) {
        Atlantis.screens.update(delta);
    }

    // from interface Game
    public void paint (float alpha) {
        Atlantis.screens.paint(alpha);
    }

    // from interface Game
    public int updateRate () {
        return UPDATE_RATE;
    }
}
