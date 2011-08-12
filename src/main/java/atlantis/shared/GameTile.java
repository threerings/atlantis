//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.shared;

/**
 * Contains the metadata for a single game tile. Most of the metadata is encapsulated in the {@link
 * Terrain} enum, but tiles optionally have a shield which is easier to model with this
 * encapsulating class.
 */
public class GameTile
{
    /** This tile's terrain and features. */
    public final Terrain terrain;

    /** Whether the city on this tile (if any) contains a shield. */
    public final boolean hasShield;

    public GameTile (Terrain terrain, boolean hasShield) {
        this.terrain = terrain;
        this.hasShield = hasShield;
    }

    @Override
    public String toString () {
        return terrain + " (" + (hasShield ? "" : "no") + "shield)";
    }

    @Override
    public boolean equals (Object other) {
        if (other == null) return false;
        GameTile otile = (GameTile)other;
        return otile.terrain == terrain && otile.hasShield == hasShield;
    }

    @Override
    public int hashCode () {
        return terrain.hashCode() ^ (hasShield ? 1 : 0);
    }
}
