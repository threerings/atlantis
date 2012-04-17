//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Arrays;
import java.util.HashSet;

import playn.core.Font;
import playn.core.PlayN;

import react.UnitSlot;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Layout;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.layout.AxisLayout;

import atlantis.shared.GameObject;
import static atlantis.shared.Log.log;

/**
 * Displays the game title and main menu.
 */
public class MainMenuScreen extends AtlantisScreen
{
    @Override // from Screen
    public void wasAdded () {
        super.wasAdded();

        Root root = iface.createRoot(AxisLayout.vertical().gap(25), UI.stylesheet);
        root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF).inset(5))));
        layer.add(root.layer);

        Font megaFont = PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 64);
        Styles tstyles = Styles.make(Style.FONT.is(megaFont),
                                     Style.TEXT_EFFECT.is(Style.TextEffect.SHADOW));
        Styles alignTop = Styles.make(Style.VALIGN.is(Style.VAlign.TOP));

        // if we're on a skinny (i.e. smartphone) screen; use a different menu layout
        Layout l2 = (PlayN.graphics().screenWidth() < 400) ? AxisLayout.vertical().gap(25) :
            AxisLayout.horizontal().gap(50);

        Button lgb, ogb, tgb;
        root.add(
            new Label("Atlantis", tstyles),
            new Group(l2, alignTop).add(
                new Group(AxisLayout.vertical().offStretch()).add(
                    new Label("Start new game:"),
                    lgb = new Button("Local game"),
                    ogb = new Button("Online game"),
                    tgb = new Button("Play-by-email game")),
                new Group(AxisLayout.vertical().offStretch()).add(
                    new Label("Games in-progress:"))));

        root.setSize(PlayN.graphics().width(), PlayN.graphics().height());

        lgb.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                startLocalGame(new String[] { "Mahatma Ghandi", "Elvis Presley", "Madonna" });
            }
        });
        ogb.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                Atlantis.screens.push(new PlayOnlineScreen());
            }
        });
    }

    protected void startLocalGame (String[] players) {
        log.info("Starting local game", "players", players);
        GameObject gobj = LocalGameManager.createLocalGame(players);
        Atlantis.screens.push(new GameScreen(gobj, new HashSet<Integer>(Arrays.asList(0, 1, 2))));
    }
}
