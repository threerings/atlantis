//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link MatchService} and/or nested classes.
 */
public class Streamer_MatchService
{
    /**
     * Handles the streaming of {@link MatchService.GameInfo} instances.
     */
    public static class GameInfo
        implements Streamer<MatchService.GameInfo>
    {
        @Override
        public Class<?> getObjectClass () {
            return MatchService.GameInfo.class;
        }

        @Override
        public void writeObject (Streamable.Output out, MatchService.GameInfo obj) {
            writeObjectImpl(out, obj);
        }

        @Override
        public MatchService.GameInfo readObject (Streamable.Input in) {
            return new MatchService.GameInfo(
                in.<GameObject>readValue(),
                in.readInt()
            );
        }

        public static  void writeObjectImpl (Streamable.Output out, MatchService.GameInfo obj) {
            out.writeValue(obj.gobj);
            out.writeInt(obj.playerIdx);
        }
    }

    // no streamer for non-Streamable enclosing class: MatchService
}
