//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import forplay.core.TextFormat;
import forplay.core.CanvasLayer;
import forplay.core.GroupLayer;
import forplay.core.Font;
import forplay.core.ImageLayer;
import static forplay.core.ForPlay.*;

import com.threerings.atlantis.client.util.TextGlyph;

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

        TextFormat titleFormat = new TextFormat().withFont(
            graphics().createFont("Helvetica", Font.Style.BOLD, 24));
        TextFormat nameFormat = new TextFormat().withFont(
            graphics().createFont("Helvetica", Font.Style.PLAIN, 16));

        // title on top
        TextGlyph title = TextGlyph.forText("Atlantis", titleFormat);
        title.layer.setTranslation(MARGIN, ypos);
        layer.add(title.layer);
        ypos += title.layer.canvas().height();

        // create our turn-holder indicator and add it before the player name layers
        _turnHolder = graphics().createCanvasLayer(WIDTH, PLAYER_HEIGHT);
        _turnHolder.canvas().setFillColor(0xFF99CCFF);
        _turnHolder.canvas().fillRect(0, 0, WIDTH, PLAYER_HEIGHT);
        layer.add(_turnHolder);
        _turnHolder.setVisible(false);

        // player names and piecen icons
        _playersY = ypos;
        int pidx = 0;
        for (String player : players) {
            ImageLayer piecen = Atlantis.media.getPiecenTile(pidx++);
            piecen.setTranslation(MARGIN + Media.PIECEN_WIDTH/2, ypos + PLAYER_HEIGHT/2);
            layer.add(piecen);

            TextGlyph name = TextGlyph.forText(player, nameFormat);
            name.layer.setTranslation(MARGIN + Media.PIECEN_WIDTH + 2, ypos + 2);
            layer.add(name.layer);

            ypos += Media.PIECEN_HEIGHT + 4;
        }

        // add remaining tiles count
        ypos += MARGIN;
        _remaining = TextGlyph.forTemplate("Remaining: 000", nameFormat);
        _remaining.layer.setTranslation(MARGIN, ypos);
        layer.add(_remaining.layer);
        ypos += _remaining.layer.canvas().height();

        // and add next tile to be placed display
        ypos += MARGIN/2;
        _nextLabel = TextGlyph.forText("Current tile:", nameFormat);
        _nextLabel.layer.setTranslation(MARGIN, ypos);
        layer.add(_nextLabel.layer);
        _nextLabel.layer.setVisible(false);
        ypos += _nextLabel.layer.canvas().height();
        _nextTileY = ypos;
        ypos += Media.TERRAIN_HEIGHT; // leave room for a terrain tile

        // finally create a solid background to go behind all these bits
        ypos += MARGIN;
        CanvasLayer bg = graphics().createCanvasLayer(WIDTH, (int)Math.ceil(ypos));
        bg.canvas().setFillColor(0xFFCCCCCC);
        bg.canvas().fillRect(0, 0, WIDTH, ypos);
        layer.add(0, bg);
    }

    public void setTurnInfo (int turnHolderIdx, int remaining) {
        _turnHolder.setTranslation(0, _playersY + turnHolderIdx * PLAYER_HEIGHT);
        _turnHolder.setVisible(turnHolderIdx >= 0);
        _remaining.setText("Remaining: " + remaining);
    }

    public void setNextTile (Glyphs.Play tile) {
        _nextLabel.layer.setVisible(tile != null);
        tile.layer.setTranslation(WIDTH/2, _nextTileY + Media.TERRAIN_HEIGHT/2);
        layer.add(tile.layer);
        // the layer will be removed for us when the tile is played
    }

    protected CanvasLayer _turnHolder;
    protected float _playersY, _nextTileY;
    protected TextGlyph _remaining, _nextLabel;

    protected static final int WIDTH = 180;
    protected static final int MARGIN = 16;
    protected static final int PLAYER_HEIGHT = Media.PIECEN_HEIGHT + 4;
}