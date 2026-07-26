package com.smarttodo;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom Application class để khởi tạo các thư viện dùng chung (như Cloudinary)
 * khi ứng dụng vừa mới chạy lên.
 */
public class SmartTodoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dpgrveiqx");
        MediaManager.init(this, config);

        // Áp dụng Dark Mode
        com.smarttodo.utils.ThemeUtils.applyTheme(this);
    }
}
