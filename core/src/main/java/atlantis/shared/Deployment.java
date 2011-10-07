//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

/**
 * Exposes deployment configuration to the game.
 */
public class Deployment
{
    /** The version number assigned to this build. */
    public static String version () {
        return "0";
    }

    /** The time at which this build was performed. */
    public static String time () {
        return "0";
    }

    /** The Nexus server hostname. */
    public static String nexusServerHost () {
        return "localhost";
    }

    /** The Nexus direct socket port. */
    public static int nexusSocketPort () {
        return 4321;
    }

    /** The Nexus WebSocket servlet port. */
    public static int nexusWebPort () {
        return 8080;
    }

    /** The Nexus WebSocket servlet path. */
    public static String nexusWebPath () {
        return "/nexusws";
    }
}
