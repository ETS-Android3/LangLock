package levkaantonov.com.study.langlock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.realm.Realm;

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
                            Misc.RUN_SERVICE = sharedPreferences.getBoolean(key, true);
                            if(Misc.RUN_SERVICE){
                                ContextCompat.startForegroundService(App.getInstance(),
                                        new Intent(App.getInstance(), LockScreenService.class));
                                break;
                            }
                            App.getInstance()
                               .stopService(new Intent(App.getInstance(), LockScreenService.class));
                            break;
                        case(useForgettingCurve):
                            Misc.USE_FORGETTING_CURVE = sharedPreferences.getBoolean(key, true);
                            break;
                        case(showCorrectAnswer):
                            Misc.SHOW_CORRECT_ANSWER = sharedPreferences.getBoolean(key, true);
                            break;
                        case(unlockScreen):
                            Misc.UNLOCK_SCREEN = sharedPreferences.getBoolean(key, true);
                            break;
                        case(langFrom):
                            Misc.CURRENT_LANG_FROM = sharedPreferences.getString(key, "ru");
                            break;
                        case(langTo):
                            Misc.CURRENT_LANG_TO = sharedPreferences.getString(key, "en");
                            break;
                        case(numberOfAnswers):
                            Misc.NUMBER_OF_ANSWERS = Integer.valueOf(sharedPreferences.getString(key, "3"));
                            break;
                        case(blitz):
                            Misc.BLITZ = sharedPreferences.getBoolean(key, true);
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
