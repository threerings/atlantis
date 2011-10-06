//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.html.HtmlPlatform;
import playn.html.HtmlGame;
import playn.core.PlayN;

import com.google.gwt.user.client.Window;

import com.threerings.nexus.client.GWTClient;

import atlantis.shared.AtlantisSerializer;

/**
 * The main entry point for the HTML5 client.
 */
public class AtlantisHtml extends HtmlGame
{
    @Override public void start() {
        HtmlPlatform platform = HtmlPlatform.register();
        platform.assetManager().setPathPrefix("atlantis/");
        Atlantis.setClient(GWTClient.create(Atlantis.SIMPLE_PORT, new AtlantisSerializer()));
        PlayN.run(new AtlantisClient());
        // scroll the iPhone header crap off the screen
        Window.scrollTo(0, 0);
    }
}
