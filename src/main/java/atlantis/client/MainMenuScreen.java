//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.Arrays;
import java.util.HashSet;

import tripleplay.game.Screen;

import atlantis.client.util.TextGlyph;
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
        TextGlyph temp = TextGlyph.forText("TEMP!", Interface.uiFormat(0xFF000000));
        temp.layer.setTranslation(100, 100);
        layer.add(temp.layer);
        input.register(temp.layer, temp.bounds(), new Input.Action() {
            public void onTrigger () {
                startLocalGame(new String[] { "Mahatma Ghandi", "Elvis Presley", "Madonna" });
            }
        });
    }

    @Override // from Screen
    public void wasShown () {
        super.wasShown();
    }

    @Override // from Screen
    public void wasHidden () {
        super.wasHidden();
    }

    @Override // from Screen
    public void wasRemoved () {
        super.wasRemoved();
        layer.destroy();
    }

    protected void startLocalGame (String[] players) {
        Log.info("Starting local game", "players", players);
        GameObject gobj = LocalGameService.createLocalGame(players);
        Atlantis.screens.push(new GameScreen(gobj, new HashSet<Integer>(Arrays.asList(0, 1, 2))));
    }
}
