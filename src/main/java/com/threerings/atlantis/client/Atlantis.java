//
// $Id$

package com.threerings.atlantis.client;

import java.util.Random;

import com.threerings.util.Randoms;

/**
 * Holds static references to all the services for a game.
 */
public class Atlantis
{
    /** Provides tile services. */
    public static final AtlantisTiles tiles = new AtlantisTiles();

    /** For great randomization. */
    public static final Random rando = new Random();

    /** For even greater randomization. */
    public static final Randoms rands = Randoms.with(rando);
}