//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.io.ServiceFactory;
import com.threerings.nexus.util.Callback;

/**
 * Creates {@link MatchService} marshaller instances.
 */
public class Factory_MatchService implements ServiceFactory<MatchService>
{
    @Override
    public DService<MatchService> createService ()
    {
        return new Marshaller();
    }

    public static DService<MatchService> createDispatcher (final MatchService service)
    {
        return new DService.Dispatcher<MatchService>() {
            @Override public MatchService get () {
                return service;
            }

            @Override public Class<MatchService> getServiceClass () {
                return MatchService.class;
            }

            @Override public void dispatchCall (short methodId, Object[] args) {
                switch (methodId) {
                case 1:
                    service.matchMe(
                        this.<Callback<GameObject>>cast(args[0]));
                    break;
                case 2:
                    service.nevermind();
                    break;
                default:
                    super.dispatchCall(methodId, args);
                }
            }
        };
    }

    protected static class Marshaller extends DService<MatchService> implements MatchService
    {
        @Override public MatchService get () {
            return this;
        }
        @Override public Class<MatchService> getServiceClass () {
            return MatchService.class;
        }
        @Override public void matchMe (Callback<GameObject> callback) {
            postCall((short)1, callback);
        }
        @Override public void nevermind () {
            postCall((short)2);
        }
    }
}
