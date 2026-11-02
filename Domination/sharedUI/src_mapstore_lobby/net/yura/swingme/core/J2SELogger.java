package net.yura.swingme.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yura Mamyrin
 */
public class J2SELogger extends net.yura.mobile.logging.Logger {

    static final Logger logger = Logger.getLogger(net.yura.mobile.logging.Logger.class.getName());

    static {
        // we already check the level when we log in the SwingME Logger
        // so if we want the java Logger to respect the level of the SwingME logger
        // we tell the java logger to always log everything
        logger.setLevel(Level.ALL);
    }

    public static void setupLogging() {
        // we need to get then reset the level as the net.yura.mobile.logging.Logger constructor resets the value
        int level = net.yura.mobile.logging.Logger.getLevel();
        net.yura.mobile.logging.Logger.setLogger(new J2SELogger());
        net.yura.mobile.logging.Logger.setLevel(level);
    }

    protected synchronized void log(String message, int level) {
        logger.log(getLevel(level), message);
    }

    protected synchronized void log(String error, Throwable throwable, int level) {
        logger.log(getLevel(level), error, throwable);
    }

    /**
     * @see net.yura.grasshopper.ConsoleHandler#getAndroidLevel(java.util.logging.Level)
     */
    private static Level getLevel(int level) {
        switch (level) {
            case net.yura.mobile.logging.Logger.DEBUG: return Level.FINE; // android:Debug
            case net.yura.mobile.logging.Logger.INFO: return Level.INFO; // android:Info
            case net.yura.mobile.logging.Logger.WARN: return Level.WARNING; // android:Warn
            case net.yura.mobile.logging.Logger.ERROR: return Level.SEVERE; // android:Error
            // TODO can not log as this level, it just means OFF or if we can we should make a new Level for it like ASSERT
            case net.yura.mobile.logging.Logger.FATAL: return Level.SEVERE; // android:Error (or should this be android:Assert)
            default: throw new IllegalArgumentException("level: "+level);
        }
    }
}
