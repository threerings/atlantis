//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link Placement} and/or nested classes.
 */
public class Streamer_Placement
    implements Streamer<Placement>
{
    @Override
    public Class<?> getObjectClass () {
        return Placement.class;
    }

    @Override
    public void writeObject (Streamable.Output out, Placement obj) {
        writeObjectImpl(out, obj);
    }

    @Override
    public Placement readObject (Streamable.Input in) {
        return new Placement(
            in.<GameTile>readValue(),
            in.readEnum(Orient.class),
            in.<Location>readValue()
        );
    }

    public static  void writeObjectImpl (Streamable.Output out, Placement obj) {
        out.writeValue(obj.tile);
        out.writeEnum(obj.orient);
        out.writeValue(obj.loc);
    }
}
