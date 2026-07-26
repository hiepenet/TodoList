package com.smarttodo.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class đại diện cho một Subtask (công việc con) trong ứng dụng.
 * Lưu trữ trên Firestore subcollection "tasks/{taskId}/subtasks".
 */
public class Subtask {

    @DocumentId
    private String subtaskId;

    private String title;
    private boolean completed;
    private int order;          // Thứ tự hiển thị trong danh sách

    @ServerTimestamp
    private Date createdAt;

    // Constructor rỗng bắt buộc cho Firestore deserialization
    public Subtask() {}

    public Subtask(String title, int order) {
        this.title = title;
        this.order = order;
        this.completed = false;
    }

    // ========== Getters & Setters ==========

    public String getSubtaskId() { return subtaskId; }
    public void setSubtaskId(String subtaskId) { this.subtaskId = subtaskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
