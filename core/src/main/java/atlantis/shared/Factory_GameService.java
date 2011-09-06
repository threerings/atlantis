//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.io.ServiceFactory;

/**
 * Creates {@link GameService} marshaller instances.
 */
public class Factory_GameService implements ServiceFactory<GameService>
{
    @Override
    public DService<GameService> createService ()
    {
        return new Marshaller();
    }

    public static DService<GameService> createDispatcher (final GameService service)
    {
        return new DService.Dispatcher<GameService>() {
            @Override public GameService get () {
                return service;
            }

            @Override public Class<GameService> getServiceClass () {
                return GameService.class;
            }

            @Override public void dispatchCall (short methodId, Object[] args) {
                switch (methodId) {
                case 1:
                    service.playerReady(
                        this.<Integer>cast(args[0]));
                    break;
                case 2:
                    service.play(
                        this.<Integer>cast(args[0]),
                        this.<Placement>cast(args[1]),
                        this.<Piecen>cast(args[2]));
                    break;
                default:
                    super.dispatchCall(methodId, args);
                }
            }
        };
    }

    protected static class Marshaller extends DService<GameService> implements GameService
    {
        @Override public GameService get () {
            return this;
        }
        @Override public Class<GameService> getServiceClass () {
            return GameService.class;
        }
        @Override public void playerReady (int playerIdx) {
            postCall((short)1, playerIdx);
        }
        @Override public void play (int playerIdx, Placement play, Piecen piecen) {
            postCall((short)2, playerIdx, play, piecen);
        }
    }
}
