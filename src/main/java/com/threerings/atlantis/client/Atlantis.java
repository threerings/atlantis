//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.Random;

import com.threerings.anim.Animator;
import com.threerings.util.Randoms;

/**
 * Holds static references to all the services for a game.
 */
public class Atlantis
{
    /** Provides images and other media. */
    public static final Media media = new Media();

    /** Performs fancy animations. */
    public static Animator anim = Animator.create();

    /** For great randomization. */
    public static final Random rando = new Random();

    /** For even greater randomization. */
    public static final Randoms rands = Randoms.with(rando);
}
