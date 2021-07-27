package com.herro.mainumiplay;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    Context context;
    private static final String APP_PREFERENCES = "config";
    private static final String APP_PREFERENCES_L_Link = "link";
    private static final String APP_PREFERENCES_key = "key";

    private SharedPreferences mSettings;

    public Prefs(Context context) {
        this.context = context;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String  getLink(){
        return mSettings.getString(APP_PREFERENCES_L_Link,"-111");
    }

    public void setLink(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_L_Link, id);
        editor.apply();
    }

    public String  getKey(){
        return mSettings.getString(APP_PREFERENCES_key,"-111");
    }

    public void setKey(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_key, id);
        editor.apply();
    }

}
