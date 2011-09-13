//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link GameTile} and/or nested classes.
 */
public class Streamer_GameTile
    implements Streamer<GameTile>
{
    @Override
    public Class<?> getObjectClass () {
        return GameTile.class;
    }

    @Override
    public void writeObject (Streamable.Output out, GameTile obj) {
        writeObjectImpl(out, obj);
    }

    @Override
    public GameTile readObject (Streamable.Input in) {
        return new GameTile(
            in.readEnum(Terrain.class),
            in.readBoolean()
        );
    }

    public static  void writeObjectImpl (Streamable.Output out, GameTile obj) {
        out.writeEnum(obj.terrain);
        out.writeBoolean(obj.hasShield);
    }
}
