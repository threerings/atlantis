//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Collections;

import com.threerings.nexus.distrib.Address;
import com.threerings.nexus.util.Callback;

import playn.core.PlayN;

import react.UnitSlot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;

import atlantis.shared.Deployment;
import atlantis.shared.GameObject;
import atlantis.shared.MatchObject;
import atlantis.shared.MatchService;

/**
 * Connects to the server and waits for an online game opponent.
 */
public class PlayOnlineScreen extends AtlantisScreen
{
    @Override // from Screen
    public void wasAdded () {
        super.wasAdded();

        Root root = iface.createRoot(AxisLayout.vertical().gap(25), UI.stylesheet);
        root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 5))));
        layer.add(root.layer);

        final Label status = new Label("Connecting...");
        Button back = new Button("Cancel");
        back.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                Atlantis.screens.remove(PlayOnlineScreen.this);
            }
        });
        root.add(status, back);

        root.setSize(PlayN.graphics().width(), PlayN.graphics().height());

        // subscribe to the singleton MatchObject on the specified host; this will trigger a
        // connection to that host
        Atlantis.client().subscribe(
            Address.create(Deployment.nexusServerHost(), MatchObject.class),
            new Callback<MatchObject>() {
            public void onSuccess (MatchObject matchobj) {
                status.text.update("Waiting for opponent...");
                matchobj.matchSvc.get().matchMe(new Callback<MatchService.GameInfo>() {
                    public void onSuccess (MatchService.GameInfo info) {
                        Atlantis.screens.replace(
                            new GameScreen(info.gobj, Collections.singleton(info.playerIdx)));
                    }
                    public void onFailure (Throwable cause) {
                        status.text.update("Failed to find opponent: " + cause.getMessage());
                    }
                });
            }
            public void onFailure (Throwable cause) {
                status.text.update("Failed to connect: " + cause.getMessage());
            }
        });
    }
}
