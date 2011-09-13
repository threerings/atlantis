//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.DistribUtil;
import com.threerings.nexus.distrib.EventSink;
import com.threerings.nexus.distrib.NexusEvent;
import com.threerings.nexus.distrib.NexusObject;

import atlantis.shared.AbstractGameManager;
import atlantis.shared.GameObject;
import atlantis.shared.GameService;

/**
 * Manages client-only games (local hot-seat multiplayer).
 */
public class LocalGameManager extends AbstractGameManager
{
    public static GameObject createLocalGame (String[] players) {
        final LocalGameManager mgr = new LocalGameManager();
        GameObject gobj = new GameObject(players, new DService<GameService>() {
            @Override public Class<GameService> getServiceClass () {
                return GameService.class;
            }
            @Override public GameService get () {
                return mgr;
            }
        });
        DistribUtil.init(gobj, 1, new EventSink() {
            public String getHost () {
                return "loopback";
            }
            public void postEvent (NexusObject source, NexusEvent event) {
                event.applyTo(source);
            }
            public void postCall (NexusObject source, short attrIndex,
                                  short methodId, Object[] args) {
                DistribUtil.dispatchCall(source, attrIndex, methodId, args);
            }
        });
        mgr.init(gobj);
        return gobj;
    }

    public LocalGameManager () {
        super(Atlantis.rands);
    }
}
