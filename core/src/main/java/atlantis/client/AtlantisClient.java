//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.core.Game;
import static playn.core.PlayN.*;

import tripleplay.util.Logger;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
    /** Our target frames per second. */
    public static final int UPDATE_RATE = 30;

    // from interface Game
    public void init () {
        // if we're on iOS, we'll see a height shorter than 416 because of the (removable) titlebar
        // (and possibly a JavaScript errors button), account for that; TODO: only do this when the
        // user-agent is appropriate
        graphics().setSize(graphics().screenWidth(), Math.max(416, graphics().screenHeight()));

        // route our logging through PlayN
        Logger.setImpl(new Logger.PlayNImpl());

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
