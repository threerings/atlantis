//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.html;

import forplay.html.HtmlPlatform;
import forplay.html.HtmlGame;
import forplay.core.ForPlay;

import atlantis.client.AtlantisClient;

/**
 * The main entry point for the HTML5 client.
 */
public class Atlantis extends HtmlGame
{
    @Override public void start() {
        HtmlPlatform platform = HtmlPlatform.register();
        platform.assetManager().setPathPrefix("atlantis/");
        ForPlay.run(new AtlantisClient());
    }
}
