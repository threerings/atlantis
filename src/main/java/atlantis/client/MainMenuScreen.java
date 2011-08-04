//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Arrays;
import java.util.HashSet;

import forplay.core.Font;
import forplay.core.ForPlay;

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
import atlantis.shared.Log;

/**
 * Displays the game title and main menu.
 */
public class MainMenuScreen extends AtlantisScreen
{
    @Override // from Screen
    public void wasAdded () {
        super.wasAdded();

        Root root = iface.createRoot(AxisLayout.vertical().gap(25), UI.stylesheet);
        root.addStyles(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 5)));
        layer.add(root.layer);

        Group botrow = new Group(AxisLayout.horizontal().alignTop().gap(50));
        Group buttons = new Group(AxisLayout.vertical().offStretch());
        Group games = new Group(AxisLayout.vertical().offStretch());
        botrow.add(buttons, games);

        Label logo = new Label().setText("Atlantis");
        logo.addStyles(Style.FONT.is(ForPlay.graphics().createFont(
                                         "Helvetica", Font.Style.PLAIN, 64)));
        root.add(logo, botrow);

        Label bheader = new Label().setText("Start new game:");
        bheader.addStyles(Style.HALIGN.is(Style.HAlign.CENTER));
        Button lgame = new Button().setText("Local game");
        Button ogame = new Button().setText("Online game");
        Button tgame = new Button().setText("Play-by-email game");
        buttons.add(bheader, lgame, ogame, tgame);

        Label theader = new Label().setText("Games in-progress:");
        theader.addStyles(Style.HALIGN.is(Style.HAlign.CENTER));
        games.add(theader);

        root.setSize(ForPlay.graphics().width(), ForPlay.graphics().height());

        lgame.click.connect(new UnitSlot() {
            @Override public void onEmit () {
                startLocalGame(new String[] { "Mahatma Ghandi", "Elvis Presley", "Madonna" });
            }
        });
    }

    protected void startLocalGame (String[] players) {
        Log.info("Starting local game", "players", players);
        GameObject gobj = LocalGameService.createLocalGame(players);
        Atlantis.screens.push(new GameScreen(gobj, new HashSet<Integer>(Arrays.asList(0, 1, 2))));
    }
}
