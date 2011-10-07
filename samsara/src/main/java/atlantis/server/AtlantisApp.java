//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import com.samskivert.util.Logger;

import com.threerings.samsara.app.data.AppCodes;
import com.threerings.samsara.app.server.AbstractApp;
// TODO: switch from AbstractApp to AbstractSamsaraApp when we're ready to have a database and all
// sorts of other fiddly bits
// import com.threerings.samsara.app.server.AbstractSamsaraApp;
import com.threerings.samsara.app.server.AbstractSamsaraAppModule;
import com.threerings.samsara.app.server.NexusModule;
import com.threerings.samsara.shared.App;

import com.threerings.nexus.server.NexusServer;

import atlantis.client.Atlantis;
import atlantis.shared.AtlantisSerializer;
import atlantis.server.MatchManager;
import atlantis.shared.Deployment;

/**
 * The main entry point for the Atlantis app.
 */
@Singleton
public class AtlantisApp extends AbstractApp // AbstractSamsaraApp
{
    /** Our app identifier. */
    public static final String IDENT = "atlantis";

    /** The static log instance configured for use by this app. */
    protected static Logger log = Logger.getLogger("atlantis");

    public static class Module extends AbstractSamsaraAppModule {
        public Module () {
            super(IDENT);
        }

        @Override protected void configure () {
            super.configure();
            bind(App.class).to(AtlantisApp.class);
            install(new NexusModule(Deployment.nexusSocketPort(), new AtlantisSerializer()));
            // TODO: bind our various servlets
            // serve(AuthServlet.class).at("/auth");
        }
    }

    @Override // from App
    public int getSiteId () {
        return 0; // TODO
    }

    @Override // from App
    public void coinsPurchased (int userId, int coins) {
        // not used
    }

    @Override // from App
    public void didAttach () {
        log.info("Atlantis app initialized.", "version", _appvers /*, "build", Build.version()*/);

        // create our singleton match manager
        new MatchManager(_server);
        log.info("Created match manager " + _server);
    }

    @Override // from App
    public void didDetach () {
        log.info("Atlantis app detached.", "version", _appvers);

        // TODO: join with the Nexus executor before shutting down?
        // shutdown();
    }

    @Inject protected NexusServer _server;
    @Inject protected @Named(AppCodes.APPVERS) String _appvers;
    @Inject protected @Named(AppCodes.APPCANDIDATE) boolean _candidate;
}
