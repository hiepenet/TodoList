package com.smarttodo.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class đại diện cho một Task trong ứng dụng.
 * Lưu trữ trên Firestore collection "tasks".
 * Priority: 1=High, 2=Medium, 3=Low
 */
public class Task {

    // Priority constants
    public static final int PRIORITY_HIGH   = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW    = 3;

    // Recurrence constants
    public static final int RECURRENCE_NONE    = 0;
    public static final int RECURRENCE_DAILY   = 1;
    public static final int RECURRENCE_WEEKLY  = 2;
    public static final int RECURRENCE_MONTHLY = 3;
    public static final int RECURRENCE_CUSTOM  = 4;

    @DocumentId
    private String taskId;

    private String userId;
    private String title;
    private String description;
    private String categoryId;
    private int priority;       // 1=High, 2=Medium, 3=Low
    private String imageUrl;    // URL ảnh từ Firebase Storage (có thể null)
    private Date deadline;      // Hạn chót
    private Date reminderTime;  // Thời gian nhắc nhở (có thể null)
    private boolean completed;

    // Subtask progress (cached trên task document)
    private int subtaskCount;       // Tổng số subtask
    private int subtaskCompleted;   // Số subtask đã hoàn thành

    // Recurrence
    private int recurrenceType;              // 0=None, 1=Daily, 2=Weekly, 3=Monthly, 4=Custom
    private int recurrenceInterval;          // Mỗi N ngày/tuần/tháng (mặc định 1)
    private List<Integer> recurrenceDays;    // Ngày trong tuần cho WEEKLY (1=Mon,...,7=Sun)

    @ServerTimestamp
    private Date createdAt;
    
    @ServerTimestamp
    private Date updatedAt;

    // Soft delete timestamp
    private Date deletedAt;

    // Constructor rỗng bắt buộc cho Firestore deserialization
    public Task() {}

    public Task(String userId, String title, String description,
                String categoryId, int priority, Date deadline) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.priority = priority;
        this.deadline = deadline;
        this.completed = false;
    }

    // ========== Getters & Setters ==========

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public Date getReminderTime() { return reminderTime; }
    public void setReminderTime(Date reminderTime) { this.reminderTime = reminderTime; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public int getSubtaskCount() { return subtaskCount; }
    public void setSubtaskCount(int subtaskCount) { this.subtaskCount = subtaskCount; }

    public int getSubtaskCompleted() { return subtaskCompleted; }
    public void setSubtaskCompleted(int subtaskCompleted) { this.subtaskCompleted = subtaskCompleted; }

    public int getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(int recurrenceType) { this.recurrenceType = recurrenceType; }

    public int getRecurrenceInterval() { return recurrenceInterval; }
    public void setRecurrenceInterval(int recurrenceInterval) { this.recurrenceInterval = recurrenceInterval; }

    public List<Integer> getRecurrenceDays() { return recurrenceDays; }
    public void setRecurrenceDays(List<Integer> recurrenceDays) { this.recurrenceDays = recurrenceDays; }

    /**
     * Kiểm tra task có bị quá hạn không
     */
    public boolean isOverdue() {
        if (deadline == null || completed) return false;
        return deadline.before(new Date());
    }

    /**
     * Lấy tên priority dạng String
     */
    public String getPriorityName() {
        switch (priority) {
            case PRIORITY_HIGH:   return "Cao";
            case PRIORITY_MEDIUM: return "Trung bình";
            case PRIORITY_LOW:    return "Thấp";
            default:              return "Không xác định";
        }
    }

    /**
     * Kiểm tra task có lặp lại không
     */
    public boolean isRecurring() {
        return recurrenceType != RECURRENCE_NONE;
    }

    /**
     * Lấy mô tả kiểu lặp lại dạng String
     */
    public String getRecurrenceText() {
        switch (recurrenceType) {
            case RECURRENCE_DAILY:   return "Hàng ngày";
            case RECURRENCE_WEEKLY:  return "Hàng tuần";
            case RECURRENCE_MONTHLY: return "Hàng tháng";
            case RECURRENCE_CUSTOM:
                if (recurrenceInterval > 1) {
                    return "Mỗi " + recurrenceInterval + " ngày";
                }
                return "Tùy chỉnh";
            default:                 return null;
        }
    }

    /**
     * Kiểm tra task có subtask không
     */
    public boolean hasSubtasks() {
        return subtaskCount > 0;
    }
}
