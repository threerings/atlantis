//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.java.JavaPlatform;
import playn.core.PlayN;

import com.threerings.nexus.client.JVMClient;

/**
 * The main entry point for the Java client.
 */
public class AtlantisJava
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assetManager().setPathPrefix("../core/src/main/resources/atlantis/images/");
        Atlantis.setClient(JVMClient.create(Atlantis.SIMPLE_PORT));
        PlayN.run(new AtlantisClient());
    }
}
