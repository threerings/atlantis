//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.core.Font;
import playn.core.TextFormat;
import static playn.core.PlayN.*;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;

/**
 * Contains user interface configuration and utilities.
 */
public class UI
{
    /** The font used for score animations. */
    public static final Font SCORE_FONT = graphics().createFont("Helvetica", Font.Style.BOLD, 24);

    /** The font used for buttons and labels and such. */
    public static final Font UI_FONT = graphics().createFont("Helvetica", Font.Style.PLAIN, 18);

    /** Returns a text format for displaying scores in the specified color. */
    public static final TextFormat scoreFormat (int color) {
        return BASE_SCORE_FORMAT.withTextColor(color);
    }

    /** Returns a text format for displaying buttons, labels and such. */
    public static final TextFormat uiFormat (int color) {
        return BASE_UI_FORMAT.withTextColor(color);
    }

    /** Our default stylesheet. */
    public static final Stylesheet stylesheet = createRootSheet();
    protected static Stylesheet createRootSheet () {
        Styles elemStyles = Styles.none().
            add(Style.FONT.is(graphics().createFont("Helvetica", Font.Style.PLAIN, 18)));
        Styles buttonStyles = Styles.none().
            add(Style.BACKGROUND.is(Background.solid(0xFF99CCFF, 5))).
            addSelected(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC, 6, 4, 4, 6)));
        return Stylesheet.builder().
            add(Element.class, elemStyles).
            add(Button.class, buttonStyles).
            create();
    }

    protected static final TextFormat BASE_SCORE_FORMAT = new TextFormat().
        withFont(SCORE_FONT).withEffect(TextFormat.Effect.outline(0xFF000000));
    protected static final TextFormat BASE_UI_FORMAT = new TextFormat().withFont(UI_FONT);
}
