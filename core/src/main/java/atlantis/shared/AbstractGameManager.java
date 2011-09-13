//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import tripleplay.util.Logger;
import tripleplay.util.Randoms;

import static atlantis.shared.Log.log;

/**
 * Handles most of the "server" game logic. This serves as the basis for the real server game
 * manager and for the client-side game manager used for client-local games.
 */
public class AbstractGameManager implements GameService
{
    /**
     * Provides this game manager with a reference to its game object. Must be called before the
     * manager is used.
     */
    public void init (GameObject gobj) {
        _gobj = gobj;
        _logic.init(_gobj);
    }

    // from interface GameService
    public void playerReady (int playerIdx) {
        checkState(_gobj.state.get() == GameObject.State.PRE_GAME,
                   "Player reported ready and we're not in pregame", "pidx", playerIdx);

        boolean added = _ready.add(playerIdx);
        checkState(added, "Player reported ready, but they're already ready", "pidx", playerIdx);

        if (_ready.size() == _gobj.players.length) {
            startGame();
        }
    }

    // from interface GameService
    public void play (int playerIdx, Placement play, Piecen piecen) {
        checkState(playerIdx == _gobj.turnHolder.get(), "Refusing play from non-turnholder",
                   "thidx", _gobj.turnHolder.get(), "pidx", playerIdx);

        // if the play includes a piecen, make sure the player has a piecen to play
        if (piecen != null) {
            checkState(_gobj.piecensInPlay(playerIdx) < Rules.STARTING_PIECENS,
                       "Player with no piecens tried to place with piecen",
                       "pidx", playerIdx, "play", play);
        }

        // TODO: validate that this is a legal play

        // add this play to the game state and our local logic
        _gobj.plays.add(play);
        _logic.addPlacement(play);
        if (piecen != null) {
            _gobj.piecens.add(piecen);
            _logic.addPiecen(piecen);
        }

        // if the play completes any features, score them and remove the involved piecens
        for (Logic.FeatureScore score : _logic.computeScores(play)) {
            if (score.complete) { // don't score incomplete features until endgame
                processScore(score);
            }
        }

        // if we're out of tiles, end the game, otherwise start the next turn
        if (_tileBag.isEmpty()) {
            endGame();
        } else {
            startTurn((_gobj.turnHolder.get() + 1) % _gobj.players.length);
        }
    }

    protected AbstractGameManager (Randoms rands) {
        _rands = rands;
    }

    protected void startGame () {
        // prepare our game tiles
        _tileBag = Rules.standardTiles();
        _rands.shuffle(_tileBag);

        // note that the game is in play
        _gobj.state.update(GameObject.State.IN_PLAY);

        // place the starting tile on the board
        _tileBag.remove(Rules.STARTER);
        Placement play = new Placement(Rules.STARTER, Orient.NORTH, new Location(0, 0));
        _gobj.plays.add(play);
        _logic.addPlacement(play);

        // set the scores for all players to zero
        for (int ii = 0; ii < _gobj.players.length; ii++) {
            _gobj.scores.put(ii, 0);
        }

        // choose the first turn-holder
        startTurn(_rands.getInt(_gobj.players.length));
    }

    protected void startTurn (int turnHolder) {
        List<GameTile> skipped = Lists.newArrayList();
        GameTile tile = _tileBag.remove(0);
        while (_logic.computeLegalPlays(tile).isEmpty()) {
            log.info("Skipping unplayable tile", "tile", tile);
            skipped.add(tile);
            tile = null;
            if (_tileBag.isEmpty()) break;
            tile = _tileBag.remove(0);
        }

        if (tile == null) {
            // crap, we found no playable tiles, end the game early with a message
            log.info("Ending game early due to lack of playable tiles", "skipped", skipped);
            // TODO: send message
            endGame();

        } else {
            _tileBag.addAll(skipped); // throw the skipped tiles back in the bag
            _gobj.placing.update(tile);
            _gobj.tilesRemaining.update(_tileBag.size());
            _gobj.turnHolder.update(turnHolder);
        }
    }

    protected void endGame () {
        // indicate that there is no next turn
        _gobj.placing.update(null);
        _gobj.turnHolder.update(-1);

        // score all remaining incomplete features and farms
        for (Logic.FeatureScore score : _logic.computeFinalScores()) {
            if (!score.complete) { // we only want incomplete scores
                processScore(score);
            }
        }
        for (Logic.FeatureScore score : _logic.computeFarmScores()) {
            processScore(score);
        }

        _gobj.state.update(GameObject.State.GAME_OVER);
    }

    protected void processScore (Logic.FeatureScore score) {
        // note the score increase for all participants
        for (int pidx : score.scorers) {
            _gobj.scores.put(pidx, _gobj.scores.get(pidx) + score.score);
        }

        // we only want to send a score event for at most one of the piecens owned by the
        // scoring player, so we remove them from this set once we've reported them
        Set<Integer> toReport = Sets.newHashSet(score.scorers);

        // reclaim the piecens from the completed feature
        for (Piecen p : score.piecens) {
            _gobj.piecens.remove(p);
            // maybe send an event reporting this piecen as a scorer
            if (toReport.remove(p.ownerIdx)) {
                _gobj.scoreSignal.emit(new GameObject.Score(score.score, p));
            }
        }
    }

    protected static void checkState (boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalStateException(Logger.format(message, args));
        }
    }

    protected final Randoms _rands;

    protected GameObject _gobj;
    protected Logic _logic = new Logic();
    protected Set<Integer> _ready = new HashSet<Integer>();
    protected List<GameTile> _tileBag;
}
