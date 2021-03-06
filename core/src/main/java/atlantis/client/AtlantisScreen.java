//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package atlantis.client;

import playn.core.PlayN;
import playn.core.Pointer;

import tripleplay.anim.Animator;
import tripleplay.game.Screen;
import tripleplay.ui.Interface;
import tripleplay.util.PointerInput;

/**
 * Makes some standard services available to all Atlantis screens.
 */
public class AtlantisScreen extends Screen
{
    /** Manages animations on this screen. */
    public final Animator anim = Animator.create();

    /** Routes user input to reactors. */
    public final PointerInput input = new PointerInput();

    /** Manages our user interfaces. */
    public final Interface iface = new Interface();

    @Override // from Screen
    public void wasShown () {
        PlayN.pointer().setListener(input.plistener);
    }

    @Override // from Screen
    public void wasHidden () {
    }

    @Override // from Screen
    public void wasRemoved () {
        layer.destroy();
        // TODO: destroy iface?
    }

    @Override // from Screen
    public void update (float delta) {
        _elapsed += delta;
        iface.update(delta);
    }

    @Override // from Screen
    public void paint (float alpha) {
        anim.update(_elapsed + alpha * AtlantisClient.UPDATE_RATE);
        iface.paint(alpha);
    }

    protected float _elapsed;
}
