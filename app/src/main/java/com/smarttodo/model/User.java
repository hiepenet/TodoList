package com.smarttodo.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class đại diện cho người dùng trong hệ thống.
 * Lưu trữ trên Firestore collection "users".
 */
public class User {

    @DocumentId
    private String uid;

    private String name;
    private String email;
    private String avatar; // URL ảnh đại diện từ Firebase Storage
    
    @ServerTimestamp
    private Date createdAt;

    // Constructor rỗng bắt buộc cho Firestore deserialization
    public User() {}

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    // ========== Getters & Setters ==========

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
