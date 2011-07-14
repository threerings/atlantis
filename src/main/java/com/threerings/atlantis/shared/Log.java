//
// Atlantis - tile laying fun for the whole family!
// https://github.com/threerings/atlantis

package com.threerings.atlantis.shared;

/**
 * Provides logging services that are routed to the appropriate logging destination on the client
 * or server.
 */
public class Log
{
    /**
     * Wires the logging front-end to the logging back-end. See {@link #setImpl}.
     */
    public interface Impl {
        void debug (String message, Throwable t);
        void info (String message, Throwable t);
        void warning (String message, Throwable t);
    }

    /**
     * Configures the logging back-end. This must be called before any code that makes use of the
     * logging services.
     */
    public static void setImpl (Impl impl) {
        _impl = impl;
    }

    /**
     * Logs a debug message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public static void debug (String message, Object... args) {
        log(DEBUG_TARGET, message, args);
    }

    /**
     * Logs an info message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public static void info (String message, Object... args) {
        log(INFO_TARGET, message, args);
    }

    /**
     * Logs a warning message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public static void warning (String message, Object... args) {
        log(WARNING_TARGET, message, args);
    }

    /**
     * Formats and returns the supplied key/value arguments as {@code [key=value, key=value, ...]}.
     */
    public static String format (Object... args) {
        return format(new StringBuilder("["), args).append("]").toString();
    }

    /**
     * Formats the supplied key/value arguments into the supplied string builder as {@code
     * key=value, key=value, ...}.
     * @return the supplied string builder.
     */
    public static StringBuilder format (StringBuilder into, Object... args) {
        for (int ii = 0, ll = args.length/2; ii < ll; ii++) {
            if (ii > 0) {
                into.append(", ");
            }
            into.append(args[2*ii]).append("=").append(args[2*ii+1]);
        }
        return into;
    }

    protected static void log (Target target, String message, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        if (args.length > 1) {
            sb.append(" [");
            format(sb, args);
            sb.append("]");
        }
        Object error = (args.length % 2 == 1) ? args[args.length-1] : null;
        target.log(sb.toString(), (Throwable)error);
    }

    protected static interface Target {
        void log (String message, Throwable t);
    }

    protected static Target DEBUG_TARGET = new Target() {
        public void log (String message, Throwable t) {
            _impl.debug(message, t);
        }
    };
    protected static Target INFO_TARGET = new Target() {
        public void log (String message, Throwable t) {
            _impl.info(message, t);
        }
    };
    protected static Target WARNING_TARGET = new Target() {
        public void log (String message, Throwable t) {
            _impl.warning(message, t);
        }
    };

    protected static Impl _impl = new Impl() {
        public void debug (String message, Throwable t) {
            info(message, t);
        }
        public void info (String message, Throwable t) {
            System.out.println(message);
            if (t != null) {
                t.printStackTrace(System.out);
            }
        }
        public void warning (String message, Throwable t) {
            info(message, t);
        }
    };
}
