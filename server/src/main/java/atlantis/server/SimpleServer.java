//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.threerings.nexus.server.GWTConnectionManager;
import com.threerings.nexus.server.JVMConnectionManager;
import com.threerings.nexus.server.NexusConfig;
import com.threerings.nexus.server.NexusServer;

import atlantis.client.Atlantis;
import atlantis.shared.AtlantisSerializer;
import atlantis.shared.Deployment;

/**
 * Operates the chat server.
 */
public class SimpleServer
{
    public static void main (String[] args)
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty("nexus.node", "test");
        props.setProperty("nexus.hostname", Deployment.nexusServerHost());
        props.setProperty("nexus.rpc_timeout", "1000");
        NexusConfig config = new NexusConfig(props);

        // create our server
        ExecutorService exec = Executors.newFixedThreadPool(3);
        NexusServer server = new NexusServer(config, exec);

        // create our singleton match manager
        new MatchManager(server);

        // set up a direct socket connection manager
        final JVMConnectionManager jvmmgr = new JVMConnectionManager(server.getSessionManager());
        jvmmgr.listen(config.publicHostname, Deployment.nexusSocketPort());
        jvmmgr.start();

        // set up a Jetty instance and our GWTIO servlet
        final GWTConnectionManager gwtmgr = new GWTConnectionManager(
            server.getSessionManager(), new AtlantisSerializer(),
            config.publicHostname, Deployment.nexusWebPort(), Deployment.nexusWebPath());
        // TODO: scan the directory to find the war dir to avoid hardcoding version
        gwtmgr.setDocRoot(new File("../html/target/atlantis-1.0-SNAPSHOT"));
        gwtmgr.start();
    }
}
