package net.yura.domination.engine;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

public class RiskSettings {

    private static final Logger logger = Logger.getLogger(RiskUtil.class.getName());

    // global settings
    public static final String SHOW_DICE_KEY = "game.dice.show";
    public static final String AI_WAIT_KEY = "ai.wait";
    public static final String SOUND_KEY = "audio.sound";
    public static final String MUSIC_KEY = "audio.music";
    public static final String SHOW_TOASTS_KEY = "show_toasts";
    public static final String COLOR_BLIND_KEY = "color_blind";
    public static final String FULL_SCREEN_KEY = "fullscreen";
    public static final String LANGUAGE_KEY = "lang";
    public static final String EXTERNAL_MAPS_KEY = "externalMapsDirUrl";

    // default new game settings
    public static final String DEFAULT_GAME_TYPE_KEY = "default.gametype";
    public static final String DEFAULT_CARD_TYPE_KEY = "default.cardtype";
    public static final String DEFAULT_AUTO_PLACE_ALL_KEY = "default.autoplaceall";
    public static final String DEFAULT_RECYCLE_CARDS_KEY = "default.recycle";
    public static final String DEFAULT_MAP_KEY = "default.map";
    public static final String DEFAULT_CARDS_KEY = "default.cards";

    public static Preferences getPreferences(Class uiclass) {
        Preferences prefs = null;
        try {
             prefs = Preferences.userNodeForPackage( uiclass );
        }
        catch(Throwable th) { } // security
        return prefs;
    }

    public static void savePlayers(Risk risk,Class uiclass) {

        Preferences prefs = getPreferences(uiclass);

        if (prefs!=null) {

            List players = risk.getGame().getPlayers();

            for (int cc=1;cc<=RiskGame.MAX_PLAYERS;cc++) {
                String nameKey = "default.player"+cc+".name";
                String colorKey = "default.player"+cc+".color";
                String typeKey = "default.player"+cc+".type";

                String name = "";
                String color = "";
                String type = "";

                Player player = (cc<=players.size())?(Player)players.get(cc-1):null;

                if (player!=null) {
                    name = player.getName();
                    color = ColorUtil.getStringForColor( player.getColor() );
                    type = risk.getType( player.getType() );
                }
                prefs.put(nameKey, name);
                prefs.put(colorKey, color);
                prefs.put(typeKey, type);

            }

            flushPreferences(prefs);
        }
    }

    public static void savePlayers(List players,Class uiclass) {

        Preferences prefs = getPreferences(uiclass);

        if (prefs!=null) {

            for (int cc=1;cc<=RiskGame.MAX_PLAYERS;cc++) {
                String nameKey = "default.player"+cc+".name";
                String colorKey = "default.player"+cc+".color";
                String typeKey = "default.player"+cc+".type";

                String name = "";
                String color = "";
                String type = "";

                String[] player = (cc<=players.size())?(String[])players.get(cc-1):null;

                if (player!=null) {
                    name = player[0];
                    color = player[1];
                    type = player[2];
                }
                prefs.put(nameKey, name);
                prefs.put(colorKey, color);
                prefs.put(typeKey, type);

            }
            flushPreferences(prefs);
        }
    }
    
    
    /**
     * gets settings, first from Preferences, but if not saved, then Risk config (game.ini)
     */
    public static Properties getPlayerSettings(final Risk risk,Class uiclass) {
        final Preferences theprefs = getPreferences(uiclass);
        return new Properties() {
            public String getProperty(String key) {
                String value = risk.getRiskConfig(key);
                if (theprefs!=null) {
                    value = theprefs.get(key, value);
                }
                return value;
            }
        };
    }

    public static void loadPlayers(Risk risk,Class uiclass) {
        if (!risk.isReplay()) {
            Properties playerSettings = getPlayerSettings(risk, uiclass);
            for (int cc=1;cc<=RiskGame.MAX_PLAYERS;cc++) {
                String name = playerSettings.getProperty("default.player"+cc+".name");
                String color = playerSettings.getProperty("default.player"+cc+".color");
                String type = playerSettings.getProperty("default.player"+cc+".type");
                if (name != null && color != null && type != null && !"".equals(name) && !"".equals(color) && !"".equals(type)) {
                    risk.parser("newplayer " + type + " " + color + " " + name);
                }
            }
        }
    }

    public static void saveGameSettings(Preferences appPreferences, String gameTypeCommand, String cardTypeCommand, boolean autoPlaceAllBoolean, boolean recycleCardsBoolean) {
        if (appPreferences != null) {
            appPreferences.put(RiskSettings.DEFAULT_GAME_TYPE_KEY, gameTypeCommand);
            appPreferences.put(RiskSettings.DEFAULT_CARD_TYPE_KEY, cardTypeCommand);
            appPreferences.putBoolean(RiskSettings.DEFAULT_AUTO_PLACE_ALL_KEY, autoPlaceAllBoolean);
            appPreferences.putBoolean(RiskSettings.DEFAULT_RECYCLE_CARDS_KEY, recycleCardsBoolean);
            flushPreferences(appPreferences);
        }
    }

    /**
     * This method HAS to be called AFTER Risk class is instantiated
     * 
     * where do game settings come from?
     *  - default values in most settings
     *  - (passed to app as args, only language, this HAS to be set before Risk is instantiated) 
     *  - Risk class loads game.ini
     *  - loaded from Preferences
     */
    public static void loadGameSettingsFromPrefs(Preferences appPreferences) {
        if (appPreferences != null) {
            AIManager.setWait( appPreferences.getInt(AI_WAIT_KEY, AIManager.getWait()) );
            Risk.setShowDice(appPreferences.getBoolean(SHOW_DICE_KEY, Risk.isShowDice()));
        }
    }

    public static void saveGameSettingsToPrefs(Preferences preferences) {
        if (preferences != null) {

            preferences.putBoolean(SHOW_DICE_KEY, Risk.isShowDice());
            preferences.putInt(AI_WAIT_KEY, AIManager.getWait());

            flushPreferences(preferences);
        }
    }

    private static void flushPreferences(Preferences prefs) {

        // on android this does not work, god knows why
        // whats the point of including a class if its
        // most simple and basic operation does not work?
        try {
            prefs.flush();
        }
        catch(Exception ex) {
            logger.log(Level.INFO, "can not flush prefs", ex);
        }
    }
}
