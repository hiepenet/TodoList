package com.smarttodo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manager quản lý SharedPreferences để lưu thông tin user local.
 * Singleton pattern để tránh tạo nhiều instance.
 */
public class PreferenceManager {

    private static PreferenceManager instance;
    private final SharedPreferences sharedPreferences;

    private PreferenceManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lấy instance singleton
     */
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }

    // ========== User Info ==========

    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(Constants.PREF_USER_ID, userId).apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(Constants.PREF_USER_ID, null);
    }

    public void saveUserName(String name) {
        sharedPreferences.edit().putString(Constants.PREF_USER_NAME, name).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    public void saveUserEmail(String email) {
        sharedPreferences.edit().putString(Constants.PREF_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.PREF_USER_EMAIL, "");
    }

    public void saveUserAvatar(String avatarUrl) {
        sharedPreferences.edit().putString(Constants.PREF_USER_AVATAR, avatarUrl).apply();
    }

    public String getUserAvatar() {
        return sharedPreferences.getString(Constants.PREF_USER_AVATAR, null);
    }

    // ========== Login State ==========

    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    /**
     * Xóa tất cả preference khi logout
     */
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    // ========== Generic helpers ==========

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
}
