//
// $Id$

package com.threerings.atlantis.client;

import forplay.core.Game;

import static forplay.core.ForPlay.*;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
    /** Manages the game board. */
    public final Board board = new Board();

    /** Manages the scores and status display. */
    // public final Scores scores;

    // from interface Game
    public void init ()
    {
        graphics().setSize(800, 600);

        board.init();

        // set up our layers; we do this here because order is important
        graphics().rootLayer().add(board.layer);
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
