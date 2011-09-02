//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link Location} and/or nested classes.
 */
public class Streamer_Location
    implements Streamer<Location>
{
    @Override
    public Class<?> getObjectClass () {
        return Location.class;
    }

    @Override
    public void writeObject (Streamable.Output out, Location obj) {
        writeObjectImpl(out, obj);
    }

    @Override
    public Location readObject (Streamable.Input in) {
        return new Location(
            in.readInt(),
            in.readInt()
        );
    }

    public static  void writeObjectImpl (Streamable.Output out, Location obj) {
        out.writeInt(obj.x);
        out.writeInt(obj.y);
    }
}
