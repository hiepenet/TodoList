package com.smarttodo.utils;

/**
 * Các hằng số được sử dụng trong toàn bộ ứng dụng.
 * Tuân theo SOLID - Single Responsibility Principle.
 */
public final class Constants {

    private Constants() {} // Ngăn khởi tạo

    // ========== Firestore Collections ==========
    public static final String COLLECTION_USERS      = "users";
    public static final String COLLECTION_TASKS      = "tasks";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_SUBTASKS   = "subtasks";

    // ========== Firestore Fields ==========
    public static final String FIELD_USER_ID       = "userId";
    public static final String FIELD_TITLE         = "title";
    public static final String FIELD_CATEGORY_ID   = "categoryId";
    public static final String FIELD_PRIORITY      = "priority";
    public static final String FIELD_COMPLETED     = "completed";
    public static final String FIELD_DEADLINE      = "deadline";
    public static final String FIELD_CREATED_AT    = "createdAt";
    public static final String FIELD_UPDATED_AT    = "updatedAt";
    public static final String FIELD_REMINDER_TIME = "reminderTime";
    public static final String FIELD_ORDER              = "order";
    public static final String FIELD_SUBTASK_COUNT      = "subtaskCount";
    public static final String FIELD_SUBTASK_COMPLETED  = "subtaskCompleted";
    public static final String FIELD_RECURRENCE_TYPE     = "recurrenceType";
    public static final String FIELD_RECURRENCE_INTERVAL = "recurrenceInterval";
    public static final String FIELD_RECURRENCE_DAYS     = "recurrenceDays";

    // ========== Firebase Storage Paths ==========
    public static final String STORAGE_TASKS   = "tasks/";
    public static final String STORAGE_AVATARS = "avatars/";

    // ========== SharedPreferences Keys ==========
    public static final String PREF_NAME         = "SmartTodoPrefs";
    public static final String PREF_USER_ID      = "userId";
    public static final String PREF_USER_NAME    = "userName";
    public static final String PREF_USER_EMAIL   = "userEmail";
    public static final String PREF_USER_AVATAR  = "userAvatar";
    public static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    // ========== Intent Extra Keys ==========
    public static final String EXTRA_TASK_ID     = "taskId";
    public static final String EXTRA_CATEGORY_ID = "categoryId";
    public static final String EXTRA_TASK        = "task";

    // ========== Notification ==========
    public static final String NOTIFICATION_CHANNEL_ID   = "smart_todo_reminder";
    public static final String NOTIFICATION_CHANNEL_NAME = "Task Reminder";
    public static final int    NOTIFICATION_REQUEST_CODE = 1000;

    // ========== AlarmManager ==========
    public static final String ACTION_TASK_REMINDER = "com.smarttodo.ACTION_TASK_REMINDER";
    public static final String EXTRA_TASK_TITLE     = "taskTitle";
    public static final String EXTRA_TASK_DESC      = "taskDesc";
    public static final String EXTRA_NOTIFICATION_ID = "notificationId";

    // ========== Request Codes ==========
    public static final int REQUEST_CAMERA  = 100;
    public static final int REQUEST_GALLERY = 101;
    public static final int REQUEST_PERMISSION_CAMERA   = 200;
    public static final int REQUEST_PERMISSION_STORAGE  = 201;
    public static final int REQUEST_PERMISSION_NOTIFICATION = 202;

    // ========== Default Categories ==========
    public static final String[] DEFAULT_CATEGORY_NAMES = {
            "Work", "Study", "Personal", "Shopping", "Travel", "Health"
    };
    public static final String[] DEFAULT_CATEGORY_COLORS = {
            "#2196F3", "#9C27B0", "#FF9800", "#E91E63", "#00BCD4", "#4CAF50"
    };

    // ========== Date Formats ==========
    public static final String DATE_FORMAT_DISPLAY  = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_DATE     = "dd/MM/yyyy";
    public static final String DATE_FORMAT_TIME     = "HH:mm";
    public static final String DATE_FORMAT_MONTH    = "MM/yyyy";

    // ========== Priority ==========
    public static final int PRIORITY_HIGH   = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW    = 3;
}
