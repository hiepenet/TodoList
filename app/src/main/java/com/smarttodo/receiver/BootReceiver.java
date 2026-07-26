package com.smarttodo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.model.Task;
import com.smarttodo.notification.NotificationHelper;
import com.smarttodo.repository.TaskRepository;
import com.smarttodo.listener.OnTaskListener;

import java.util.List;

/**
 * BroadcastReceiver nhận ACTION_BOOT_COMPLETED.
 * Khi máy khởi động lại, đăng ký lại tất cả các Alarm Reminder đang pending.
 * Điều này đảm bảo notifications không bị mất sau khi tắt/bật máy.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }

        // Kiểm tra user đã đăng nhập chưa
        if (!FirebaseManager.getInstance().isUserLoggedIn()) return;

        // Lấy tất cả task có reminder và đặt lại alarm
        rescheduleReminders(context);
    }

    /**
     * Lấy danh sách task có reminderTime trong tương lai và đặt lại alarm
     */
    private void rescheduleReminders(Context context) {
        TaskRepository taskRepository = new TaskRepository();
        NotificationHelper notificationHelper = new NotificationHelper(context);

        taskRepository.getTasksWithReminder(new OnTaskListener.OnTasksLoaded() {
            @Override
            public void onSuccess(List<Task> tasks) {
                for (Task task : tasks) {
                    notificationHelper.scheduleReminder(task);
                }
            }

            @Override
            public void onFailure(String error) {
                // Không làm gì - có thể log nếu cần
            }
        });
    }
}
