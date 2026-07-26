package com.smarttodo.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.listener.OnTaskListener;
import com.smarttodo.model.Subtask;
import com.smarttodo.model.Task;
import com.smarttodo.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository xử lý tất cả CRUD operations cho Task trên Firestore.
 * Sử dụng LiveData để tự động cập nhật UI khi dữ liệu thay đổi (real-time sync).
 */
public class TaskRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "TaskRepository";

    public TaskRepository() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    private String getUserId() {
        return FirebaseManager.getInstance().getCurrentUserId();
    }

    /**
     * Lấy tất cả task của user, lắng nghe real-time updates.
     * Sắp xếp Java in-memory để không bị lỗi thiếu Index trên Firestore Console.
     */
    public void getAllTasks(MutableLiveData<List<Task>> liveData) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("TaskRepository", "Snapshot error: ", error);
                        return;
                    }
                    if (snapshots == null) return;

                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Task task = doc.toObject(Task.class);
                            if (task != null && task.getDeletedAt() == null) {
                                task.setTaskId(doc.getId());
                                tasks.add(task);
                            }
                        } catch (Exception e) {
                            Log.e("TaskRepository", "Error parsing task: ", e);
                        }
                    }

                    // Sắp xếp theo ngày tạo mới nhất (descending) trực tiếp trong RAM
                    Collections.sort(tasks, (t1, t2) -> {
                        if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
                        if (t1.getCreatedAt() == null) return 1;
                        if (t2.getCreatedAt() == null) return -1;
                        return t2.getCreatedAt().compareTo(t1.getCreatedAt());
                    });

                    liveData.postValue(tasks);
                });
    }

    /**
     * Thêm task mới
     */
    public void addTask(Task task, OnTaskListener.OnTaskOperationComplete listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onFailure("Chưa đăng nhập");
            return;
        }
        task.setUserId(uid);

        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            String id = db.collection(Constants.COLLECTION_TASKS).document().getId();
            task.setTaskId(id);
        }

        db.collection(Constants.COLLECTION_TASKS)
                .document(task.getTaskId())
                .set(task)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Cập nhật task
     */
    public void updateTask(Task task, OnTaskListener.OnTaskOperationComplete listener) {
        if (task.getTaskId() == null) {
            listener.onFailure("Task ID không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title",        task.getTitle());
        updates.put("description",  task.getDescription());
        updates.put("categoryId",   task.getCategoryId());
        updates.put("priority",     task.getPriority());
        updates.put("imageUrl",     task.getImageUrl());
        updates.put("deadline",     task.getDeadline());
        updates.put("reminderTime", task.getReminderTime());
        updates.put("completed",    task.isCompleted());
        updates.put("recurrenceType",     task.getRecurrenceType());
        updates.put("recurrenceInterval", task.getRecurrenceInterval());
        updates.put("recurrenceDays",     task.getRecurrenceDays());
        updates.put("subtaskCount",       task.getSubtaskCount());
        updates.put("subtaskCompleted",   task.getSubtaskCompleted());
        updates.put(Constants.FIELD_UPDATED_AT, com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection(Constants.COLLECTION_TASKS)
                .document(task.getTaskId())
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Soft Delete: Đưa task vào thùng rác thay vì xóa hẳn
     */
    public void deleteTask(String taskId, OnTaskListener.OnTaskOperationComplete listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .update("deletedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Hard Delete: Xóa vĩnh viễn
     */
    public void hardDeleteTask(String taskId, OnTaskListener.OnTaskOperationComplete listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Khôi phục task từ thùng rác
     */
    public void restoreTask(String taskId, OnTaskListener.OnTaskOperationComplete listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .update("deletedAt", null)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Đánh dấu task hoàn thành / chưa hoàn thành
     */
    public void toggleTaskComplete(String taskId, boolean completed,
                                   OnTaskListener.OnTaskOperationComplete listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("completed", completed);
        updates.put(Constants.FIELD_UPDATED_AT, com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lấy task theo ID
     */
    public void getTaskById(String taskId, OnTaskListener.OnTaskLoaded listener) {
        db.collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Task task = snapshot.toObject(Task.class);
                        if (task != null) task.setTaskId(snapshot.getId());
                        listener.onSuccess(task);
                    } else {
                        listener.onFailure("Task không tồn tại");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lấy danh sách task trong thùng rác
     */
    public void getTrashedTasks(MutableLiveData<List<Task>> liveData) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("TaskRepository", "Snapshot error: ", error);
                        return;
                    }
                    if (snapshots == null) return;

                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Task task = doc.toObject(Task.class);
                            if (task != null && task.getDeletedAt() != null) {
                                task.setTaskId(doc.getId());
                                tasks.add(task);
                            }
                        } catch (Exception e) {
                            Log.e("TaskRepository", "Error parsing task: ", e);
                        }
                    }

                    // Sắp xếp theo ngày xóa mới nhất (descending) trực tiếp trong RAM
                    Collections.sort(tasks, (t1, t2) -> {
                        if (t1.getDeletedAt() == null && t2.getDeletedAt() == null) return 0;
                        if (t1.getDeletedAt() == null) return 1;
                        if (t2.getDeletedAt() == null) return -1;
                        return t2.getDeletedAt().compareTo(t1.getDeletedAt());
                    });

                    liveData.postValue(tasks);
                });
    }

    /**
     * Lấy task trong ngày hôm nay (dùng cho Widget)
     */
    public void getTodayTasks(OnTaskListener.OnTasksLoaded listener) {
        String uid = getUserId();
        if (uid == null) return;

        Date startOfDay = com.smarttodo.utils.DateUtils.getStartOfDay(null);
        Date endOfDay   = com.smarttodo.utils.DateUtils.getEndOfDay(null);

        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Task task = doc.toObject(Task.class);
                            if (task != null && task.getDeletedAt() == null && task.getDeadline() != null) {
                                if (task.getDeadline().compareTo(startOfDay) >= 0 && task.getDeadline().compareTo(endOfDay) <= 0) {
                                    task.setTaskId(doc.getId());
                                    tasks.add(task);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onSuccess(tasks);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lấy 3 task gần nhất theo deadline (dùng cho Widget)
     */
    public void getUpcomingTasks(OnTaskListener.OnTasksLoaded listener) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Task task = doc.toObject(Task.class);
                            if (task != null && task.getDeletedAt() == null && !task.isCompleted()) {
                                task.setTaskId(doc.getId());
                                tasks.add(task);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Collections.sort(tasks, (t1, t2) -> {
                        if (t1.getDeadline() == null && t2.getDeadline() == null) return 0;
                        if (t1.getDeadline() == null) return 1;
                        if (t2.getDeadline() == null) return -1;
                        return t1.getDeadline().compareTo(t2.getDeadline());
                    });
                    if (tasks.size() > 3) tasks = tasks.subList(0, 3);
                    listener.onSuccess(tasks);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lấy task có reminder cần đặt lại sau reboot
     */
    public void getTasksWithReminder(OnTaskListener.OnTasksLoaded listener) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Task task = doc.toObject(Task.class);
                            if (task != null && !task.isCompleted() && task.getReminderTime() != null) {
                                if (task.getReminderTime().after(new Date())) {
                                    task.setTaskId(doc.getId());
                                    tasks.add(task);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onSuccess(tasks);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ========== Subtask Operations ==========

    /**
     * Lấy tất cả subtask của một task, lắng nghe real-time.
     */
    public void getSubtasks(String taskId, androidx.lifecycle.MutableLiveData<List<Subtask>> liveData) {
        if (taskId == null) return;

        db.collection(Constants.COLLECTION_TASKS)
                .document(taskId)
                .collection(Constants.COLLECTION_SUBTASKS)
                .orderBy(Constants.FIELD_ORDER)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Subtask snapshot error: ", error);
                        return;
                    }
                    if (snapshots == null) return;

                    List<Subtask> subtasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Subtask sub = doc.toObject(Subtask.class);
                            if (sub != null) {
                                sub.setSubtaskId(doc.getId());
                                subtasks.add(sub);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing subtask: ", e);
                        }
                    }
                    liveData.postValue(subtasks);
                });
    }

    /**
     * Thêm subtask mới và cập nhật count trên task document.
     * Sử dụng WriteBatch để đảm bảo data consistency.
     */
    public void addSubtask(String taskId, Subtask subtask, OnTaskListener.OnTaskOperationComplete listener) {
        if (taskId == null) {
            listener.onFailure("Task ID không hợp lệ");
            return;
        }

        var taskRef = db.collection(Constants.COLLECTION_TASKS).document(taskId);
        var subtaskRef = taskRef.collection(Constants.COLLECTION_SUBTASKS).document();
        subtask.setSubtaskId(subtaskRef.getId());

        var batch = db.batch();
        batch.set(subtaskRef, subtask);
        batch.update(taskRef, Constants.FIELD_SUBTASK_COUNT, com.google.firebase.firestore.FieldValue.increment(1));
        batch.update(taskRef, Constants.FIELD_UPDATED_AT, com.google.firebase.firestore.FieldValue.serverTimestamp());

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Toggle subtask complete/incomplete và cập nhật count.
     */
    public void toggleSubtaskComplete(String taskId, String subtaskId, boolean completed,
                                       OnTaskListener.OnTaskOperationComplete listener) {
        if (taskId == null || subtaskId == null) {
            listener.onFailure("ID không hợp lệ");
            return;
        }

        var taskRef = db.collection(Constants.COLLECTION_TASKS).document(taskId);
        var subtaskRef = taskRef.collection(Constants.COLLECTION_SUBTASKS).document(subtaskId);

        var batch = db.batch();
        batch.update(subtaskRef, "completed", completed);
        int increment = completed ? 1 : -1;
        batch.update(taskRef, Constants.FIELD_SUBTASK_COMPLETED, com.google.firebase.firestore.FieldValue.increment(increment));
        batch.update(taskRef, Constants.FIELD_UPDATED_AT, com.google.firebase.firestore.FieldValue.serverTimestamp());

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Xóa subtask và cập nhật count.
     */
    public void deleteSubtask(String taskId, String subtaskId, boolean wasCompleted,
                               OnTaskListener.OnTaskOperationComplete listener) {
        if (taskId == null || subtaskId == null) {
            listener.onFailure("ID không hợp lệ");
            return;
        }

        var taskRef = db.collection(Constants.COLLECTION_TASKS).document(taskId);
        var subtaskRef = taskRef.collection(Constants.COLLECTION_SUBTASKS).document(subtaskId);

        var batch = db.batch();
        batch.delete(subtaskRef);
        batch.update(taskRef, Constants.FIELD_SUBTASK_COUNT, com.google.firebase.firestore.FieldValue.increment(-1));
        if (wasCompleted) {
            batch.update(taskRef, Constants.FIELD_SUBTASK_COMPLETED, com.google.firebase.firestore.FieldValue.increment(-1));
        }
        batch.update(taskRef, Constants.FIELD_UPDATED_AT, com.google.firebase.firestore.FieldValue.serverTimestamp());

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ========== Recurrence Operations ==========

    /**
     * Tạo task tiếp theo cho recurring task khi được mark complete.
     * Tính deadline mới dựa trên recurrence type và interval.
     */
    public void createNextRecurrence(Task completedTask, OnTaskListener.OnTaskOperationComplete listener) {
        if (completedTask == null || !completedTask.isRecurring()) {
            if (listener != null) listener.onFailure("Task không có recurrence");
            return;
        }

        // Tính deadline mới
        Date nextDeadline = calculateNextDeadline(completedTask);
        Date nextReminder = calculateNextReminder(completedTask, nextDeadline);

        // Tạo task mới
        Task nextTask = new Task();
        String newId = db.collection(Constants.COLLECTION_TASKS).document().getId();
        nextTask.setTaskId(newId);
        nextTask.setUserId(completedTask.getUserId());
        nextTask.setTitle(completedTask.getTitle());
        nextTask.setDescription(completedTask.getDescription());
        nextTask.setCategoryId(completedTask.getCategoryId());
        nextTask.setPriority(completedTask.getPriority());
        nextTask.setDeadline(nextDeadline);
        nextTask.setReminderTime(nextReminder);
        nextTask.setCompleted(false);
        nextTask.setImageUrl(completedTask.getImageUrl());
        // Giữ nguyên recurrence settings
        nextTask.setRecurrenceType(completedTask.getRecurrenceType());
        nextTask.setRecurrenceInterval(completedTask.getRecurrenceInterval());
        nextTask.setRecurrenceDays(completedTask.getRecurrenceDays());
        // Reset subtask counts (subtask không được copy)
        nextTask.setSubtaskCount(0);
        nextTask.setSubtaskCompleted(0);

        addTask(nextTask, listener);
    }

    /**
     * Tính deadline tiếp theo dựa trên recurrence type.
     */
    private Date calculateNextDeadline(Task task) {
        if (task.getDeadline() == null) return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(task.getDeadline());

        int interval = Math.max(1, task.getRecurrenceInterval());

        switch (task.getRecurrenceType()) {
            case Task.RECURRENCE_DAILY:
                cal.add(Calendar.DAY_OF_MONTH, interval);
                break;
            case Task.RECURRENCE_WEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, interval);
                break;
            case Task.RECURRENCE_MONTHLY:
                cal.add(Calendar.MONTH, interval);
                break;
            case Task.RECURRENCE_CUSTOM:
                cal.add(Calendar.DAY_OF_MONTH, interval);
                break;
            default:
                return task.getDeadline();
        }

        // Đảm bảo deadline mới không ở quá khứ
        Date now = new Date();
        while (cal.getTime().before(now)) {
            switch (task.getRecurrenceType()) {
                case Task.RECURRENCE_DAILY:
                case Task.RECURRENCE_CUSTOM:
                    cal.add(Calendar.DAY_OF_MONTH, interval);
                    break;
                case Task.RECURRENCE_WEEKLY:
                    cal.add(Calendar.WEEK_OF_YEAR, interval);
                    break;
                case Task.RECURRENCE_MONTHLY:
                    cal.add(Calendar.MONTH, interval);
                    break;
            }
        }

        return cal.getTime();
    }

    /**
     * Tính reminder mới: giữ nguyên khoảng cách giữa reminder và deadline.
     */
    private Date calculateNextReminder(Task task, Date nextDeadline) {
        if (task.getReminderTime() == null || task.getDeadline() == null || nextDeadline == null) {
            return null;
        }
        long diff = task.getDeadline().getTime() - task.getReminderTime().getTime();
        return new Date(nextDeadline.getTime() - diff);
    }
}
