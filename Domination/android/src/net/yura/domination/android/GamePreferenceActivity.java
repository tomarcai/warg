package net.yura.domination.android;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import net.yura.android.AndroidMeActivity;
import net.yura.android.AndroidMeApp;
import net.yura.domination.audio.GameSound;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskSettings;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.Application;
import net.yura.mobile.logging.Logger;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.View;

public class GamePreferenceActivity extends PreferenceActivity {

    private static ResourceBundle resb = TranslationBundle.getBundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setPreferenceScreen( makePreferenceScreen(getPreferenceManager(),this) );
        }
        else {
            // hack to get rid of strange square on honeycomb
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                ((View)getListView().getParent()).setBackgroundDrawable(null);
            }
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GamePreferenceFragment()).commit();
        }
    }

    private static PreferenceScreen makePreferenceScreen(PreferenceManager man, Context context) {
        PreferenceScreen root = man.createPreferenceScreen(context);

        PreferenceCategory inlinePrefCat = new PreferenceCategory(context);
        inlinePrefCat.setTitle( resb.getString("swing.menu.options") );
        root.addPreference(inlinePrefCat);

        CheckBoxPreference fullscreen = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        fullscreen.setTitle( resb.getString("game.menu.fullscreen") );
        fullscreen.setKey(RiskSettings.FULL_SCREEN_KEY);
        fullscreen.setDefaultValue(GameActivity.getDefaultFullScreen(context));
        inlinePrefCat.addPreference(fullscreen);
        fullscreen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                GameActivity.setGameFullscreen((Boolean)newValue);
                return true;
            }
        });

        CheckBoxPreference show_toasts = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        show_toasts.setTitle( resb.getString("game.menu.showtoasts") );
        show_toasts.setKey(RiskSettings.SHOW_TOASTS_KEY);
        inlinePrefCat.addPreference(show_toasts);

        CheckBoxPreference color_blind = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        color_blind.setTitle( resb.getString("game.menu.colorblind") );
        color_blind.setKey(RiskSettings.COLOR_BLIND_KEY);
        inlinePrefCat.addPreference(color_blind);

        CheckBoxPreference sound = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        sound.setTitle(resb.getString("game.menu.sound"));
        sound.setKey(RiskSettings.SOUND_KEY);
        sound.setDefaultValue(GameSound.INSTANCE.isSoundEnabled());
        sound.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                GameSound.INSTANCE.setSoundEnabled((Boolean)newValue);
                return true;
            }
        });
        inlinePrefCat.addPreference(sound);

        CheckBoxPreference music = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        music.setTitle(resb.getString("game.menu.music"));
        music.setKey(RiskSettings.MUSIC_KEY);
        music.setDefaultValue(GameSound.INSTANCE.isMusicEnabled());
        music.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                GameSound.INSTANCE.setMusicEnabled((Boolean)newValue);
                return true;
            }
        });
        inlinePrefCat.addPreference(music);

        CheckBoxPreference showDice = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        showDice.setTitle(resb.getString("game.menu.showdice"));
        showDice.setKey(RiskSettings.SHOW_DICE_KEY);
        showDice.setDefaultValue(Risk.isShowDice());
        showDice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Risk.setShowDice((Boolean)newValue);
                return true;
            }
        });
        inlinePrefCat.addPreference(showDice);

        final ListPreference ai = new IntListPreference(context);
        ai.setTitle( resb.getString("game.menu.aiSpeed") );
        ai.setKey(RiskSettings.AI_WAIT_KEY);
        final String[] aiSpeeds = new String[] {
                resb.getString("game.menu.aiSpeed.normal"),
                resb.getString("game.menu.aiSpeed.fast"),
                resb.getString("game.menu.aiSpeed.lightning"),
                resb.getString("game.menu.aiSpeed.instant")};
        final String[] aiSpeedsValues = new String[] {"500","300","100","0"};
        ai.setEntries(aiSpeeds);
        ai.setEntryValues(aiSpeedsValues);
        ai.setDefaultValue(aiSpeedsValues[0]);
        ai.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = String.valueOf(newValue);
                AIManager.setWait(Integer.parseInt(value));
                setSummary(ai,value);
                return true;
            }
        });
        inlinePrefCat.addPreference(ai);
        setSummary(ai,ai.getValue());

        final ListPreference lang = new ListPreference(context);
        lang.setTitle( resb.getString("game.menu.language") );
        lang.setKey(RiskSettings.LANGUAGE_KEY);
        Locale[] locales = Locale.getAvailableLocales();
        final String[] languageNames = new String[locales.length];
        final String[] languages = new String[locales.length];
        for (int c=0;c<locales.length;c++) {
            languages[c] = locales[c].toString();
            languageNames[c] = locales[c].getDisplayName();
        }
        lang.setEntries(languageNames);
        lang.setEntryValues(languages);
        lang.setDefaultValue( Locale.getDefault().toString() );
        lang.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setSummary(lang, String.valueOf(newValue));
                scheduleRestart();
                return true;
            }
        });
        inlinePrefCat.addPreference(lang);
        String languageSummary = TranslationBundle.getBundle().getLocale().toString();
        setSummary(lang, "".equals(languageSummary) ? "default (English)" : languageSummary); // english is the default

        return root;
    }

    private static void setSummary(ListPreference prefs, String value) {
	int index = prefs.findIndexOfValue(value);
	if (index >= 0) {
	    prefs.setSummary( prefs.getEntries()[index] );
	}
	else {
	    prefs.setSummary(value);
	    // many wrong values come here (e.g. "_ES" "_EN" "_FR" "_IT" "_PT")
	    System.out.println("value "+value+" not found in "+Arrays.asList(prefs.getEntryValues()));
	}
    }

    private static void scheduleRestart() {

        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {

                    // HACK, for unknown reasons android has stopped calling onSaveInstanceState
                    Application.getInstance().saveState();

                    // works with Android > 10 from https://stackoverflow.com/a/71392776
                    Context ctx = AndroidMeApp.getContext();
                    PackageManager pm = ctx.getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage(ctx.getPackageName());
                    Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
                    ctx.startActivity(mainIntent);
                    //Runtime.getRuntime().exit(0); // this is actually the same thing as System.exit
                    System.exit(0);
                }
                else {
                    Activity activity = AndroidMeActivity.DEFAULT_ACTIVITY;
                    Intent i = activity.getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(i);
                    android.os.Process.killProcess(android.os.Process.myPid());

                    /*
                    // another way of restarting the app, not sure what is better.
                    Activity activity = AndroidMeActivity.DEFAULT_ACTIVITY;
                    android.app.PendingIntent intent = android.app.PendingIntent.getActivity(activity.getBaseContext(), 0, new Intent(activity.getIntent()), activity.getIntent().getFlags());
                    android.app.AlarmManager manager = (android.app.AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                    manager.set(AlarmManager.RTC, System.currentTimeMillis() + 500, intent);
                    System.exit(2);
                    */
                }
            }
        }, 500);
    }
    /*
    // Called only on Honeycomb and later
    @Override
    public void onBuildHeaders(List<Header> target) {
        Header header = new Header();
        header.title = "header title";
        header.fragment = GamePreferenceFragment.class.getName();
        target.add(header);
    }
    */

    public static class GamePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            try {
                setPreferenceScreen(makePreferenceScreen(getPreferenceManager(), getActivity()));
            }
            catch (Exception ex) {
                Logger.warn("can not start GamePreferenceFragment", ex);
            }
        }
    }

    public static class IntListPreference extends ListPreference {

        public IntListPreference(Context context) {
            super(context);
        }
        public IntListPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean persistString(String value) {
            if(value == null) {
                return false;
            } else {
                return persistInt(Integer.valueOf(value));
            }
        }

        @Override
        protected String getPersistedString(String defaultReturnValue) {
            if(getSharedPreferences().contains(getKey())) {
                int intValue = getPersistedInt(0);
                return String.valueOf(intValue);
            } else {
                return defaultReturnValue;
            }
        }
    }
}
