//
// $Id$

package com.threerings.atlantis.client;

import java.util.Random;

import forplay.core.ForPlay;
import forplay.core.Game;

import com.threerings.atlantis.shared.Log;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
    // from interface Game
    public void init ()
    {
        ForPlay.graphics().setSize(800, 600);

        Log.setImpl(new Log.Impl() {
            public void debug (String message, Throwable t) {
                if (t != null) ForPlay.log().debug(message, t);
                else ForPlay.log().debug(message);
            }
            public void info (String message, Throwable t) {
                if (t != null) ForPlay.log().info(message, t);
                else ForPlay.log().info(message);
            }
            public void warning (String message, Throwable t) {
                if (t != null) ForPlay.log().warn(message, t);
                else ForPlay.log().warn(message);
            }
        });

        Atlantis.tiles.init();
        Atlantis.board.init();

        // set up our layers; we do this here because order is important
        ForPlay.graphics().rootLayer().add(Atlantis.board.layer);
    }

    // from interface Game
    public void update (float delta)
    {
    }

    // from interface Game
    public void paint (float alpha)
    {
    }

    // from interface Game
    public int updateRate ()
    {
        return 30;
    }
}
