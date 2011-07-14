//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.List;

import com.threerings.atlantis.shared.GameTile;
import com.threerings.atlantis.shared.GameTiles;
import com.threerings.atlantis.shared.Location;
import com.threerings.atlantis.shared.Logic;
import com.threerings.atlantis.shared.Orient;
import com.threerings.atlantis.shared.Placement;
import com.threerings.atlantis.shared.Placements;

/**
 * Manages the game flow. Listens for distributed state changes, handles submitting a player's
 * moves, etc.
 */
public class GameController
{
    public GameController (Board board, Scoreboard scores)
    {
        _board = board;
        _board.init(this);
        _scores = scores;
    }

    public void startGame ()
    {
        // TEMP: create the list of remaining tiles
        _tileBag = GameTiles.standard();
        Atlantis.rands.shuffle(_tileBag);

        // TODO: this will come from somewhere better
        _plays = new Placements();

        // prepare the board
        _board.reset(_plays);

        // TEMP: for now just allow tiles to be placed one after another
        _tileBag.remove(GameTiles.STARTER);
        place(new Placement(GameTiles.STARTER, Orient.NORTH, new Location(0, 0), null));
    }

    public void place (Placement placement)
    {
        _plays.add(placement);
        Logic.propagateClaims(_plays, placement);
        _board.addPlacement(placement);

        // TEMP: for now just allow tiles to be placed one after another
        GameTile tile = _tileBag.remove(0);
        Glyphs.Play pglyph = new Glyphs.Play(tile);
        _board.setPlacing(_plays, tile, pglyph);

        _scores.setTurnInfo(0, _tileBag.size());
        _scores.setNextTile(pglyph);
    }

    protected Board _board;
    protected Scoreboard _scores;
    protected Placements _plays;
    protected List<GameTile> _tileBag;
}
