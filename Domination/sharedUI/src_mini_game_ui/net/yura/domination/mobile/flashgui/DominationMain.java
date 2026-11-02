package net.yura.domination.mobile.flashgui;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import net.yura.domination.audio.SimpleAudio;
import net.yura.domination.audio.GameSound;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskSettings;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mapstore.MapServerClient;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.grasshopper.info.ApplicationInfoProvider;
import net.yura.grasshopper.submitter.BugSubmitter;
import net.yura.grasshopper.info.LogList;
import net.yura.grasshopper.SimpleBug;
import net.yura.lobby.client.PushLobbyClient;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.lobby.model.Game;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.plaf.SynthLookAndFeel;
import net.yura.mobile.gui.plaf.nimbus.NimbusLookAndFeel;
import net.yura.swingme.core.J2SELogger;

/**
 * This class is instantiated by the AndroidMEApp even if there is no AndroidMEActivity
 */
public class DominationMain extends Application {

    private static final Logger logger = Logger.getLogger(DominationMain.class.getName());

    public static final String SAVE_EXTENSION = ".save";

    /**
     * @see net.yura.domination.ui.flashgui.MainMenu#product
     */
    public static final String product;
    public static final String version;
    static {
        if (Application.getPlatform() == Application.PLATFORM_IOS) {
            product = "iOS-GUI";
        }
        else if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
            product = "AndroidGUI";
        }
        else {
            product = "MiniGameGUI";
        }

        String versionCode = System.getProperty("versionCode");
        version = versionCode != null ? versionCode : RiskUtil.RISK_VERSION;
    }

    public static Preferences appPreferences = Preferences.userNodeForPackage(DominationMain.class);
    public GooglePlayGameServices googlePlayGameServices;

    public Risk risk;
    public MiniFlashRiskAdapter adapter;

    public interface GooglePlayGameServices {
	void beginUserInitiatedSignIn();
	void signOut();
	boolean isSignedIn();

	void showAchievements();
        void unlockAchievement(String id);

        /**
         * @deprecated
         */
	void startGameGooglePlay(net.yura.lobby.model.Game game);
        /**
         * @deprecated
         */
	void setLobbyUsername(String username);
        /**
         * @deprecated
         */
	void gameStarted(int id);
    }

    /**
     * setup logging ONLY, do NOT do anything else as we may have not setup things
     * from {@link net.yura.domination.android.GameActivity#onSingleCreate()} yet
     */
    public DominationMain() {

        // IO depends on this, so we need to do this first
        RiskUtil.streamOpener = new RiskMiniIO();

        // get version from AndroidManifest.xml
        //String versionName = System.getProperty("versionName");
        //Risk.RISK_VERSION = versionName!=null ? versionName : "?me4se?";

        try {
            // TODO TranslationBundle.getBundle().getLocale().toString() MAY BE WRONG, WE MAY HAVE A CUSTOM LOCALE SETTING!!
            SimpleBug.initLogFile(RiskUtil.GAME_NAME + " " + product, version, TranslationBundle.getBundle().getLocale().toString());
            BugSubmitter.setApplicationInfoProvider( new ApplicationInfoProvider() {
                /**
                 * info specific to Domination goes here, for general android system info look in:
                 * @see net.yura.android.AndroidMeApp#setSystemProperties()
                 */
                @Override
                public void addInfoForSubmit(Map map) {
                    Risk r = risk;
                    if (r != null) {
                        RiskGame game = r.getGame();
                        if (game != null) {
                            map.put("gameLog", new LogList( game.getCommands() ));
                        }
                    }

                    if (adapter != null && adapter.email != null) {
                        map.put("email", adapter.email);
                    }

                    map.put("lobbyID", MiniLobbyClient.getMyUUID() );
                }
                @Override
                public boolean ignoreError(LogRecord record) {
                    if (RiskUtil.isOldVersion()) {
                        return true;
                    }
                    // if we have already quit the game, any events arriving after may cause errors
                    if (risk != null && DesktopPane.getDesktopPane() == null) {
                        return true;
                    }

                    return false;
                }
            } );
        }
        catch (Throwable th) {
            logger.log(Level.WARNING, "Grasshopper not loaded", th);
        }

        J2SELogger.setupLogging();
    }

    /**
     * This is called AFTER {@link net.yura.domination.android.GameActivity#onSingleCreate()}
     */
    @Override
    public void initialize(DesktopPane rootpane) {

        SynthLookAndFeel synth = null;

        try {
            if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
                synth = (SynthLookAndFeel) Class.forName("net.yura.android.plaf.AndroidLookAndFeel").newInstance();

                // This hack does not seem to be needed (tested on API-22 emulator) and in fact makes things look worse
                // need to work out what android OS actually needs this hack and enable it ONLY for that version of android
                // small hack to center radiobutton icon (the default height is too big, we set it to the width so the icon is square)
                //Style radioButtonStyle = synth.getStyle("RadioButton");
                //Icon radioButtonIcon = (Icon) radioButtonStyle.getProperty("icon", Style.ALL);
                //if (radioButtonIcon != null) {
                //    radioButtonStyle.addProperty(new CentreIcon(radioButtonIcon, radioButtonIcon.getIconWidth(), radioButtonIcon.getIconWidth()), "icon", Style.ALL);
                //}
            }
            if (Application.getPlatform() == Application.PLATFORM_IOS) {
                synth = (SynthLookAndFeel) Class.forName("net.yura.ios.plaf.IOSLookAndFeel").newInstance();
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "can not load theme", ex);
        }

        if (synth == null) {
            synth = new NimbusLookAndFeel();
        }

        InputStream themeData = Application.getResourceAsStream("/dom_synth.xml");
        try {
            synth.load(themeData);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        finally {
            RiskUtil.close(themeData);
        }

        rootpane.setLookAndFeel( synth );

        MapChooser.loadThemeExtension(); // this has theme elements used inside AND outside of the MapChooser



        if ( "true".equals( System.getProperty("debug") ) ) {

            // MWMWMWMWMWMWMWMWMWMWMWM ONLY DEBUG MWMWMMWMWMWMWMWMWMWMWMWMWM

            // this can only work AFTER the setLookAndFeel is called, as before it will fail with no theme
            Logger.getLogger("").addHandler( new Handler() {
                boolean open;
                @Override
                public void publish(LogRecord record) {
                    if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                        if (!open) {
                            open = true;
                            try {
                                // TODO this does not work if the theme is not set yet, it will just throw an exception
                                OptionPane.showMessageDialog(null, record.getMessage()+" "+record.getThrown(), "WARN", OptionPane.WARNING_MESSAGE);
                            }
                            catch(Exception ex) {
                                logger.log(Level.WARNING, "failed to show error", ex);
                            }
                        }
                    }
                }

                @Override public void flush() { }
                @Override public void close() { }
            } );

            // cant do this on J2SE, swing will print too much junk.
            if (Application.getPlatform() != Application.PLATFORM_ME4SE) {
                // if we want to see DEBUG, default is INFO
                Logger.getLogger("").setLevel(java.util.logging.Level.ALL);
            }

            // so we do not need to wait for AI while testing
            net.yura.domination.engine.ai.AIManager.setWait(5);

            // MWMWMWMWMWMWMWMWMWMWM END ONLY DEBUG MWMWMMWMWMWMWMWMWMWMWMWM
        }

        if (appPreferences != null) {

            if (Application.getPlatform() == Application.PLATFORM_ANDROID && !"net.yura.android.AndroidPreferences".equals(appPreferences.getClass().getName())) {
                logger.warning("wrong Preferences class " + appPreferences.getClass());
            }

            String shouldDifferentiateWithoutColor = System.getProperty("shouldDifferentiateWithoutColor");
            if ("true".equalsIgnoreCase(shouldDifferentiateWithoutColor) && !containsKey(RiskSettings.COLOR_BLIND_KEY)) {
                appPreferences.putBoolean(RiskSettings.COLOR_BLIND_KEY, true);
                // on android this does nothing unless we call flushPreferences :-(
                // on all other OSs it sets the property in memory and does not persist it
                // on android this will only work the first time, if the user
                // changes the setting, it will not update in the game after the first time
                if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
                    flushPreferences();
                }
            }

            String lang = appPreferences.get(RiskSettings.LANGUAGE_KEY, null);
            if (lang != null) {
                TranslationBundle.setLanguage(lang);
            }

            String externalMapsUrl = appPreferences.get(RiskSettings.EXTERNAL_MAPS_KEY, null);
            if (externalMapsUrl != null) {
                MiniUtil.setExternalMapDir(externalMapsUrl);
            }
        }
        else {
            System.out.println("can not load appPreferences as it is NULL!");
        }

        risk = new Risk();

        RiskSettings.loadGameSettingsFromPrefs(appPreferences);

        try {
            // setup audio
            if (appPreferences != null) {
                GameSound.INSTANCE.setSoundEnabled(appPreferences.getBoolean(RiskSettings.SOUND_KEY, GameSound.INSTANCE.isSoundEnabled()));
                GameSound.INSTANCE.setMusicEnabled(appPreferences.getBoolean(RiskSettings.MUSIC_KEY, GameSound.INSTANCE.isMusicEnabled()));
            }
            GameSound.INSTANCE.setAudioSystem(risk, new SimpleAudio());
            GameSound.INSTANCE.load("medieval");
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "unable to load medieval audio theme", ex);
        }

        adapter = new MiniFlashRiskAdapter(risk);


        // this needs to run in the UI thread, otherwise 1 thread could be trying to read the auto.save file
        // and another trying to delete it, so we do all actions on the file in the same (UI) thread
        DesktopPane.invokeLater(new Runnable() {
            @Override
            public void run() {
                File autoSaveFile = getAutoSaveFile();
                if (autoSaveFile.exists()) {
                    logger.info("Loading from autosave");
                    // rename the file before we load it with the game thread so it does not get deleted by another thread
                    RiskUtil.rename(autoSaveFile, new File(autoSaveFile.getParent(),autoSaveFile.getName()+".load"));
                    risk.parser( "loadgame "+getAutoSaveFile()+".load" );
                }
                else {
                    adapter.openMainMenu();

                    if (hasPendingOpenLobby()) {
                        adapter.openLobby();
                    }
                }

                logger.info("UI STARTED");
            }
        });


        new Thread("Update-Version-Check") {
            @Override
            public void run() {

                // TODO check game version for non-PlayStore game
                //RiskUtil.getNewVersionCheck() is not good enough for android as uses RiskUtil.RISK_VERSION

                MapUpdateService.getInstance().init( MiniUtil.getFileList("map"), MapServerClient.MAP_PAGE );
            }
        }.start();

        //risk.parser("newgame");
        //risk.parser("newplayer ai hard blue bob");
        //risk.parser("newplayer ai hard red fred");
        //risk.parser("newplayer ai hard green greg");
        //risk.parser("startgame domination increasing");

//        try {
//            File saves = new File( net.yura.android.AndroidMeApp.getIntance().getFilesDir() ,"saves");
//            File sdsaves = new File("/sdcard/Domination-saves");
//            copyFolder(saves, sdsaves);
//            copyFolder(sdsaves, saves);
//            System.out.println("files"+ Arrays.asList( saves.list() ) );
//        }
//        catch (Exception ex) {
//            RiskUtil.printStackTrace(ex);
//        }
    }

/*
    @Override
    protected void destroyApp(boolean arg0) throws javax.microedition.midlet.MIDletStateChangeException {

        if (risk.getGame()!=null) {
            risk.parser("savegame auto.save");
        }
        risk.kill();
        try { risk.join(); } catch (InterruptedException e) { } // wait for game thread to die

        super.destroyApp(arg0);
    }
*/

    public static void quit() {
        // HACK: if the user hits quit 2 times in a row,
        // the 2nd event may throw a nullpointer as desktopPane is set to null after the 1st
        if (DesktopPane.getDesktopPane() != null) {

            logger.info("UI QUIT");

            Application.exit();
        }
    }

    public static class CentreIcon extends Icon {
        Icon wrappedIcon;
        public CentreIcon(Icon icon,int w,int h) {
            wrappedIcon = icon;
            width = w;
            height = h;
        }
        @Override
        public void paintIcon(Component c, Graphics2D g, int x, int y) {
            // paint real icon in the middle of this icon
            wrappedIcon.paintIcon(c, g, x + (getIconWidth()-wrappedIcon.getIconWidth())/2, y + (getIconHeight()-wrappedIcon.getIconHeight())/2);
        }
    }

    public static boolean getBoolean(String key, boolean deflt) {
        if (appPreferences == null) return deflt;
        return appPreferences.getBoolean(key, deflt);
    }

    public static String getString(String key, String defaultValue) {
        if (appPreferences == null) return defaultValue;
        return appPreferences.get(key, defaultValue);
    }

    public static void setAccounts(List<String> accounts) {
        if (!accounts.isEmpty()) {
            if (appPreferences != null) {
                appPreferences.put("accounts", JavaCompatUtil.listToCsv(accounts, ','));
                flushPreferences();
            }
        }
    }

    public static void setExternalMapsDir(String url) {
        MiniUtil.setExternalMapDir(url);
        if (appPreferences != null) {
            DominationMain.appPreferences.put(RiskSettings.EXTERNAL_MAPS_KEY, url);
            flushPreferences();
        }
    }

    public static String getAccountsString() {
        String accounts = getString("accounts", null);
        // if we accidentally saved an empty string in an old version, remove it
        if ("".equals(accounts)) {
            appPreferences.remove("accounts");
            flushPreferences();
            return null;
        }
        return accounts;
    }

    private static boolean containsKey(String key) {
        try {
            return Arrays.asList(appPreferences.keys()).contains((String)key);
        }
        catch (Exception ex) {
            return false;
        }
    }
    private static void flushPreferences() {
        try {
            appPreferences.flush();
        }
        catch(Exception ex) {
            logger.log(Level.WARNING, "can not flush prefs", ex);
        }
    }

    public void setGooglePlayGameServices(GooglePlayGameServices listener) {
	googlePlayGameServices = listener;
    }
    public static GooglePlayGameServices getGooglePlayGameServices() {
	DominationMain main = (DominationMain) Application.getInstance();
        // main is only null if the app is in the process of shutting down.
	return main == null ? null : main.googlePlayGameServices;
    }

    // ----------------------------- calling native Activity -----------------------------

    private static Map<Integer,ActivityResultListener> nativeCalls = new HashMap();
    private static int nativeCallsCount = 100000; // auto Ids need to start higher then all hard coded ids.

    public interface ActivityResultListener {
        void onActivityResult(Object data);
        void onCanceled();
    }

    public static void openURL(String url, ActivityResultListener listener) {
        nativeCallsCount++;
        url = url + (url.indexOf('?') >= 0 ? "&" : "?") + "requestCode=" + nativeCallsCount;
        nativeCalls.put(nativeCallsCount,listener);
        Application.openURL(url);
    }

    public void onResult(int requestCode, int resultCode, Object obj) {
        ActivityResultListener listener = nativeCalls.remove(requestCode);
        if (listener != null) {
            if (resultCode == -1) { // Activity.RESULT_OK
                listener.onActivityResult(obj);
            }
            else if (resultCode == 0) { // Activity.RESULT_CANCELED
                listener.onCanceled();
            }
            else {
                logger.warning("unknown resultCode "+resultCode);
            }
        }
    }

    // ----------------------------- handle open from notification -----------------------------

    private Game pendingOpenGame;

    public boolean hasPendingOpenLobby() {
        return pendingOpenGame != null;
    }

    public void lobbyConnected() {
        if (pendingOpenGame != null) {
            adapter.lobby.openGame(pendingOpenGame);
            pendingOpenGame = null;
        }
    }

    public void pushNotificationsToken(String system, final String token) {
        logger.info("Push Token " + system + " " + token);

        // android can give us new token at ANY point, even when we didnt request them
        // so if the app is not ready, we can just ignore this token
        if (adapter == null) {
            return;
        }

        // we only request the token once we have connected
        MiniLobbyClient lobby = adapter.lobby;

        // if the user has closed the lobby by the time we get the token we have nothing we can do
        if (lobby != null) {
            if (Application.getPlatform() == Application.PLATFORM_IOS) {
                lobby.setPushToken(system, token, null);
            }
            else if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
                try {
                    lobby.setPushToken(system, token, new PushLobbyClient() {
                        @Override
                        public void registerDone() {
                            try {
                                Application.getInstance().platformRequest("notify://setRegisteredOnServer/" + token);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                catch (IllegalStateException ise) {
                    logger.log(Level.INFO, "error in setPushToken, prob called too early on android", ise);
                }
            }
        }
    }

    public void openNotification(Map params) {

        String gameId = (String)params.get(PushLobbyClient.GAME_ID);
        String options = (String)params.get(PushLobbyClient.OPTIONS);

        if (gameId != null && options != null) {
            Game game = new Game();
            game.setId(Integer.parseInt(gameId));
            game.setOptions(options);

            MiniFlashRiskAdapter ui = adapter;
            if (ui != null) {
                if (ui.lobby != null) {
                    if (ui.lobby.whoAmI() != null) {
                        ui.lobby.openGame(game);
                    }
                    else {
                        pendingOpenGame = game;
                        // this happens if the connection is slow and there is time between calling connect and connecting
                        logger.info("lobby open but we are not logged in yet");
                    }
                }
                else {
                    pendingOpenGame = game;
                    ui.openLobby();
                }
            }
            else {
                // the game has not initialized yet
                pendingOpenGame = game;
            }
        }
        else {
            logger.info("opened from notification, but no game info " + params);
        }
    }

    // ----------------------------- GAME SAVE -----------------------------

    private final static String AUTO_SAVE_FILE_NAME = "auto.save";
    public static File getAutoSaveFile() {
        return new File(MiniUtil.getSaveGameDir(), AUTO_SAVE_FILE_NAME);
    }

    public boolean shouldSaveGame() {
        Risk risk = this.risk;
        return risk != null && risk.getGame() != null && risk.getLocalGame();
    }

    /**
     * this is called when the user decides to quit the game, in that situation saveState will NOT be called
     * this is also called when a transparent activity is opened over the game, (e.g. GamePreferenceActivity)
     * in that case, we do not want to waste time saving state, as we know we will not get removed.
     */
    protected void pauseApp() {
        super.pauseApp();

        // if everything is shut down and there is no current game
        // make sure we clean up so no game is loaded on next start

        // TODO we may have been paused WHILE the game is starting,
        // and then we may end up deleting the file we are trying to load.
        if ( !shouldSaveGame() ) {
            File file = getAutoSaveFile();
            if (file.exists()) {
                logger.info("[DominationMain] DELETING AUTOSAVE");
                file.delete();
            }
        }
    }

    public void saveState() {
        // if the system wants to kill our activity we need to save the game if we have one

        if (shouldSaveGame()) {
            logger.info("[DominationMain] SAVING TO AUTOSAVE");
            // in game thread, we do not want to do it there as we will not know when its finished
            //getRisk().parser("savegame "+getAutoSaveFileURL());

            try {
                final Risk risk = this.risk;
                if (risk != null) {
                    final File autoSaveFile = DominationMain.getAutoSaveFile();
                    final File tempSaveFile = new File(autoSaveFile.getParent(), autoSaveFile.getName() + ".part");

                    // WARNING! we may be in the middle of a large annihilation battle, with both sides rolling dice
                    risk.parserAndWait("savegame " + DominationMain.getAutoSaveFile() + ".part");
                    // if we may have closed the game while also closing the activity
                    // the save probably failed, and the rename will fail for sure.

                    // if we have run out of disk space, nothing we can do
                    if (!tempSaveFile.exists() && MiniUtil.getSaveGameDir().getUsableSpace() < 1000) {
                        return;
                    }

                    // check AGAIN in case something changed while we were saving
                    if (shouldSaveGame()) {
                        RiskUtil.rename(tempSaveFile, autoSaveFile);
                    }
                }
            }
            catch (Throwable ex) {
                logger.log(Level.WARNING, "onSaveInstanceState AUTOSAVE Error", ex);
            }
        }
    }
}
