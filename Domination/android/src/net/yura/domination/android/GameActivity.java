package net.yura.domination.android;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.android.AndroidPreferences;
import net.yura.domination.BuildConfig;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.domination.mobile.flashgui.MiniFlashRiskAdapter;
import net.yura.lobby.model.Game;

public class GameActivity extends AndroidMeActivity implements GoogleAccount.SignInListener,DominationMain.GooglePlayGameServices {

    private static final Logger logger = Logger.getLogger(GameActivity.class.getName());

    /**
     * this code needs to not clash with other codes such as the ones in
     * {@link GoogleAccount} 9000
     * {@link RealTimeMultiplayer} 2,3,4
     * {@link AboutActivity#RC_OPEN_DOCUMENT_TREE} 5
     * {@link DominationMain#nativeCallsCount} 100000+
     */
    private static final int RC_REQUEST_ACHIEVEMENTS = 1;

    private GoogleAccount googleAccount;
    private RealTimeMultiplayer realTimeMultiplayer;

    private String pendingAchievement;
    private boolean pendingShowAchievements;
    private net.yura.lobby.model.Game pendingStartGameGooglePlay;
    private boolean pendingSendLobbyUsername;

    /**
     * need to create everything owned by the activity, but the game/static objects may already exist
     */
    @Override
    protected void onSingleCreate() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DominationMain.appPreferences = new AndroidPreferences(preferences);
        System.setProperty("debug", String.valueOf(BuildConfig.DEBUG)); // Temp hack to get around http://b.android.com/52962
        super.onSingleCreate();

        googleAccount = new GoogleAccount(this);

        realTimeMultiplayer = new RealTimeMultiplayer(this, new RealTimeMultiplayer.Lobby() {
            @Override
            public void createNewGame(Game game) {
                getUi().lobby.createNewGame(game);
            }
            @Override
            public void openGame(Game game) {
                getUi().lobby.openGame(game);
            }
            @Override
            public void getUsername() {
                MiniFlashRiskAdapter ui = getUi();
                if (ui.lobby != null) {
                    if (ui.lobby.whoAmI() != null) {
                        realTimeMultiplayer.setLobbyUsername(ui.lobby.whoAmI());
                    }
                    else {
                        pendingSendLobbyUsername = true;
                        logger.warning("lobby open but we do not have a username");
                    }
                }
                else {
                    pendingSendLobbyUsername = true;
                    ui.openLobby();
                }
            }
        });

        googleAccount.addSignInListener(this);
        googleAccount.addSignInListener(realTimeMultiplayer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            TheBackupAgent.backup(this);
        }

        try {
            AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        RiskUtil.setOldVersion();
                    }
                }
            });
        }
        catch (Throwable th) {
            logger.log(Level.INFO, "can not check for updates", th);
        }

        // enable full screen if needed
        checkIfFullScreenNeeded();
        // keep enabling full screen as android seems to always want to come out of this mode
        // https://stackoverflow.com/a/24004866 "Yuck, Android development is truly horrendous"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    checkIfFullScreenNeeded();
                }
            });
        }

        DominationMain application = (DominationMain)AndroidMeApp.getMIDlet();
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            application.setGooglePlayGameServices(this);
        }

        new Thread("DominationMain.setAccounts") {
            @Override
            public void run() {
                // this code sometimes causes ANRs, so should be in a background thread
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
                    AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
                    Account[] accounts = manager.getAccounts();
                    List<String> emails = new ArrayList();
                    for (Account account: accounts) {
                        String name = account.name;
                        if (name !=null && name.indexOf('@') > 0) {
                            emails.add(name);
                        }
                    }
                    DominationMain.setAccounts(emails);
                }
            }
        }.start();

        Paint paint = new Paint();
        // on some Android devices it does not support gender neutral person
        // so we replace it with person bust in silhouette
        if (!canRender(paint, RiskUtil.EMOJI_HUMAN_ALIVE)) {
            RiskUtil.EMOJI_HUMAN_ALIVE = "\ud83d\udc64";
        }
        // replace robot with computer
        if (!canRender(paint, RiskUtil.EMOJI_AI_ALIVE)) {
            RiskUtil.EMOJI_AI_ALIVE = "\ud83d\udcbb";
        }
    }

    public static boolean canRender(Paint paint, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return paint.hasGlyph(text);
        }
        return false;
    }

    private void checkIfFullScreenNeeded() {
        if (DominationMain.appPreferences.getBoolean("fullscreen", getDefaultFullScreen(this))) {
            setGameFullscreen(true);
        }
    }

    public static void setGameFullscreen(boolean fullscreen) {
        Window window = AndroidMeActivity.DEFAULT_ACTIVITY.getWindow();
        if (fullscreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                window.getDecorView().setSystemUiVisibility(uiOptions);
            }
        }
        else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                window.getDecorView().setSystemUiVisibility(0);
            }
        }
    }

    public static boolean getDefaultFullScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("[GameActivity] onResume");

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            googleAccount.signInSilently();
        }

        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
        catch (Exception ex) {
            // sometimes cancelAll throws a SecurityException, internet says just add try/catch
            logger.log(Level.WARNING, "error in onResume", ex);
        }

        checkIfFullScreenNeeded();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleAccount.onActivityResult(requestCode, resultCode, data);
        realTimeMultiplayer.onActivityResult(requestCode, resultCode, data);
    }

    // ----------------------------- GameHelper.GameHelperListener -----------------------------

    @Override
    public void onSignInSucceeded() {
        logger.info("onSignInSucceeded()");
        MiniFlashRiskAdapter ui = getUi();
        if (ui!=null) {

            GoogleSignInAccount signedInAccount = GoogleSignIn.getLastSignedInAccount(this);
            String id = signedInAccount.getId();
            String idToken = signedInAccount.getIdToken();
            String email = signedInAccount.getEmail();

            ui.playGamesStateChanged(id,idToken,email);
        }
        if (pendingAchievement!=null) {
            unlockAchievement(pendingAchievement);
            pendingAchievement=null;
        }
        if (pendingShowAchievements) {
            pendingShowAchievements = false;
            showAchievements();
        }
        if (pendingStartGameGooglePlay != null) {
            startGameGooglePlay(pendingStartGameGooglePlay);
            pendingStartGameGooglePlay = null;
        }

        Games.getPlayersClient(this, GoogleSignIn.getLastSignedInAccount(this)).getCurrentPlayer().addOnSuccessListener(new OnSuccessListener<Player>() {
            @Override
            public void onSuccess(Player player) {

                System.out.println("play games username: " + player.getDisplayName());
            }
        });
    }

    @Override
    public void onSignInFailed() {
        MiniFlashRiskAdapter ui = getUi();
        if (ui!=null) {
            ui.playGamesStateChanged(null,null,null);
        }
        // user must have cancelled signing in, so they must not want to see achievements.
        pendingShowAchievements = false;
    }

    // ----------------------------- GooglePlayGameServices -----------------------------

    @Override
    public void beginUserInitiatedSignIn() {
        googleAccount.startSignInIntent();
    }

    @Override
    public void signOut() {
        realTimeMultiplayer.signOut();
        googleAccount.signOut();
    }

    @Override
    public boolean isSignedIn() {
        return googleAccount.isSignedIn();
    }

    @Override
    public void startGameGooglePlay(net.yura.lobby.model.Game game) {
        logger.info("startGameGooglePlay");
        if (isSignedIn()) {
            realTimeMultiplayer.startGameGooglePlay(game);
        }
        else {
            logger.info("redirecting to sign in");
            pendingStartGameGooglePlay = game;
            beginUserInitiatedSignIn();
        }
    }

    @Override
    public void setLobbyUsername(String username) {
        if (pendingSendLobbyUsername) {
            pendingSendLobbyUsername = false;
            realTimeMultiplayer.setLobbyUsername(username);
        }
    }

    @Override
    public void gameStarted(int id) {
        realTimeMultiplayer.gameStarted(id);
    }

    @Override
    public void showAchievements() {
        // call this to sign out
        //GoogleSignIn.getClient(this, com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

        if (isSignedIn()) {
            Task<Intent> getAchievementsIntent = Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)).getAchievementsIntent();
            getAchievementsIntent.addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_REQUEST_ACHIEVEMENTS);
                }
            });
            getAchievementsIntent.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    logger.log(Level.INFO, "error in getAchievementsIntent", e);
                }
            });
        }
        else {
            pendingShowAchievements = true;
            beginUserInitiatedSignIn();
        }
    }

    @Override
    public void unlockAchievement(String id) {
        if (isSignedIn()) {
            Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .unlock(id);
        }
        else {
            pendingAchievement = id;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ResourceBundle resb = TranslationBundle.getBundle();
                        new AlertDialog.Builder(GameActivity.this)
                                .setTitle(resb.getString("achievement.achievementUnlocked"))
                                .setMessage(resb.getString("achievement.signInToSave"))
                                .setPositiveButton(resb.getString("achievement.signInToSave.ok"), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        beginUserInitiatedSignIn();
                                    }
                                })
                                .setNegativeButton(resb.getString("achievement.signInToSave.cancel"), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .show();
                    }
                    catch (Throwable th) {
                        Level level = GameActivity.this.isFinishing() ? Level.INFO : Level.WARNING;
                        logger.log(level, "error showing Achievement Unlocked UI", th);
                    }
                }
            });
        }
    }

    private MiniFlashRiskAdapter getUi() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        return dmain == null ? null : dmain.adapter;
    }
}
