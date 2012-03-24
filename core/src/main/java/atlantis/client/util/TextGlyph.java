//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client.util;

import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import static playn.core.PlayN.*;

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
    public final ImageLayer layer = graphics().createImageLayer();

    /** Returns the width of this glyph. */
    public float width () {
        return _image.width();
    }

    /** Returns the height of this glyph. */
    public float height () {
        return _image.height();
    }

    /**
     * Sets the text on this layer. Note that the text must be the same width or shorter than the
     * existing text, or than the original template text used to create the glyph. The underlying
     * image on which the text is rendered will not be resized.
     */
    public TextGlyph setText (String text) {
        TextLayout layout = graphics().layoutText(text, _format);
        if (_image == null) {
            createLayer(layout);
        } else {
            _image.canvas().clear();
        }
        float x = _format.align.getX(layout.width(), _image.canvas().width());
        _image.canvas().drawText(layout, x, 0);
        return this;
    }

    public TextGlyph setOriginBottomCenter () {
        layer.setOrigin(_image.canvas().width()/2, _image.canvas().height());
        return this;
    }

    public Rectangle bounds () {
        return new Rectangle(0, 0, _image.canvas().width(), _image.canvas().height());
    }

    protected void createLayer (TextLayout layout) {
        createLayer((int)Math.ceil(layout.width()), (int)Math.ceil(layout.height()));
    }

    protected void createLayer (int width, int height) {
        layer.setImage(_image = graphics().createImage(width, height));
    }

    protected TextGlyph (TextFormat format) {
        _format = format;
    }

    protected CanvasImage _image;
    protected final TextFormat _format;
}
