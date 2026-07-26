package com.smarttodo.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.smarttodo.R;
import com.smarttodo.activity.MainActivity;
import com.smarttodo.model.Task;
import com.smarttodo.receiver.AlarmReceiver;
import com.smarttodo.utils.Constants;

/**
 * Helper class quản lý Notification và AlarmManager cho Task reminders.
 * Tạo Notification Channel, hiển thị notification, đặt/hủy alarm.
 */
public class NotificationHelper {

    private final Context context;
    private final NotificationManager notificationManager;
    private final AlarmManager alarmManager;

    public NotificationHelper(Context context) {
        this.context             = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager        = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Tạo Notification Channel (bắt buộc từ Android 8.0)
        createNotificationChannel();
    }

    /**
     * Tạo Notification Channel cho Task reminders.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhắc nhở task từ Smart TODO");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Đặt alarm nhắc nhở cho task.
     * @param task Task cần nhắc nhở
     */
    public void scheduleReminder(Task task) {
        if (task == null || task.getReminderTime() == null || task.getTaskId() == null) {
            Log.e("NotificationHelper", "scheduleReminder aborted: task, reminderTime or taskId is null");
            return;
        }

        long triggerTimeMillis = task.getReminderTime().getTime();

        // Không đặt alarm cho thời gian đã qua
        if (triggerTimeMillis <= System.currentTimeMillis()) {
            Log.w("NotificationHelper", "Reminder time is in the past, skipping.");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.ACTION_TASK_REMINDER);
        intent.putExtra(Constants.EXTRA_TASK_ID,    task.getTaskId());
        intent.putExtra(Constants.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(Constants.EXTRA_TASK_DESC,  task.getDescription());

        int requestCode = Math.abs(task.getTaskId().hashCode());
        intent.putExtra(Constants.EXTRA_NOTIFICATION_ID, requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                } else if (alarmManager != null) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (alarmManager != null) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                }
            } else {
                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                }
            }
            Log.d("NotificationHelper", "Reminder scheduled successfully for: " + task.getTitle() + " at " + triggerTimeMillis);
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "SecurityException scheduling exact alarm: ", e);
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error scheduling alarm: ", e);
        }
    }

    /**
     * Hủy alarm nhắc nhở của task
     */
    public void cancelReminder(String taskId) {
        if (taskId == null) return;
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.ACTION_TASK_REMINDER);

        int requestCode = Math.abs(taskId.hashCode());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * Hiển thị notification khi đến giờ nhắc nhở
     */
    public void showReminderNotification(String taskTitle, String taskDesc, int notificationId) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                notificationId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ " + (taskTitle != null ? taskTitle : "Nhắc nhở công việc"))
                .setContentText(taskDesc != null && !taskDesc.isEmpty() ? taskDesc : "Đã đến giờ làm task!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(taskDesc != null && !taskDesc.isEmpty() ? taskDesc : "Đã đến giờ làm task!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 250, 500})
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(0xFF4CAF50)
                .build();

        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
    }
}
