package com.example.langlock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

import static com.example.langlock.Misc.CURRENT_LANG_FROM;
import static com.example.langlock.Misc.CURRENT_LANG_TO;
import static com.example.langlock.Misc.DEBUG_TAG;
import static com.example.langlock.Misc.RUN_SERVICE;


public class MainActivity extends AppCompatActivity{
    private WordsRecycleViewAdapter      adapter;
    private OrderedRealmCollection<Word> words;
    private Realm                        realm;
    private RecyclerView                 recyclerView;
    private Menu                         menu;
    private SearchView                   searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        realm = Realm.getDefaultInstance();
        recyclerView = findViewById(R.id.words_recycler_view);
        setUpRecyclerView();

        if(!RUN_SERVICE){
            return;
        }

        Intent serviceIntent = new Intent(this, LockScreenService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu){
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        menu.setGroupVisible(R.id.group_normal_mode, true);
        menu.setGroupVisible(R.id.group_delete_mode, false);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        ImageView searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchIcon.setImageDrawable(ContextCompat.getDrawable(App.getInstance(),R.drawable.ic_search));
        searchView.setOnSearchClickListener(new SearchView.OnClickListener(){
            @Override
            public void onClick(View v){
                menu.setGroupVisible(R.id.group_normal_mode, false);
                menu.setGroupVisible(R.id.group_delete_mode, false);
                adapter.enableDeletionMode(false);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener(){
            @Override
            public boolean onClose(){
                menu.setGroupVisible(R.id.group_normal_mode, true);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String s){
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s){
                adapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_add:
                Toast.makeText(this, "Добавить слово", Toast.LENGTH_SHORT).show();
                addWord();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_train_mode:
                startActivity(new Intent(this, TrainingActivity.class));
                return true;
            case R.id.action_start_delete_mode:
                adapter.enableDeletionMode(true);
                menu.setGroupVisible(R.id.group_normal_mode, false);
                menu.setGroupVisible(R.id.group_delete_mode, true);
                return true;
            case R.id.action_end_delete_mode:
                DatabaseHelper.deleteWordsAsync(realm, adapter.getCountersToDelete());
                return true;
            case R.id.action_cancel_delete_mode:
                adapter.enableDeletionMode(false);
                menu.setGroupVisible(R.id.group_normal_mode, true);
                menu.setGroupVisible(R.id.group_delete_mode, false);
                return true;
            case R.id.action_help:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addWord(){
        final AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        final LayoutInflater      inflater = this.getLayoutInflater();
        View                      view     = inflater.inflate(R.layout.word_dialog, null);
        builder.setView(view);
        final Dialog dialog = builder.create();

        final EditText editTextWord      = view.findViewById(R.id.editTextWord);
        final EditText editTextTranslate = view.findViewById(R.id.editTextTranslate);
        Button         ok                = view.findViewById(R.id.dialogOk);
        Button         cancel            = view.findViewById(R.id.dialogCancel);
        Button         translate         = view.findViewById(R.id.translateText);

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try{
                    if(editTextWord.getText().toString().isEmpty() ||
                       editTextTranslate.getText().toString().isEmpty()){
                        throw new Exception();
                    }
                    DatabaseHelper.addWordAsync(realm, editTextWord.getText(), editTextTranslate.getText());
                    dialog.dismiss();
                }
                catch (Exception e){
                    Toast.makeText(MainActivity.this, "Заполните поля",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        translate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String text = editTextWord.getText().toString();
                if(text.isEmpty()){
                    Toast.makeText(MainActivity.this, "Заполните поля",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isNetworkAvailable()){
                    Toast.makeText(MainActivity.this, "Подключение к интернету отсутствует",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String translatedText = translate(text, String.format("%s-%s", CURRENT_LANG_FROM, CURRENT_LANG_TO));
                editTextTranslate.setText(translatedText);
            }
        });

        dialog.show();
    }

    private void setUpRecyclerView(){
        adapter = new WordsRecycleViewAdapter(realm.where(Word.class).findAllAsync().sort(Word.FIELD_ID), realm);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper     touchHelper         = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new WordsRecycleViewAdapter.OnItemClickListener(){
            @Override
            public void onEditClick(final Word word) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final LayoutInflater  inflater = MainActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.word_dialog, null);
                builder.setView(view);
                final Dialog dialog = builder.create();

                final EditText wordText = view.findViewById(R.id.editTextWord);
                wordText.setText(word.getWord());
                final EditText translateText = view.findViewById(R.id.editTextTranslate);
                translateText.setText(word.getTranslate());

                Button cancel = view.findViewById(R.id.dialogCancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                Button ok = view.findViewById(R.id.dialogOk);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(wordText.getText().toString().isEmpty() ||
                               translateText.getText().toString().isEmpty()){
                                throw new Exception();
                            }
                            Word updateWord = new Word(
                                    word.getId(),
                                    wordText.getText().toString(),
                                    translateText.getText().toString());
                            DatabaseHelper.updateWordAsync(realm, updateWord);
                            dialog.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Заполните поля",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Button translate = view.findViewById(R.id.translateText);
                translate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = wordText.getText().toString();
                        if(text.isEmpty()){
                            Toast.makeText(MainActivity.this, "Заполните поля",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(!isNetworkAvailable()){
                            Toast.makeText(MainActivity.this, "Подключение к интернету отсутствует",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String translatedText = translate(text, String.format("%s-%s", CURRENT_LANG_FROM, CURRENT_LANG_TO));
                        translateText.setText(translatedText);
                    }
                });

                dialog.show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private String translate(String text, String lang){
        TranslateBackgroundTask task = new TranslateBackgroundTask(App.getInstance().getApplicationContext());
        try{
            String translate = task.execute(text, lang).get();
            return translate;
        }
        catch (ExecutionException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        return "";
    }

    //region help classes
    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback{

        TouchHelperCallback(){
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder holder, int direction){
            Log.e(DEBUG_TAG, "ID " + holder.getItemId() + " AdapterPosition " +
                             holder.getAdapterPosition());
            DatabaseHelper.deleteWordAsync(realm, holder.getItemId());
        }

        @Override
        public boolean isLongPressDragEnabled(){
            return true;
        }
    }
    //endregion
}
