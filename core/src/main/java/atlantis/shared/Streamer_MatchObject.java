//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link MatchObject} and/or nested classes.
 */
public class Streamer_MatchObject
    implements Streamer<MatchObject>
{
    @Override
    public Class<?> getObjectClass () {
        return MatchObject.class;
    }

    @Override
    public void writeObject (Streamable.Output out, MatchObject obj) {
        writeObjectImpl(out, obj);
        obj.writeContents(out);
    }

    @Override
    public MatchObject readObject (Streamable.Input in) {
        MatchObject obj = new MatchObject(
            in.<MatchService>readService()
        );
        obj.readContents(in);
        return obj;
    }

    public static  void writeObjectImpl (Streamable.Output out, MatchObject obj) {
        out.writeService(obj.matchSvc);
    }
}
