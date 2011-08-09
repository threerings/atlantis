//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.html;

import playn.html.HtmlPlatform;
import playn.html.HtmlGame;
import playn.core.PlayN;

import atlantis.client.AtlantisClient;

/**
 * The main entry point for the HTML5 client.
 */
public class Atlantis extends HtmlGame
{
    @Override public void start() {
        HtmlPlatform platform = HtmlPlatform.register();
        platform.assetManager().setPathPrefix("atlantis/");
        PlayN.run(new AtlantisClient());
    }
}
