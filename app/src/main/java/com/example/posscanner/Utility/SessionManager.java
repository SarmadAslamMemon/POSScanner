package com.example.posscanner.Utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.posscanner.Activities.HomeScreen;

public class SessionManager {

    private static final String PREF_NAME = "MyAppPreferences";


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    // Save API response data
    public void saveJsonString(String jsonString) {

        editor.putString("jsonString", jsonString);

        editor.apply();
    }

    public String getJSonString() {
        return preferences.getString("jsonString", "");
    }

    public void clearJsonString()
    {
        editor.remove("jsonString");
        editor.apply();


    }



}