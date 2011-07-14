//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.Arrays;
import java.util.HashSet;

import forplay.core.Game;
import static forplay.core.ForPlay.*;

import com.threerings.atlantis.shared.GameObject;
import com.threerings.atlantis.shared.Log;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
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

        Atlantis.media.init();

        // start a fake game
        GameObject gobj = LocalGameService.createLocalGame(
            new String[] { "Mahatma Ghandi", "Elvis Presley", "Madonna" });
        new GameController(gobj, new HashSet<Integer>(Arrays.asList(0, 1, 2)));
    }

    // from interface Game
    public void update (float delta) {
        _elapsed += delta;
    }

    // from interface Game
    public void paint (float alpha) {
        float current = _elapsed + alpha * updateRate();
        Atlantis.anim.update(current);
    }

    // from interface Game
    public int updateRate () {
        return 30;
    }

    protected float _elapsed;
}
