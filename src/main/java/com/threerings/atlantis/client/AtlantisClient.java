//
// $Id$

package com.threerings.atlantis.client;

import java.util.Random;

import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.SurfaceLayer;
import static forplay.core.ForPlay.*;

import com.threerings.atlantis.shared.Log;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
    // from interface Game
    public void init ()
    {
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

        Atlantis.tiles.init();

        // create a background layer that will tile a pattern
        _bground = graphics().createSurfaceLayer(graphics().width(), graphics().height());
        _bground.surface().setFillPattern(graphics().createPattern(Atlantis.tiles.getTableImage()));
        graphics().rootLayer().add(_bground);

        // TEMP: create a game controller and board and throw them up
        Board board = new Board();
        graphics().rootLayer().add(board.layer);

        GameController ctrl = new GameController(board);
        ctrl.startGame();
    }

    // from interface Game
    public void update (float delta)
    {
    }

    // from interface Game
    public void paint (float alpha)
    {
        _bground.surface().fillRect(0, 0, graphics().width(), graphics().height());
    }

    // from interface Game
    public int updateRate ()
    {
        return 30;
    }

    protected SurfaceLayer _bground;
}
