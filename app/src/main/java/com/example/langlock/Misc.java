package com.example.langlock;

import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

public class Misc {
    public static final String CHANNEL_ID                  = "1";
    public static final String DEBUG_TAG                   = "Логи";
    public static int          CURRENT_VERSION             = Build.VERSION.SDK_INT;
    public static boolean      SHOW_CORRECT_ANSWER         =
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getBoolean(SettingsActivity.showCorrectAnswer, false);
    public static boolean      RUN_SERVICE =
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getBoolean(SettingsActivity.enabledLocker, true);
    public static boolean      USE_FORGETTING_CURVE =
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getBoolean(SettingsActivity.useForgettingCurve, false);
    public static boolean UNLOCK_SCREEN     =
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getBoolean(SettingsActivity.unlockScreen, false);
    public static String  CURRENT_LANG_FROM =
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getString(SettingsActivity.langFrom, "ru");
    public static String  CURRENT_LANG_TO   =
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getString(SettingsActivity.langTo, "en");
    public static int    NUMBER_OF_ANSWERS =
        Integer.valueOf(
            PreferenceManager
            .getDefaultSharedPreferences(App.getInstance())
            .getString(SettingsActivity.numberOfAnswers, "3"));
    public static boolean BLITZ =
            PreferenceManager
                    .getDefaultSharedPreferences(App.getInstance())
                    .getBoolean(SettingsActivity.blitz, false);

    public static int WITHOUT_NAVBAR_FLAG = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    public static Word PREVIOUS_WORD;
}
