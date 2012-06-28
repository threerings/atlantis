//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client.util;

import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.TextLayout;
import static playn.core.PlayN.*;

import pythagoras.f.Rectangle;

import tripleplay.util.TextConfig;

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
    public static TextGlyph forTemplate (String template, TextConfig config) {
        TextGlyph glyph = new TextGlyph(config);
        glyph.createLayer(config.layout(template));
        return glyph;
    }

    /**
     * Creates a glyph that will render text using the supplied format.
     *
     * @param width the maximum width to allow for the text. The layer created by this glyph will
     * be the specified width and of height that accommodates a single line of text.
     */
    public static TextGlyph forWidth (int width, TextConfig config) {
        TextGlyph glyph = new TextGlyph(config);
        TextLayout layout = config.layout("JYyj"); // hack to get line height
        glyph.createLayer(width, config.effect.adjustHeight(layout.height()));
        return glyph;
    }

    /**
     * Creates a glyph containing the supplied text.
     */
    public static TextGlyph forText (String text, TextConfig config) {
        return new TextGlyph(config).setText(text);
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
        TextLayout layout = _config.layout(text);
        if (_image == null) {
            createLayer(layout);
        } else {
            _image.canvas().clear();
        }
        _config.render(_image.canvas(), layout, 0, 0);
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
        layer.setImage(_image = _config.createImage(layout));
    }

    protected void createLayer (float width, float height) {
        layer.setImage(_image = graphics().createImage(width, height));
    }

    protected TextGlyph (TextConfig config) {
        _config = config;
    }

    protected CanvasImage _image;
    protected final TextConfig _config;
}
