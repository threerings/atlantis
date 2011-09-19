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

        final Label status = new Label().setText("Connecting...");
        Button back = new Button().setText("Cancel");
        back.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                Atlantis.screens.remove(PlayOnlineScreen.this);
            }
        });
        root.add(status, back);

        root.setSize(PlayN.graphics().width(), PlayN.graphics().height());

        // TODO: get address from config somewhere
        String address = "localhost";

        // subscribe to the singleton MatchObject on the specified host; this will trigger a
        // connection to that host
        Atlantis.client().subscribe(
            Address.create(address, MatchObject.class), new Callback<MatchObject>() {
            public void onSuccess (MatchObject matchobj) {
                status.setText("Waiting for opponent...");
                matchobj.matchSvc.get().matchMe(new Callback<MatchService.GameInfo>() {
                    public void onSuccess (MatchService.GameInfo info) {
                        Atlantis.screens.replace(
                            new GameScreen(info.gobj, Collections.singleton(info.playerIdx)));
                    }
                    public void onFailure (Throwable cause) {
                        status.setText("Failed to find opponent: " + cause.getMessage());
                    }
                });
                // TODO
            }
            public void onFailure (Throwable cause) {
                status.setText("Failed to connect: " + cause.getMessage());
            }
        });
    }
}
