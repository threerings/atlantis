//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client.util;

import forplay.core.CanvasLayer;
import forplay.core.TextFormat;
import forplay.core.TextLayout;
import static forplay.core.ForPlay.*;

import pythagoras.f.Rectangle;

/**
 * Manages a chunk of text; takes care of preserving its position through text changes.
 */
public class TextGlyph
{
    /**
     * Creates a glyph that will render text using the supplied format.
     *
     * @param template a text string that represents the widest/largest text that will ever be
     * displayed in this glyph. This will be used to size the underlying canvas so that it is large
     * enough to hold changing text.
     */
    public static TextGlyph forTemplate (String template, TextFormat format) {
        TextGlyph glyph = new TextGlyph(format);
        TextLayout layout = graphics().layoutText(template, format);
        glyph.createLayer(layout);
        return glyph;
    }

    /**
     * Creates a glyph that will render text using the supplied format.
     *
     * @param width the maximum width to allow for the text. The layer created by this glyph will
     * be the specified width and of height that accommodates a single line of text.
     */
    public static TextGlyph forWidth (int width, TextFormat format) {
        TextGlyph glyph = new TextGlyph(format);
        TextLayout layout = graphics().layoutText("JYyj", format); // hack to get line height
        glyph.createLayer(width, (int)Math.ceil(layout.height()));
        return glyph;
    }

    /**
     * Creates a glyph containing the supplied text.
     */
    public static TextGlyph forText (String text, TextFormat format) {
        return new TextGlyph(format).setText(text);
    }

    /** The current layer in use by this glyph. */
    public CanvasLayer layer;

    /**
     * Sets the text on this layer. Note that the text must be the same width or shorter than the
     * existing text, or than the original template text used to create the glyph. The underlying
     * image on which the text is rendered will not be resized.
     */
    public TextGlyph setText (String text) {
        TextLayout layout = graphics().layoutText(text, _format);
        if (layer == null) {
            createLayer(layout);
        } else {
            layer.canvas().clear();
        }
        float x = _format.align.getX(layout.width(), layer.canvas().width());
        layer.canvas().drawText(layout, x, 0);
        return this;
    }

    public TextGlyph setOriginBottomCenter () {
        layer.setOrigin(layer.canvas().width()/2, layer.canvas().height());
        return this;
    }

    public Rectangle bounds () {
        return new Rectangle(0, 0, layer.canvas().width(), layer.canvas().height());
    }

    protected void createLayer (TextLayout layout) {
        createLayer((int)Math.ceil(layout.width()), (int)Math.ceil(layout.height()));
    }

    protected void createLayer (int width, int height) {
        layer = graphics().createCanvasLayer(width, height);
    }

    protected TextGlyph (TextFormat format) {
        _format = format;
    }

    protected final TextFormat _format;
}
