//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link Piecen} and/or nested classes.
 */
public class Streamer_Piecen
    implements Streamer<Piecen>
{
    @Override
    public Class<?> getObjectClass () {
        return Piecen.class;
    }

    @Override
    public void writeObject (Streamable.Output out, Piecen obj) {
        writeObjectImpl(out, obj);
    }

    @Override
    public Piecen readObject (Streamable.Input in) {
        return new Piecen(
            in.readInt(),
            in.<Location>readValue(),
            in.readInt()
        );
    }

    public static  void writeObjectImpl (Streamable.Output out, Piecen obj) {
        out.writeInt(obj.ownerIdx);
        out.writeValue(obj.loc);
        out.writeInt(obj.featureIdx);
    }
}
