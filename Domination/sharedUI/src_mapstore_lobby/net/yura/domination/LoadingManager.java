package net.yura.domination;

import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.swingme.core.LoadingScreen;

public class LoadingManager {

    public static void showLoadingScreen(boolean show) {
        if (show) {
            LoadingScreen.show(TranslationBundle.getBundle().getString("mainmenu.loading"));
        }
        else {
            LoadingScreen.hide();
        }
    }
}
