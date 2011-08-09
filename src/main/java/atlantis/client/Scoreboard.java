//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import java.util.ArrayList;
import java.util.List;

import playn.core.TextFormat;
import playn.core.CanvasLayer;
import playn.core.GroupLayer;
import playn.core.Font;
import playn.core.ImageLayer;
import static playn.core.PlayN.*;

import pythagoras.f.Dimension;
import pythagoras.f.Point;

import react.Function;
import react.Functions;
import react.Slot;
import react.Value;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.util.Coords;

import atlantis.client.util.TextGlyph;

/**
 * Displays the current score, turn holder, placing tile and other metadata.
 */
public class Scoreboard
{
    /** The current piecen counts for all of the players. */
    public final List<Value<Integer>> piecens = new ArrayList<Value<Integer>>();

    /** The current scores for all of the players. */
    public final List<Value<Integer>> scores = new ArrayList<Value<Integer>>();

    /** The number of tiles remaining. */
    public Value<Integer> remaining = Value.create(0);

    /** The current turn holder index, or -1 if it's no one's turn. */
    public Value<Integer> turnHolder = Value.create(-1);

    public Scoreboard (GameScreen screen) {
        _screen = screen;
        _root = screen.iface.createRoot(AxisLayout.vertical().gap(10), UI.stylesheet);
        _root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC, 16))));
        // adjust the depth of the UI layer; see Board for other layer depths
        _root.layer.setDepth(+1);
        screen.layer.add(_root.layer);
    }

    public void init (String[] players) {
        Styles titleStyles = Styles.make(
            Style.FONT.is(graphics().createFont("Helvetica", Font.Style.BOLD, 24)));
        Styles nameStyles = Styles.make(
            Style.FONT.is(graphics().createFont("Helvetica", Font.Style.PLAIN, 16)));
        Styles numberStyles = nameStyles.add(Style.HALIGN.is(Style.HAlign.RIGHT));

        // this is kind of sneaky, but have our player group create the turn holder background
        // layer after the first time that it is laid out (so that we know the height of a player
        // info row)
        _pgroup = new Group(AxisLayout.vertical().offStretch()) {
            @Override protected void layout () {
                super.layout();
                if (_turnHolder == null) {
                    // create our turn-holder indicator
                    int width = (int)Math.ceil(_root.size().getWidth());
                    int height = (int)Math.ceil(childAt(0).size().getHeight()) + 4;
                    _turnHolder = graphics().createCanvasLayer(width, height);
                    _turnHolder.canvas().setFillColor(0xFF99CCFF);
                    _turnHolder.canvas().fillRect(0, 0, width, height);
                    _turnHolder.setDepth(-1); // render below player names
                    _turnHolder.setTranslation(0, playerRowPos(0)); // start on player 0
                    _turnHolder.setVisible(false);
                    _root.layer.add(_turnHolder);
                }
                turnHolder.connectNotify(_highlightTurnHolder);
            }
        };

        // create our various scoreboard interface elements
        Label remain;
        _root.add(
            new Label(titleStyles).setText("Atlantis"),
            _pgroup,
            remain = new Label(nameStyles),
            new Label(nameStyles).setText("Current tile:"),
            _curtile = new Label() {
                @Override protected Dimension computeSize (float hintX, float hintY) {
                    return new Dimension(Media.TERRAIN_SIZE);
                }
            });

        remaining.map(new Function<Integer,String>() {
            public String apply (Integer remain) {
                return "Remaining: " + remain;
            }
        }).connectNotify(remain.textSlot());

        // player names and other metadata
        int pidx = 0;
        for (String player : players) {
            Label s, p;
            _pgroup.add(
                new Group(AxisLayout.horizontal()).add(
                    new Label(nameStyles).setText(player).setConstraint(AxisLayout.stretched()),
                    s = new Label(numberStyles),
                    p = new Label(numberStyles).setIcon(Atlantis.media.getPiecensImage(),
                                                        Atlantis.media.getPiecenBounds(pidx++))));
            scores.add(Value.create(0));
            scores.get(scores.size()-1).map(Functions.TO_STRING).connectNotify(s.textSlot());
            piecens.add(Value.create(0));
            piecens.get(piecens.size()-1).map(Functions.TO_STRING).connectNotify(p.textSlot());
        }

        _root.packToWidth(WIDTH);
    }

    public void setNextTile (Glyphs.Play tile) {
        // TODO: _nextLabel.layer.setVisible(tile != null);
        Point ppos = Coords.layerToParent(_curtile.layer, _root.layer, 0, 0, new Point());
        tile.layer.setTranslation(ppos.x + Media.TERRAIN_WIDTH/2,
                                  ppos.y + Media.TERRAIN_HEIGHT/2);
        // use an animation to add and fade the tile in, this will ensure that we're sequenced
        // properly with end-of-previous-turn animations
        _screen.anim.add(_root.layer, tile.layer);
        _screen.anim.tweenAlpha(tile.layer).easeOut().from(0).to(1).in(500);
        // the layer will be removed for us when the tile is played
    }

    protected float playerRowPos (int playerIdx) {
        Element pinfo = _pgroup.childAt(playerIdx);
        Point ppos = Coords.layerToParent(pinfo.layer, _root.layer, 0, 0, new Point());
        return ppos.y - 2;
    }

    protected Slot<Integer> _highlightTurnHolder = new Slot<Integer>() {
        public void onEmit (Integer turnHolder) {
            _turnHolder.setVisible(turnHolder >= 0);
            if (turnHolder >= 0) {
                _screen.anim.tweenY(_turnHolder).easeInOut().to(playerRowPos(turnHolder)).in(500L);
            }
        }
    };

    protected GameScreen _screen;
    protected Root _root;
    protected CanvasLayer _turnHolder;
    protected float _playersY, _nextTileY;
    protected Group _pgroup;
    protected Label _curtile;

    protected static final int WIDTH = 180;
}
