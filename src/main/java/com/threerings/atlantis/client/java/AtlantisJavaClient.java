//
// $Id$

package com.threerings.atlantis.client.java;

import forplay.java.JavaAssetManager;
import forplay.java.JavaPlatform;
import forplay.core.ForPlay;

import com.threerings.atlantis.client.AtlantisClient;

/**
 * The main entry point for the Java client.
 */
public class AtlantisJavaClient
{
    public static void main (String[] args)
    {
        JavaAssetManager assets = JavaPlatform.register().assetManager();
        assets.setPathPrefix("src/main/resources");
        ForPlay.run(new AtlantisClient());
    }
}
