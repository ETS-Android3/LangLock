package com.example.langlock;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import io.realm.Realm;

import static com.example.langlock.Misc.NUMBER_OF_ANSWERS;
import static com.example.langlock.Misc.PREVIOUS_WORD;
import static com.example.langlock.Misc.SHOW_CORRECT_ANSWER;

public class TrainingActivity extends AppCompatActivity implements View.OnClickListener{
    private final Random   random = new Random();
    private       Word     correctQuestion;
    private       Word[]   questions;
    private       Button[] answers;
    private       Realm    realm;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_activity);

        realm = Realm.getDefaultInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        makeTest();
    }

    public void makeTest(){
        TextView question      = findViewById(R.id.trainingScreenQuestionTextView);
        TextView questionLabel = findViewById(R.id.trainingScreenQuestionLabel);
        questionLabel.setText("вопрос:");
        TextView answersLabel = findViewById(R.id.trainingScreenAnswersLabel);

        int count = DatabaseHelper.getWordsCount(realm);
        if(count == 0 || count < NUMBER_OF_ANSWERS){
            questionLabel.setText("недостаточно записей в базе данных");
            questionLabel.setVisibility(View.VISIBLE);

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
            return;
        }

        answers = new Button[]{
                findViewById(R.id.trainingScreenButton1),
                findViewById(R.id.trainingScreenButton2),
                findViewById(R.id.trainingScreenButton3),
                findViewById(R.id.trainingScreenButton4),
                findViewById(R.id.trainingScreenButton5)
        };

        questions = DatabaseHelper.getWords(realm);
        correctQuestion = questions[random.nextInt(NUMBER_OF_ANSWERS)];

        question.setText(correctQuestion.getWord());
        question.setVisibility(View.VISIBLE);
        questionLabel.setVisibility(View.VISIBLE);
        answersLabel.setVisibility(View.VISIBLE);

        for (int i = 0; i < NUMBER_OF_ANSWERS; i++) {
            answers[i].setText(questions[i].getTranslate());
            answers[i].setBackground(getDrawable(R.drawable.default_background_training));
            answers[i].setTextColor(getColor(R.color.blackText));
            answers[i].setOnClickListener(this);
            answers[i].setVisibility(View.VISIBLE);
        }
        PREVIOUS_WORD = correctQuestion;
    }

    @Override public void onClick(View v){
        final String correct = correctQuestion.getTranslate();
        if(((Button) v).getText().toString().equals(correct)){
            v.setBackground(getDrawable(R.drawable.success_background));
            ((Button) v).setTextColor(getColor(R.color.whiteText));
            v.setOnClickListener(null);

            new Handler().postDelayed(new Runnable(){
                public void run(){
                    makeTest();
                }
            }, 500);
        } else {
            v.setBackground(getDrawable(R.drawable.failure_background));
            ((Button) v).setTextColor(getColor(R.color.whiteText));
            v.setOnClickListener(null);

            Handler handler = new Handler();

            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if(SHOW_CORRECT_ANSWER){
                        for (Button i : answers) {
                            if(i.getText().toString().equals(correct)){
                                i.setBackground(getDrawable(R.drawable.success_background));
                                i.setTextColor(getColor(R.color.whiteText));
                            }
                        }
                    }
                }
            }, 400);

            handler.postDelayed(new Runnable(){
                public void run(){
                    makeTest();
                }
            }, 800);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        realm.close();
    }
}
