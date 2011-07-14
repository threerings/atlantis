//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

import com.threerings.nexus.distrib.NexusService;

/**
 * Mediates the interaction between an Atlantis client and server.
 */
public interface GameService extends NexusService
{
    /**
     * Lets the server know that the player in question is ready. In a networked multiplayer game,
     * each client has to report in as ready before the game will start.
     */
    void playerReady (int playerIdx);

    /**
     * Submits the supplied play on behalf of the player with the specified index.
     */
    void play (int playerIdx, Placement play);
}
