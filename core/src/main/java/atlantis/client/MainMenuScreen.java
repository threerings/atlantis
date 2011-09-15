//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Arrays;
import java.util.HashSet;

import playn.core.Font;
import playn.core.PlayN;

import react.UnitSlot;

import tripleplay.game.Screen;
import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;

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
        root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 5))));
        layer.add(root.layer);

        Font megaFont = PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 64);
        Styles tstyles = Styles.make(Style.FONT.is(megaFont),
                                     Style.TEXT_EFFECT.is(Style.TextEffect.SHADOW));
        Styles lstyles = Styles.make(Style.HALIGN.is(Style.HAlign.CENTER));

        Button lgb, ogb, tgb;
        root.add(
            new Label(tstyles).setText("Atlantis"),
            new Group(AxisLayout.horizontal().alignTop().gap(50)).add(
                new Group(AxisLayout.vertical().offStretch()).add(
                    new Label(lstyles).setText("Start new game:"),
                    lgb = new Button().setText("Local game"),
                    ogb = new Button().setText("Online game"),
                    tgb = new Button().setText("Play-by-email game")),
                new Group(AxisLayout.vertical().offStretch()).add(
                    new Label(lstyles).setText("Games in-progress:"))));

        root.setSize(PlayN.graphics().width(), PlayN.graphics().height());

        lgb.click.connect(new UnitSlot() {
            @Override public void onEmit () {
                startLocalGame(new String[] { "Mahatma Ghandi", "Elvis Presley", "Madonna" });
            }
        });
        ogb.click.connect(new UnitSlot() {
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
