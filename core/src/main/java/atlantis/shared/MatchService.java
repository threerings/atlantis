//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.distrib.NexusObject;
import com.threerings.nexus.distrib.NexusService;
import com.threerings.nexus.util.Callback;

/**
 * Provides a brain-dead match-making services.
 */
public interface MatchService extends NexusService
{
    /** Communicates game information back to a matched player. */
    class GameInfo implements ObjectResponse {
        /** The game distributed object. */
        public final GameObject gobj;

        /** The requesting player's index in the players array. */
        public final int playerIdx;

        public GameInfo (GameObject gobj, int playerIdx) {
            this.gobj = gobj;
            this.playerIdx = playerIdx;
        }

        public NexusObject[] getObjects () {
            return new NexusObject[] { gobj };
        }
    }

    /** Requests to be matched up in a game. */
    void matchMe (Callback<GameInfo> callback);

    /** Cancels any pending match request. */
    void nevermind ();
}
