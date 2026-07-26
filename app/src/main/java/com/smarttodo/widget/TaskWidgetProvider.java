package com.smarttodo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.smarttodo.R;
import com.smarttodo.activity.MainActivity;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.listener.OnTaskListener;
import com.smarttodo.model.Task;
import com.smarttodo.repository.TaskRepository;
import com.smarttodo.utils.DateUtils;

import java.util.List;

/**
 * TaskWidgetProvider - Widget hiện đại hiển thị 3 task gần hạn nhất.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "TaskWidgetProvider";
    public static final String ACTION_REFRESH = "com.smarttodo.ACTION_WIDGET_REFRESH";
    public static final String ACTION_TOGGLE_TASK = "com.smarttodo.ACTION_TOGGLE_WIDGET_TASK";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_IS_COMPLETED = "extra_is_completed";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if (appWidgetIds == null || appWidgetIds.length == 0) return;

        for (int appWidgetId : appWidgetIds) {
            renderWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();

        if (ACTION_REFRESH.equals(action)) {
            refreshAllWidgets(context);
        } else if (ACTION_TOGGLE_TASK.equals(action)) {
            String taskId = intent.getStringExtra(EXTRA_TASK_ID);
            boolean currentCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false);

            if (taskId != null && !taskId.isEmpty()) {
                TaskRepository repo = new TaskRepository();
                repo.toggleTaskComplete(taskId, !currentCompleted, new OnTaskListener.OnTaskOperationComplete() {
                    @Override
                    public void onSuccess() {
                        refreshAllWidgets(context);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Failed to toggle task from widget: " + error);
                    }
                });
            }
        }
    }

    private void refreshAllWidgets(Context context) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, TaskWidgetProvider.class);
            int[] appWidgetIds = manager.getAppWidgetIds(widget);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, manager, appWidgetIds);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing widgets: ", e);
        }
    }

    private void renderWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task);

            // Open app
            Intent openAppIntent = new Intent(context, MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent openAppPending = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_container, openAppPending);

            // Refresh button
            Intent refreshIntent = new Intent(context, TaskWidgetProvider.class);
            refreshIntent.setAction(ACTION_REFRESH);
            PendingIntent refreshPending = PendingIntent.getBroadcast(
                    context, 100, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.btn_refresh_widget, refreshPending);

            // Initial state with today's full date
            String todayStr = "⚡ 3 Task gần hạn nhất";
            try {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, dd/MM/yyyy", new java.util.Locale("vi", "VN"));
                String dStr = sdf.format(cal.getTime());
                if (dStr != null && !dStr.isEmpty()) {
                    dStr = Character.toUpperCase(dStr.charAt(0)) + dStr.substring(1);
                    todayStr = "📅 " + dStr;
                }
            } catch (Exception ignored) {}

            views.setTextViewText(R.id.tv_widget_header, todayStr);
            views.setTextViewText(R.id.tv_widget_empty, "• Đang tải dữ liệu công việc...");
            views.setViewVisibility(R.id.tv_widget_empty, View.VISIBLE);
            views.setViewVisibility(R.id.row_task1, View.GONE);
            views.setViewVisibility(R.id.row_task2, View.GONE);
            views.setViewVisibility(R.id.row_task3, View.GONE);

            appWidgetManager.updateAppWidget(appWidgetId, views);

            // Load upcoming tasks
            if (FirebaseManager.getInstance().isUserLoggedIn()) {
                TaskRepository repo = new TaskRepository();
                repo.getUpcomingTasks(new OnTaskListener.OnTasksLoaded() {
                    @Override
                    public void onSuccess(List<Task> tasks) {
                        try {
                            updateTasksListUI(context, appWidgetManager, appWidgetId, views, tasks);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating tasks UI: ", e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        try {
                            views.setTextViewText(R.id.tv_widget_empty, "• Không có công việc nào sắp tới");
                            views.setViewVisibility(R.id.tv_widget_empty, View.VISIBLE);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        } catch (Exception ignored) {}
                    }
                });
            } else {
                views.setTextViewText(R.id.tv_widget_empty, "• Chưa đăng nhập ứng dụng");
                views.setViewVisibility(R.id.tv_widget_empty, View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in renderWidget: ", e);
        }
    }

    private void updateTasksListUI(Context context, AppWidgetManager manager,
                                   int appWidgetId, RemoteViews views, List<Task> tasks) {
        int[] rowIds      = {R.id.row_task1, R.id.row_task2, R.id.row_task3};
        int[] checkIds    = {R.id.btn_check1, R.id.btn_check2, R.id.btn_check3};
        int[] titleIds    = {R.id.tv_task1, R.id.tv_task2, R.id.tv_task3};
        int[] deadlineIds = {R.id.tv_deadline1, R.id.tv_deadline2, R.id.tv_deadline3};

        if (tasks == null || tasks.isEmpty()) {
            views.setTextViewText(R.id.tv_widget_empty, "🎉 Bạn không có công việc nào cần làm!");
            views.setViewVisibility(R.id.tv_widget_empty, View.VISIBLE);
            views.setViewVisibility(R.id.row_task1, View.GONE);
            views.setViewVisibility(R.id.row_task2, View.GONE);
            views.setViewVisibility(R.id.row_task3, View.GONE);
        } else {
            views.setViewVisibility(R.id.tv_widget_empty, View.GONE);

            for (int i = 0; i < rowIds.length; i++) {
                if (i < tasks.size()) {
                    Task task = tasks.get(i);
                    views.setViewVisibility(rowIds[i], View.VISIBLE);

                    // Check Icon: ✅ (Hoàn thành) hoặc ⚪ (Chưa xong)
                    String checkIcon = task.isCompleted() ? "✅" : "⚪";
                    views.setTextViewText(checkIds[i], checkIcon);

                    // Title
                    views.setTextViewText(titleIds[i], task.getTitle());

                    // Deadline
                    String deadlineStr = "";
                    if (task.getDeadline() != null) {
                        deadlineStr = "⏰ " + DateUtils.formatDateTime(task.getDeadline());
                    } else {
                        deadlineStr = "Không có hạn chót";
                    }
                    views.setTextViewText(deadlineIds[i], deadlineStr);

                    // Toggle intent
                    Intent toggleIntent = new Intent(context, TaskWidgetProvider.class);
                    toggleIntent.setAction(ACTION_TOGGLE_TASK);
                    toggleIntent.putExtra(EXTRA_TASK_ID, task.getTaskId());
                    toggleIntent.putExtra(EXTRA_IS_COMPLETED, task.isCompleted());

                    PendingIntent togglePending = PendingIntent.getBroadcast(
                            context,
                            appWidgetId * 100 + i,
                            toggleIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    views.setOnClickPendingIntent(checkIds[i], togglePending);
                } else {
                    views.setViewVisibility(rowIds[i], View.GONE);
                }
            }
        }

        manager.updateAppWidget(appWidgetId, views);
    }
}
