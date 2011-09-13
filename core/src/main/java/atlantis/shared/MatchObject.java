//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

import com.threerings.nexus.distrib.DAttribute;
import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.NexusObject;
import com.threerings.nexus.distrib.Singleton;

/**
 * A singleton object that vends the match service.
 */
public class MatchObject extends NexusObject
    implements Singleton
{
    /** Provides match-making services. */
    public DService<MatchService> matchSvc;

    public MatchObject (DService<MatchService> matchSvc) {
        this.matchSvc = matchSvc;
    }

    @Override
    protected DAttribute getAttribute (int index) {
        switch (index) {
        case 0: return matchSvc;
        default: throw new IndexOutOfBoundsException("Invalid attribute index " + index);
        }
    }

    @Override
    protected int getAttributeCount () {
        return 1;
    }
}
