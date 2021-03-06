//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.core.Font;
import static playn.core.PlayN.*;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;
import tripleplay.util.TextConfig;

/**
 * Contains user interface configuration and utilities.
 */
public class UI
{
    /** The font used for score animations. */
    public static final Font SCORE_FONT = graphics().createFont("Helvetica", Font.Style.BOLD, 24);

    /** The font used for buttons and labels and such. */
    public static final Font UI_FONT = graphics().createFont("Helvetica", Font.Style.PLAIN, 18);

    /** Returns a text config for displaying scores in the specified color. */
    public static final TextConfig scoreConfig (int color) {
        return BASE_SCORE_CFG.withColor(color);
    }

    /** Returns a text format for displaying buttons, labels and such. */
    public static final TextConfig uiConfig (int color) {
        return BASE_UI_CFG.withColor(color);
    }

    /** Our default stylesheet. */
    public static final Stylesheet stylesheet = createRootSheet();
    protected static Stylesheet createRootSheet () {
        Styles elemStyles = Styles.none().
            add(Style.FONT.is(graphics().createFont("Helvetica", Font.Style.PLAIN, 18)));
        Styles buttonStyles = Styles.none().
            add(Style.BACKGROUND.is(Background.solid(0xFF99CCFF).inset(5))).
            addSelected(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC).inset(6, 4, 4, 6)));
        return Stylesheet.builder().
            add(Element.class, elemStyles).
            add(Button.class, buttonStyles).
            create();
    }

    protected static final TextConfig BASE_SCORE_CFG =
        new TextConfig(0xFFFFFFFF).withFont(SCORE_FONT).withOutline(0xFF000000);
    protected static final TextConfig BASE_UI_CFG =
        new TextConfig(0xFFFFFFFF).withFont(UI_FONT);
}
