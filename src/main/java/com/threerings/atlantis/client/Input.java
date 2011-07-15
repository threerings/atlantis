//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.client;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import forplay.core.ForPlay;
import forplay.core.Layer;
import forplay.core.Pointer;

import pythagoras.f.AffineTransform;
import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;

/**
 * Dispatches user input to the appropriate entity.
 */
public class Input
{
    /** Provides a handle on an action registration. */
    public interface Registration {
        /** Unregisters the action associated with this handle. */
        void cancel ();
    }

    /** Encapsulates an action to be taken on user input. */
    public interface Action {
        /** Called when the user triggers a reactor. */
        void onTrigger ();
    }

    /** Provides fine-grained control over the reaction process for clients that need it. */
    public interface Reactor {
        /** Returns true if this reactor is no longer relevant and should be removed. */
        boolean hasExpired ();
        /** Returns true if the (screen-coordinates) point triggers this reactor. */
        boolean hitTest (IPoint p);
        /** Executes the reactor's action. */
        void onTrigger ();
    }

    /** Implements reaction for a layer and a bounding box. */
    public static abstract class LayerReactor implements Reactor {
        public LayerReactor (Layer layer, IRectangle bounds) {
            _layer = layer;
            _bounds = bounds;
        }

        @Override public boolean hasExpired () {
            return _layer.parent() == null;
        }

        @Override public boolean hitTest (IPoint p) {
            if (!_layer.visible()) return false;
            // compute the transform from screen coordinates to this layer's coordinates and then
            // check that the point falls in the (layer transform relative) bounds
            return _bounds.contains(inverseTransform(_layer, p, new Point()));
        }

        protected Layer _layer;
        protected IRectangle _bounds;
    }

    public Input () {
        ForPlay.pointer().setListener(new Pointer.Listener() {
            @Override public void onPointerStart (float x, float y) {
                // see if any of our reactors consume this click
                Point p = new Point(x, y);
                // take a snapshot of the reactors list at the start of the click to avoid
                // concurrent modification if reactors are added or removed during processing
                for (Reactor r : Lists.newArrayList(Lists.reverse(_reactors))) {
                    if (r.hasExpired()) {
                        _reactors.remove(r);
                    } else if (r.hitTest(p)) {
                        r.onTrigger();
                        return;
                    }
                }

                // if no reactors consume the click, potentially start a BPL
                for (int ii = _listeners.size()-1; ii >= 0; ii--) {
                    BPL bpl = _listeners.get(ii);
                    if (bpl.bounds.contains(x, y)) {
                        _activeBPL = bpl;
                        _activeBPL.listener.onPointerStart(x, y);
                        break;
                    }
                }
            }

            @Override public void onPointerDrag (float x, float y) {
                if (_activeBPL != null) {
                    _activeBPL.listener.onPointerDrag(x, y);
                }
            }

            @Override public void onPointerEnd (float x, float y) {
                if (_activeBPL != null) {
                    _activeBPL.listener.onPointerEnd(x, y);
                    _activeBPL = null;
                }
            }

            protected boolean _started;
            protected BPL _activeBPL;
        });
    }

    /**
     * Configures a listener to be notified of pointer activity that does not trigger a reactor. On
     * pointer start, such listeners will be scanned from most-recently-registered to
     * least-recently-registered and checked for bounds intersections. Subsequent pointer drag and
     * end events will be dispatched to the listener that matched the pointer start.
     */
    public Registration register (IRectangle bounds, Pointer.Listener listener) {
        final BPL bpl = new BPL(bounds, listener);
        _listeners.add(bpl);
        return new Registration() {
            public void cancel () {
                _listeners.remove(bpl);
            }
        };
    }

    /**
     * Registers an input action to be taken when the user clicks or presses in the supplied
     * bounding rectangle, as transformed by the supplied layer's transform. While the layer in
     * question is not visible, the action will not be triggered.
     *
     * <p> Note: if an action is considered for processing and its layer has been removed from the
     * view hierarchy, the action will automatically be canceled. </p>
     *
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Layer layer, IRectangle bounds, final Action action) {
        return register(new LayerReactor(layer, bounds) {
            public void onTrigger () {
                action.onTrigger();
            }
        });
    }

    /**
     * Registers a reactor. More recently registered reactors will be checked before older
     * reactors, and will thus be preferred in the case of overlap. Presently there's no other way
     * to control overlap behavior.
     *
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (final Reactor reactor) {
        _reactors.add(reactor);
        return new Registration() {
            public void cancel () {
                _reactors.remove(reactor);
            }
        };
    }

    protected static class BPL {
        public final IRectangle bounds;
        public Pointer.Listener listener;
        public BPL (IRectangle bounds, Pointer.Listener listener) {
            this.bounds = bounds;
            this.listener = listener;
        }
    }

    /** A list of all registered reactors. */
    protected List<Reactor> _reactors = Lists.newArrayList();

    /** A list of all registered bounded pointer listeners. */
    protected List<BPL> _listeners = Lists.newArrayList();

    protected static Point inverseTransform (Layer layer, IPoint point, Point into) {
        Layer parent = layer.parent();
        IPoint cur = (parent == null) ? point : inverseTransform(parent, point, into);
        forplay.core.Transform lt = layer.transform();
        _scratch.setTransform(lt.m00(), lt.m10(), lt.m01(), lt.m11(), lt.tx(), lt.ty());
        into = _scratch.inverseTransform(cur, into);
        into.x += layer.originX();
        into.y += layer.originY();
        return into;
    }

    /** A scratch transform, used by {@link #inverseTransform}. */
    protected static AffineTransform _scratch = new AffineTransform();
}
