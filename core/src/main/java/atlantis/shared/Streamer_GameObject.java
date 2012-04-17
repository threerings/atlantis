//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link GameObject} and/or nested classes.
 */
public class Streamer_GameObject
    implements Streamer<GameObject>
{
    /**
     * Handles the streaming of {@link GameObject.Score} instances.
     */
    public static class Score
        implements Streamer<GameObject.Score>
    {
        @Override
        public Class<?> getObjectClass () {
            return GameObject.Score.class;
        }

        @Override
        public void writeObject (Streamable.Output out, GameObject.Score obj) {
            writeObjectImpl(out, obj);
        }

        @Override
        public GameObject.Score readObject (Streamable.Input in) {
            return new GameObject.Score(
                in.readInt(),
                in.<Piecen>readValue()
            );
        }

        public static  void writeObjectImpl (Streamable.Output out, GameObject.Score obj) {
            out.writeInt(obj.score);
            out.writeValue(obj.piecen);
        }
    }

    @Override
    public Class<?> getObjectClass () {
        return GameObject.class;
    }

    @Override
    public void writeObject (Streamable.Output out, GameObject obj) {
        writeObjectImpl(out, obj);
        obj.writeContents(out);
    }

    @Override
    public GameObject readObject (Streamable.Input in) {
        GameObject obj = new GameObject(
            in.readStrings(),
            in.<GameService>readService()
        );
        obj.readContents(in);
        return obj;
    }

    public static  void writeObjectImpl (Streamable.Output out, GameObject obj) {
        out.writeStrings(obj.players);
        out.writeService(obj.gameSvc);
    }
}
