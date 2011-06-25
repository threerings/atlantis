//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client.util;

import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.ResourceCallback;

/**
 * Provides support for creating tile images.
 */
public class Tiles
{
    /**
     * Creates a tile from the supplied source image. Note: the image must be known to be loaded at
     * the time of this call, otherwise use {@link #createTileDeferred} which will dynamically
     * listen for image loading completion, but is more expensive as a result.
     *
     * @param source the source image, which should contain tile images of uniform width and
     * height.
     * @param tileWidth the width of a tile, in pixels.
     * @param tileHeight the height of a tile, in pixels.
     * @param tileIdx the index of the tile to be used. Tiles are numbered in row-major order, for
     * example:
     * <pre>
     * 0, 1, 2, 3
     * 4, 5, 6, 7
     * </pre>
     */
    public ImageLayer createTile (Image source, int tileWidth, int tileHeight, int tileIdx)
    {
        ImageLayer layer = ForPlay.graphics().createImageLayer();
        initTileLayer(layer, source, tileWidth, tileHeight, tileIdx);
        return layer;
    }

    /**
     * Creates a tile from the supplied source image, which may not yet have completed loading. See
     * {@link #createTile(Image,int,int,int)} for details.
     */
    public ImageLayer createTileDeferred (
        final Image source, final int tileWidth, final int tileHeight, final int tileIdx)
    {
        final ImageLayer layer = ForPlay.graphics().createImageLayer();
        source.addCallback(new ResourceCallback<Image>() {
            public void done (Image rimage) {
                initTileLayer(layer, rimage, tileWidth, tileHeight, tileIdx);
            }
            public void error (Throwable cause) {
                reportError("createTileDeferred failure [img=" + source + "]", cause);
            }
        });
        return layer;
    }

    protected void initTileLayer (
        ImageLayer layer, Image source, int tileWidth, int tileHeight, int tileIdx)
    {
        layer.setImage(source);
        layer.setWidth(tileWidth);
        layer.setHeight(tileHeight);
        int tilesPerRow = (source.width() / tileWidth);
        int row = tileIdx / tilesPerRow, col = tileIdx % tilesPerRow;
        layer.setSourceRect(col * tileWidth, row * tileWidth, tileWidth, tileHeight);
    }

    protected void reportError (String errmsg, Throwable cause)
    {
        ForPlay.log().warn(errmsg, cause);
    }
}
