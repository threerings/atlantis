//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.html.HtmlPlatform;
import playn.html.HtmlGame;
import playn.core.PlayN;

import com.threerings.nexus.client.GWTClient;

/**
 * The main entry point for the HTML5 client.
 */
public class AtlantisHtml extends HtmlGame
{
    @Override public void start() {
        HtmlPlatform platform = HtmlPlatform.register();
        platform.assetManager().setPathPrefix("atlantis/");
        // TODO: GWTIO.Serializer szer = ...
        // Atlantis.setClient(GWTClient.create(Atlantis.SIMPLE_PORT, szer));
        PlayN.run(new AtlantisClient());
    }
}