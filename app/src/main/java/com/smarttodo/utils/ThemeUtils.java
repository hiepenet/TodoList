package com.smarttodo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility quản lý Chế độ Tối (Dark Mode) và Chế độ Sáng (Light Mode).
 * Lưu trữ trạng thái trong SharedPreferences và áp dụng qua AppCompatDelegate.
 */
public class ThemeUtils {

    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_DARK  = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lấy chế độ theme hiện tại đang lưu. Mặc định là THEME_LIGHT.
     */
    public static int getSavedThemeMode(Context context) {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_LIGHT);
    }

    /**
     * Lưu và áp dụng chế độ theme mới.
     */
    public static void setThemeMode(Context context, int themeMode) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, themeMode).apply();
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    /**
     * Kiểm tra xem hiện tại có đang bật Dark Mode không.
     */
    public static boolean isDarkMode(Context context) {
        return getSavedThemeMode(context) == THEME_DARK;
    }

    /**
     * Bật hoặc tắt Dark Mode.
     */
    public static void setDarkMode(Context context, boolean isDark) {
        setThemeMode(context, isDark ? THEME_DARK : THEME_LIGHT);
    }

    /**
     * Áp dụng theme đã lưu khi khởi tạo Activity / Application.
     */
    public static void applyTheme(Context context) {
        int savedMode = getSavedThemeMode(context);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}
