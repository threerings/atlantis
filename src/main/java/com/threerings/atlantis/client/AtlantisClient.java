//
// $Id$

package com.threerings.atlantis.client;

import java.util.Random;

import forplay.core.ForPlay;
import forplay.core.Game;

/**
 * The main entry point for the game.
 */
public class AtlantisClient implements Game
{
    // from interface Game
    public void init ()
    {
        ForPlay.graphics().setSize(800, 600);

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
