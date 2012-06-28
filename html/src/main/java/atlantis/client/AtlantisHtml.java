//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.html.HtmlPlatform;
import playn.html.HtmlGame;
import playn.core.PlayN;

import com.google.gwt.user.client.Window;

import com.threerings.nexus.client.GWTClient;
import com.threerings.nexus.net.GWTConnection;

import atlantis.shared.AtlantisSerializer;
import atlantis.shared.Deployment;

/**
 * The main entry point for the HTML5 client.
 */
public class AtlantisHtml extends HtmlGame
{
    @Override public void start() {
        HtmlPlatform platform = HtmlPlatform.register();
        // if we're on iOS, we'll see a height shorter than 416 because of the (removable) titlebar
        // (and possibly a JavaScript errors button), account for that; TODO: only do this when the
        // user-agent is appropriate
        platform.graphics().setSize(platform.graphics().screenWidth(),
                                    Math.max(416, platform.graphics().screenHeight()));
        platform.assets().setPathPrefix("atlantis/");
        Atlantis.setClient(GWTClient.create(Deployment.nexusWebPort(), Deployment.nexusWebPath(),
                                            new AtlantisSerializer()));
        PlayN.run(new AtlantisClient());
        // scroll the iPhone header crap off the screen
        Window.scrollTo(0, 0);
    }
}
