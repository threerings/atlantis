//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.java.JavaPlatform;
import playn.core.PlayN;

import com.threerings.nexus.client.JVMClient;

import atlantis.shared.Deployment;

/**
 * The main entry point for the Java client.
 */
public class AtlantisJava
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        int width = Math.min(1024, platform.graphics().screenWidth());
        int height = Math.min(768, platform.graphics().screenHeight());
        platform.graphics().setSize(width, height);
        platform.assets().setPathPrefix("atlantis/images");
        Atlantis.setClient(JVMClient.create(Deployment.nexusSocketPort()));
        PlayN.run(new AtlantisClient());
    }
}
