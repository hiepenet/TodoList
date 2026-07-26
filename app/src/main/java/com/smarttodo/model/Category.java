package com.smarttodo.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class đại diện cho một danh mục (Category) của Task.
 * Lưu trữ trên Firestore collection "categories".
 */
public class Category {

    @DocumentId
    private String categoryId;

    private String userId;
    private String name;
    private String color; // Màu dạng hex, ví dụ "#2196F3"
    
    @ServerTimestamp
    private Date createdAt;

    // Constructor rỗng bắt buộc cho Firestore deserialization
    public Category() {}

    public Category(String userId, String name, String color) {
        this.userId = userId;
        this.name = name;
        this.color = color;
    }

    // ========== Getters & Setters ==========

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name; // Dùng cho Spinner adapter
    }
}
