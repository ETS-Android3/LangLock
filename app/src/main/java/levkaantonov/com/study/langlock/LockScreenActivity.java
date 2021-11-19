package levkaantonov.com.study.langlock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import io.realm.Realm;


public class LockScreenActivity extends AppCompatActivity implements View.OnClickListener {
    //region Props and fields
    private final Random            random = new Random();
    private Word                    correctQuestion;
    private Word[]                  questions;
    private Button[]                answers;
    private Realm                   realm;
    private static int  blitzCount = 0;
    //endregion

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.lockscreen_activity);
            View layout = findViewById(R.id.lockScreenLayout);
            layout.setSystemUiVisibility(Misc.WITHOUT_NAVBAR_FLAG);

        Log.e(Misc.DEBUG_TAG, "LockScreenActivity onCreate");

        realm = Realm.getDefaultInstance();

        if ((getIntent() != null) && getIntent().hasExtra("kill")
                && (getIntent().getExtras().getInt("kill") == 1)) {
            finish();
        }
        //region Hide nav bar
        if (Misc.CURRENT_VERSION >= Build.VERSION_CODES.KITKAT) {
            hideNavBar();

            this.getWindow()
                    .getDecorView()
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                hideNavBar();
                            }
                        }
                    });
        }
        //endregion

        //region Setting keyguard
        this.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        this.getWindow()
            .addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //endregion

        try {
            StateListener phoneStateListener = new StateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            makeTest();
        } catch (Exception e) {
            Toast
                .makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
                .show();
        }
    }

    @Override
    protected void onDestroy(){
        realm.close();
        super.onDestroy();
    }

    @Override protected void onStop(){
        super.onStop();
        makeTest();
    }

    @Override protected void onStart(){
        super.onStart();
        hideNavBar();
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Misc.CURRENT_VERSION >= Build.VERSION_CODES.KITKAT && hasFocus) {
            hideNavBar();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lockScreenButton1:
            case R.id.lockScreenButton2:
            case R.id.lockScreenButton3:
            case R.id.lockScreenButton4:
            case R.id.lockScreenButton5:
                final String correct = correctQuestion.getTranslate();
                if(((Button) v).getText().toString().equals(correct)){
                    v.setBackground(getDrawable(R.drawable.success_background));
                    ((Button) v).setTextColor(getColor(R.color.whiteText));
                    v.setOnClickListener(null);

                    new Handler().postDelayed(new Runnable(){
                        public void run(){
                            if(Misc.BLITZ && blitzCount < 2){
                                blitzCount++;
                                makeTest();
                            }else {
                                blitzCount = 0;
                                finish();
                            }
                        }
                    }, 500);

                } else {
                    v.setBackground(getDrawable(R.drawable.failure_background));
                    ((Button) v).setTextColor(getColor(R.color.whiteText));
                    v.setOnClickListener(null);

                    Handler handler = new Handler();
                    if(Misc.SHOW_CORRECT_ANSWER){
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                for (Button i : answers) {
                                    if(i.getText().toString().equals(correct)){
                                        i.setBackground(getDrawable(R.drawable.success_background));
                                        i.setTextColor(getColor(R.color.whiteText));
                                    }}}}, 300);
                    }

                    handler.postDelayed(new Runnable(){
                        public void run(){
                            if(Misc.UNLOCK_SCREEN)
                                finish();

                            if(Misc.BLITZ && blitzCount < 2){
                                blitzCount++;
                                makeTest();
                            } else {
                                blitzCount = 0;
                                if(Misc.BLITZ)
                                    finish();
                            }
                        }
                    }, 800);
                }

                Word updatedWord = new Word(
                        correctQuestion.getId(),
                        correctQuestion.getWord(),
                        correctQuestion.getTranslate());
                updatedWord.setLastUse(System.currentTimeMillis());
                DatabaseHelper.updateWordAsync(realm, updatedWord);
                break;
            case R.id.lockScreenCloseButton:
                finish();
                break;
        }
    }

    private void hideNavBar() {
        this.getWindow()
            .getDecorView()
            .setSystemUiVisibility(Misc.WITHOUT_NAVBAR_FLAG);
    }

    public void makeTest(){
        TextView question = findViewById(R.id.lockScreenQuestionTextView);
        TextView questionLabel = findViewById(R.id.lockScreenQuestionLabel);
        questionLabel.setText("вопрос:");
        TextView answersLabel = findViewById(R.id.lockScreenAnswersLabel);
        Button closeButton = findViewById(R.id.lockScreenCloseButton);
        closeButton.setVisibility(View.INVISIBLE);

        int count = DatabaseHelper.getWordsCount(realm);
        if(count == 0 || count < Misc.NUMBER_OF_ANSWERS){
            questionLabel.setText("недостаточно записей в базе данных");
            questionLabel.setVisibility(View.VISIBLE);
            closeButton.setOnClickListener(this);
            closeButton.setVisibility(View.VISIBLE);

            PreferenceManager
                    .getDefaultSharedPreferences(App.getInstance())
                    .edit()
                    .putBoolean(SettingsActivity.enabledLocker, false)
                    .apply();
            PreferenceManager
                    .getDefaultSharedPreferences(App.getInstance())
                    .edit()
                    .putBoolean(SettingsActivity.blitz, false)
                    .apply();

            App.getInstance().stopService(new Intent(App.getInstance(), LockScreenService.class));
            return;
        }

        answers = new Button[]{
                findViewById(R.id.lockScreenButton1),
                findViewById(R.id.lockScreenButton2),
                findViewById(R.id.lockScreenButton3),
                findViewById(R.id.lockScreenButton4),
                findViewById(R.id.lockScreenButton5)
        };

        questions = DatabaseHelper.getWords(realm);
        correctQuestion = Misc.USE_FORGETTING_CURVE
                ? questions[0]
                : questions[random.nextInt(Misc.NUMBER_OF_ANSWERS)];

        question.setText(correctQuestion.getWord());
        question.setVisibility(View.VISIBLE);
        questionLabel.setVisibility(View.VISIBLE);
        answersLabel.setVisibility(View.VISIBLE);

        for (int i = 0; i < Misc.NUMBER_OF_ANSWERS; i++) {
            answers[i].setText(questions[i].getTranslate());
            answers[i].setBackground(getDrawable(R.drawable.default_background));
            answers[i].setTextColor(getColor(R.color.blackText));
            answers[i].setOnClickListener(this);
            answers[i].setVisibility(View.VISIBLE);
        }
        Misc.PREVIOUS_WORD = correctQuestion;
    }

    //region PhoneStateListener
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    finish();
                }
                break;
            }
        }
    }
    //endregion
}
