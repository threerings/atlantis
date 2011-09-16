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
// import atlantis.shared.AtlantisSerializer;

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
        props.setProperty("nexus.hostname", "localhost");
        props.setProperty("nexus.rpc_timeout", "1000");
        NexusConfig config = new NexusConfig(props);

        // create our server
        ExecutorService exec = Executors.newFixedThreadPool(3);
        NexusServer server = new NexusServer(config, exec);

        // create our singleton match manager
        new MatchManager(server);

        // set up a connection manager and listen on a port
        final JVMConnectionManager jvmmgr = new JVMConnectionManager(server.getSessionManager());
        jvmmgr.listen(config.publicHostname, Atlantis.SIMPLE_PORT);
        jvmmgr.start();

        // // set up a Jetty instance and our GWTIO servlet
        // final GWTConnectionManager gwtmgr = new GWTConnectionManager(
        //     server.getSessionManager(), new AtlantisSerializer(), config.publicHostname, 6502);
        // gwtmgr.setDocRoot(new File("dist/webapp"));
        // gwtmgr.start();
    }
}
