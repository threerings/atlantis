//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import forplay.core.SurfaceLayer;
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

        Atlantis.media.init();

        // create a background layer that will tile a pattern
        _bground = graphics().createSurfaceLayer(graphics().width(), graphics().height());
        _bground.surface().setFillPattern(graphics().createPattern(Atlantis.media.getTableImage()));
        _bground.setZOrder(Atlantis.BACKGROUND_Z);
        graphics().rootLayer().add(_bground);

        // TEMP: create a game controller and board and throw them up
        Board board = new Board();
        graphics().rootLayer().add(board.tiles);
        // this layer has to go "above" the scores layer
        board.flight.setZOrder(Atlantis.SCORES_Z+1);
        graphics().rootLayer().add(board.flight);

        Scoreboard scores = new Scoreboard();
        scores.init(new String[] { "Elvis", "Madonna", "Mahatma Gandhi" });
        scores.layer.setZOrder(Atlantis.SCORES_Z);
        graphics().rootLayer().add(scores.layer);

        // TEMP: draw a grid over the board for debugging
        float width = graphics().width(), height = graphics().height();
        SurfaceLayer grid = graphics().createSurfaceLayer((int)width, (int)height);
        grid.surface().drawLine(0f, height/2, width, height/2, 1f);
        grid.surface().drawLine(width/2, 0f, width/2, height, 1f);
        graphics().rootLayer().add(grid);

        GameController ctrl = new GameController(board, scores);
        ctrl.startGame();
    }

    // from interface Game
    public void update (float delta)
    {
        _elapsed += delta;
    }

    // from interface Game
    public void paint (float alpha)
    {
        float current = _elapsed + alpha * updateRate();
        Atlantis.anim.update(current);
        _bground.surface().fillRect(0, 0, graphics().width(), graphics().height());
    }

    // from interface Game
    public int updateRate ()
    {
        return 30;
    }

    protected float _elapsed;
    protected SurfaceLayer _bground;
}
