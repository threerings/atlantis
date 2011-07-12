//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import forplay.core.CanvasLayer;
import forplay.core.GroupLayer;
import forplay.core.ImageLayer;
import static forplay.core.ForPlay.*;

/**
 * Displays the current score, turn holder, placing tile and other metadata.
 */
public class Scoreboard
{
    /** Contains all of our scoreboard components. */
    public final GroupLayer layer = graphics().createGroupLayer();

    public void init (String[] players) {
        // create our various scoreboard interface elements
        float ypos = MARGIN;

        // title on top
        CanvasLayer title = graphics().createCanvasLayer(WIDTH-2*MARGIN, 32);
        title.canvas().setFillColor(0xFFFFFFFF); // TODO: remove
        title.canvas().strokeRect(0, 0, WIDTH-2*MARGIN-1, 32-1);
        title.canvas().drawText("Atlantis", 0, 0);
        title.setTranslation(MARGIN, ypos);
        layer.add(title);
        ypos += 32;

        // player names and piecen icons
        _playersY = ypos;
        int pidx = 0;
        for (String player : players) {
            ImageLayer piecen = Atlantis.media.getPiecenTile(pidx++);
            piecen.setTranslation(MARGIN + Media.PIECEN_WIDTH/2, ypos + Media.PIECEN_HEIGHT/2 + 2);
            layer.add(piecen);

            CanvasLayer name = graphics().createCanvasLayer(
                WIDTH-Media.PIECEN_WIDTH-2-2*MARGIN, Media.PIECEN_HEIGHT);
            name.canvas().setFillColor(0xFFFFFFFF); // TODO: remove
            name.canvas().drawText(player, 0, 0);
            name.setTranslation(MARGIN + Media.PIECEN_WIDTH + 2, ypos + 2);
            layer.add(name);

            ypos += Media.PIECEN_HEIGHT + 4;
        }
    }

    protected float _playersY;

    protected static final int WIDTH = 180;
    protected static final int MARGIN = 16;
}
