package com.example.langlock;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Word extends RealmObject{
    //region Fields
    public static final String FIELD_ID = "id";
    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    @PrimaryKey
    private Integer id;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    private String translate;
    public String getTranslate() {
        return translate;
    }
    public void setTranslate(String translate) {
        this.translate = translate;
    }

    private String word;
    public void setWord(String word) {
        this.word = word;
    }
    public String getWord() {
        return word;
    }

    private long lastUse;
    public void setLastUse(long lastUse){
        this.lastUse = lastUse;
    }
    public long getLastUse(){
        return this.lastUse;
    }

    //endregion
    //region Ctors

    public Word() {
    }
    public Word(Integer id, String word, String translate){
        if(id.equals(null)){
            throw new IllegalArgumentException();
        }

        if(word.isEmpty() || translate.isEmpty()){
            throw new IllegalArgumentException();
        }

        this.id = id;
        this.word = word;
        this.translate = translate;
        lastUse = System.currentTimeMillis();
    }

    //endregion
    //region Methods

    public static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }

    static void create(Realm realm, Object... args){
        Word word = realm.createObject(Word.class, increment());
        word.setWord(args[0].toString());
        word.setTranslate(args[1].toString());
    }

    static void delete(Realm realm, long id){
        Word item = realm.where(Word.class).equalTo(FIELD_ID, id).findFirst();
        if (item != null) {
            item.deleteFromRealm();
        }
    }

    static void update(Realm realm, Word word){
        if(word != null){
            realm.copyToRealmOrUpdate(word);
        }
    }
    //endregion
}
