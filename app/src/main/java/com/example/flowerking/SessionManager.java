package com.example.flowerking;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "FlowerKingSession";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USERNAME = "USERNAME";

    public static void saveUser(Context context, int userId, String username) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_USER_ID, userId).putString(KEY_USERNAME, username).apply();
    }

    public static int getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getInt(KEY_USER_ID, -1);
    }

    public static String getUsername(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_USERNAME, "Usuario");
    }

    public static void logout(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}