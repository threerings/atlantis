//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.java;

import forplay.java.JavaPlatform;
import forplay.core.ForPlay;

import com.threerings.atlantis.client.AtlantisClient;

/**
 * The main entry point for the Java client.
 */
public class Atlantis
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assetManager().setPathPrefix("src/main/resources");
        ForPlay.run(new AtlantisClient());
    }
}
