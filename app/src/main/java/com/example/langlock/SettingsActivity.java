package com.example.langlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import io.realm.Realm;

import static com.example.langlock.Misc.BLITZ;
import static com.example.langlock.Misc.NUMBER_OF_ANSWERS;
import static com.example.langlock.Misc.CURRENT_LANG_FROM;
import static com.example.langlock.Misc.CURRENT_LANG_TO;
import static com.example.langlock.Misc.RUN_SERVICE;
import static com.example.langlock.Misc.UNLOCK_SCREEN;
import static com.example.langlock.Misc.USE_FORGETTING_CURVE;
import static com.example.langlock.Misc.SHOW_CORRECT_ANSWER;


public class SettingsActivity extends AppCompatActivity{
    public static final String                                          enabledLocker      = "enabledLocker";
    public static final String                                          useForgettingCurve = "useForgettingCurve";
    public static final String                                          showCorrectAnswer  = "showCorrectAnswer";
    public static final String                                          unlockScreen       = "unlockScreen";
    public static final String                                             langFrom           = "langFrom";
    public static final String                                             langTo             = "langTo";
    public static final String                                             numberOfAnswers    = "numberOfAnswers";
    public static final String                                             blitz              = "blitz";
    protected static    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static      Realm                                              realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        realm = Realm.getDefaultInstance();

        getFragmentManager()
                .beginTransaction()
                .add(R.id.prefs_content, new SettingsFragment())
                .commit();

       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            Preference button = getPreferenceManager().findPreference("eraseDataBase");
            if (button != null) {
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()  {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        Boolean result =  DatabaseHelper.eraseDataFromDb(realm);
                        if(result){
                            Toast.makeText(App.getInstance().getApplicationContext(), "Удаление...",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
            }

            listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
                @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
                    switch (key){
                        case (enabledLocker):
                            RUN_SERVICE = sharedPreferences.getBoolean(key, true);
                            if(RUN_SERVICE){
                                ContextCompat.startForegroundService(App.getInstance(),
                                        new Intent(App.getInstance(), LockScreenService.class));
                                break;
                            }
                            App.getInstance()
                               .stopService(new Intent(App.getInstance(), LockScreenService.class));
                            break;
                        case(useForgettingCurve):
                            USE_FORGETTING_CURVE = sharedPreferences.getBoolean(key, true);
                            break;
                        case(showCorrectAnswer):
                            SHOW_CORRECT_ANSWER = sharedPreferences.getBoolean(key, true);
                            break;
                        case(unlockScreen):
                            UNLOCK_SCREEN = sharedPreferences.getBoolean(key, true);
                            break;
                        case(langFrom):
                            CURRENT_LANG_FROM = sharedPreferences.getString(key, "ru");
                            break;
                        case(langTo):
                            CURRENT_LANG_TO = sharedPreferences.getString(key, "en");
                            break;
                        case(numberOfAnswers):
                            NUMBER_OF_ANSWERS = Integer.valueOf(sharedPreferences.getString(key, "3"));
                            break;
                        case(blitz):
                            BLITZ = sharedPreferences.getBoolean(key, true);
                            break;
                    }
                }
            };
        }

        @Override
        public void onDestroy(){
            super.onDestroy();
            realm.close();
        }

        @Override public void onResume(){
            super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override public void onPause(){
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}
