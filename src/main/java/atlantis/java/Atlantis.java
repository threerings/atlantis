//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.java;

import playn.java.JavaPlatform;
import playn.core.PlayN;

import atlantis.client.AtlantisClient;

/**
 * The main entry point for the Java client.
 */
public class Atlantis
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assetManager().setPathPrefix("src/main/resources");
        PlayN.run(new AtlantisClient());
    }
}
