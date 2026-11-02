package net.yura.swingme.core;

import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.ProgressBar;
import net.yura.mobile.gui.components.Window;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.Url;
import javax.microedition.lcdui.Graphics;

public class LoadingScreen {

    private static Window instance;

    public static void show(String message) {

        if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
            Application.openURL("nativeNoResult://net.yura.android.LoadingDialog?message=" + Url.encode(message));
            return;
        }

        if (instance == null) {
            Window window = new Window();
            window.setName("OpaqueDialog");

            final ProgressBar bar = new ProgressBar();
            bar.setIndeterminate(true);

            window.add(bar);
            window.add(new Label(message), Graphics.RIGHT);
            window.pack();
            window.setLocationRelativeTo(null);

            instance = window;
        }

        // this can get called multiple times as we may be opening something from multiple possible locations
        // e.g. 'open file' or 'load from online, then open file'
        if (!instance.isVisible()) {
            instance.setVisible(true);
        }
    }

    /**
     * this can get called multiple times at the same time from different threads
     * e.g. a game has been loaded, but we have also just been disconnected
     */
    public static void hide() {

        if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
            Application app = Application.getInstance();
            if (app != null) {
                try {
                    app.platformRequest("nativeNoResult://net.yura.android.LoadingDialog?command=hide");
                }
                catch (Exception ex) {
                    // if android has closed the activity by the time we want to hide the loading screen, then do nothing
                    if (hasMessage(ex, "AndroidME default activity is null")) {
                        Logger.info("unable to hide!", ex);
                    }
                    else {
                        Logger.warn("unable to hide!", ex);
                    }
                }
            }
            return;
        }

        Window window = instance;
        if (window != null && window.isVisible()) {
            window.setVisible(false);
        }

        instance = null;
    }

    private static boolean hasMessage(Throwable ex, String message) {
        while (ex != null) {
            if (ex.getMessage() != null && ex.getMessage().contains(message)) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }
}
