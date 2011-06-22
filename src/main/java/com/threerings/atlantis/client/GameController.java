//
// $Id$

package com.threerings.atlantis.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;

/**
 * Manages the game flow. Listens for distributed state changes, handles submitting a player's
 * moves, etc.
 */
public class GameController
{
    public GameController (Board board)
    {
        _board = board;
        _board.init(this);
    }

    public void startGame ()
    {
        // TODO: this will come from somewhere better
        Set<Placement> plays = new HashSet<Placement>();
        plays.add(new Placement(GameTile.STARTER, Orient.NORTH, 0, 0));

        // prepare the board
        _board.reset(plays);
    }

    protected Board _board;
}
