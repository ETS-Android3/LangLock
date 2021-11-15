package levkaantonov.com.study.langlock;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;

import static levkaantonov.com.study.langlock.Misc.BLITZ;
import static levkaantonov.com.study.langlock.Misc.NUMBER_OF_ANSWERS;
import static levkaantonov.com.study.langlock.Misc.USE_FORGETTING_CURVE;
import static levkaantonov.com.study.langlock.Word.FIELD_ID;

public class DatabaseHelper{
    private static volatile DatabaseHelper instance;
    private static final    long           TWENTY_MINUTES_AGO = 20 * 60 * 1000;
    private static final    long           ONE_HOUR_AGO       = 60 * 60 * 1000;
    private static final    long           TWO_HOURS_AGO      = 120 * 60 * 1000;
    private static final    long           FOUR_HOURS_AGO     = 240 * 60 * 1000;
    private static final    long           SIX_HOURS_AGO      = 360 * 60 * 1000;

    //region Methods
    public static DatabaseHelper getInstance(){
        DatabaseHelper localInstance = instance;
        if(localInstance == null){
            synchronized (DatabaseHelper.class) {
                localInstance = instance;
                if(localInstance == null){
                    instance = localInstance = new DatabaseHelper();
                }
            }
        }
        return localInstance;
    }

    public static boolean updateWordAsync(Realm realm, final Word word){
        try{
            realm.executeTransactionAsync(new Realm.Transaction(){
                @Override
                public void execute(Realm realm){
                    Word.update(realm, word);
                }
            });
            return true;
        }
        catch (Exception e){
            Toast.makeText(App.getInstance().getApplicationContext(), "Ошибка базы данных.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean addWordAsync(Realm realm, final Object... args){
        try{
            realm.executeTransactionAsync(new Realm.Transaction(){
                @Override
                public void execute(Realm realm){
                    Word.create(realm, args);
                }
            });
            return true;
        }
        catch (Exception e){
            Toast.makeText(App.getInstance().getApplicationContext(), "Ошибка базы данных.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean deleteWordAsync(Realm realm, final long id){
        try{
            realm.executeTransactionAsync(new Realm.Transaction(){
                @Override
                public void execute(Realm realm){
                    Word.delete(realm, id);
                }
            });
            return true;
        }
        catch (Exception e){
            Toast.makeText(App.getInstance().getApplicationContext(), "Ошибка базы данных.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean deleteWordsAsync(Realm realm, Collection<Integer> ids){
        try{
            final Integer[] idsToDelete = new Integer[ids.size()];
            ids.toArray(idsToDelete);
            realm.executeTransactionAsync(new Realm.Transaction(){
                @Override
                public void execute(Realm realm){
                    for (Integer id : idsToDelete) {
                        Word.delete(realm, id);
                    }
                }
            });
            return true;
        }
        catch (Exception e){
            Toast.makeText(App.getInstance().getApplicationContext(), "Ошибка базы данных.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean eraseDataFromDb(Realm realm){
        try{
            realm.executeTransaction(new Realm.Transaction(){
                @Override public void execute(Realm realm){
                    realm.deleteAll();
                }
            });
            return true;
        }
        catch (Exception e){
            Toast.makeText(App.getInstance().getApplicationContext(), "Ошибка базы данных.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static int getWordsCount(Realm realm){
        return ((int) realm.where(Word.class).count());
    }

    public static Word[] getWords(Realm realm){
        int    count = getWordsCount(realm);
        Word[] words = new Word[NUMBER_OF_ANSWERS];
        Integer[] ids = USE_FORGETTING_CURVE || !BLITZ
                ? getUniqueRandomNumbers(--count, NUMBER_OF_ANSWERS)
                : getUniqueRandomNumbers(count, NUMBER_OF_ANSWERS);
//        Integer[] ids = getUniqueRandomNumbers(count, NUMBER_OF_ANSWERS);
        for (int i = 0; i < NUMBER_OF_ANSWERS; i++) {
            if(USE_FORGETTING_CURVE){
                if(i == 0){
                    words[i] = getWordUsingForgettingCurve(realm);
                } else {
                    assert words[0] != null;
                    words[i] = realm.where(Word.class)
                                    .notEqualTo(FIELD_ID, words[0].getId())
                                    .findAllAsync().get(ids[i]);
                }
            } else {
                words[i] = realm.where(Word.class)
                                .findAllAsync().get(ids[i]);
            }
        }
        return words;
    }

    public static Integer[] getUniqueRandomNumbers(int count, int numbers){
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        for (int i = 0; i < 3; i++) {
            Collections.shuffle(list);
        }
        return list.subList(0, numbers).toArray(new Integer[numbers]);
    }

    public static OrderedRealmCollection<Word> loadWords(Realm realm){
        return realm.where(Word.class).findAllAsync().sort(Word.FIELD_ID);
    }

    public static Word getWordUsingForgettingCurve(Realm realm){
        long currentTime = System.currentTimeMillis();
        List<Word> words = Arrays.asList(
                realm.where(Word.class)
                     .between("lastUse",
                             currentTime - TWENTY_MINUTES_AGO - 6000,
                             currentTime)
                     .sort("lastUse", Sort.ASCENDING)
                     .findFirst(),
                realm.where(Word.class)
                     .between("lastUse",
                             currentTime - ONE_HOUR_AGO - 6000,
                             currentTime - TWENTY_MINUTES_AGO - 6000)
                     .sort("lastUse", Sort.ASCENDING)
                     .findFirst(),
                realm.where(Word.class)
                     .between("lastUse",
                             currentTime - TWO_HOURS_AGO - 6000,
                             currentTime - ONE_HOUR_AGO - 6000)
                     .sort("lastUse", Sort.ASCENDING)
                     .findFirst(),
                realm.where(Word.class)
                     .between("lastUse",
                             currentTime - FOUR_HOURS_AGO - 6000,
                             currentTime - TWO_HOURS_AGO - 6000)
                     .sort("lastUse", Sort.ASCENDING)
                     .findFirst(),
                realm.where(Word.class)
                     .between("lastUse",
                             currentTime - SIX_HOURS_AGO - 6000,
                             currentTime - FOUR_HOURS_AGO - 6000)
                     .sort("lastUse", Sort.ASCENDING)
                     .findFirst(),
                realm.where(Word.class)
                     .lessThan("lastUse",
                             currentTime - SIX_HOURS_AGO - 6000)
                     .sort("lastUse", Sort.ASCENDING)
                     .findFirst());

        if(words.size() == 0){
            Log.e("Логи", "нет слова");
            return null;
        }

        Collections.sort(words,
                new Comparator<Word>(){
                    @Override
                    public int compare(Word w1, Word w2){
                        if(w1 == null || w2 == null)
                            return 0;

                        if(w1.getLastUse() < w2.getLastUse())
                            return -1;
                        else if(w1.getLastUse() > w2.getLastUse())
                            return 1;
                        else
                            return 0;
                    }
                });
        Log.e("Логи", "есть слово");
        return words.get(0);
    }
    //endregion
}
