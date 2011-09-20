//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.android;

import playn.android.GameActivity;
import playn.core.PlayN;

import atlantis.client.AtlantisClient;

public class AtlantisActivity extends GameActivity
{
    @Override public void main () {
        platform().assetManager().setPathPrefix("atlantis/images");
        PlayN.run(new AtlantisClient());
    }
}
