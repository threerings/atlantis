//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.distrib.NexusService;
import com.threerings.nexus.util.Callback;

/**
 * Provides a brain-dead match-making services.
 */
public interface MatchService extends NexusService
{
    /** Requests to be matched up in a game. */
    void matchMe (Callback<GameObject> callback);

    /** Cancels any pending match request. */
    void nevermind ();
}
