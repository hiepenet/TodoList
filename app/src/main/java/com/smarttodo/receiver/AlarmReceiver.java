package com.smarttodo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smarttodo.notification.NotificationHelper;
import com.smarttodo.utils.Constants;

/**
 * BroadcastReceiver nhận broadcast từ AlarmManager.
 * Khi đến giờ nhắc nhở, hiển thị notification cho user.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Constants.ACTION_TASK_REMINDER.equals(intent.getAction())) {
            return;
        }

        // Lấy thông tin từ Intent
        String taskTitle    = intent.getStringExtra(Constants.EXTRA_TASK_TITLE);
        String taskDesc     = intent.getStringExtra(Constants.EXTRA_TASK_DESC);
        int    notifId      = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, 0);

        // Hiển thị notification
        NotificationHelper helper = new NotificationHelper(context);
        helper.showReminderNotification(
                taskTitle != null ? taskTitle : "Task Reminder",
                taskDesc,
                notifId
        );
    }
}
