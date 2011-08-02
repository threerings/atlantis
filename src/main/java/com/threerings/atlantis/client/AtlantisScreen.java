//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import com.threerings.anim.Animator;
import com.threerings.game.Screen;

/**
 * Makes some standard services available to all Atlantis screens.
 */
public class AtlantisScreen extends Screen
{
    /** Routes user input to appropriate entities. */
    public final Input input = new Input();

    /** Manages animations on this screen. */
    public final Animator anim = Animator.create();

    @Override // from Screen
    public void wasShown () {
        input.activate();
    }

    @Override // from Screen
    public void wasHidden () {
    }

    @Override // from Screen
    public void wasRemoved () {
        layer.destroy();
    }

    @Override // from Screen
    public void update (float delta) {
        _elapsed += delta;
    }

    @Override // from Screen
    public void paint (float alpha) {
        anim.update(_elapsed + alpha * AtlantisClient.UPDATE_RATE);
    }

    protected float _elapsed;
}
