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

/**
 * The main entry point for the HTML5 client.
 */
public class AtlantisHtml extends HtmlGame
{
    @Override public void start() {
        HtmlPlatform platform = HtmlPlatform.register();
        platform.assetManager().setPathPrefix("atlantis/");
        // TODO: whether to listen on 8080 or 80 needs to come from build properties
        int port = 8080;
        // TODO: the path to the servlet needs to come from build properties
        String path = "/candidate/atlantis/" + GWTConnection.WS_PATH;
        Atlantis.setClient(GWTClient.create(port, path, new AtlantisSerializer()));
        PlayN.run(new AtlantisClient());
        // scroll the iPhone header crap off the screen
        Window.scrollTo(0, 0);
    }
}
