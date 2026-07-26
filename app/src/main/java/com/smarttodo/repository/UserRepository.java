package com.smarttodo.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.listener.OnCompleteListener;
import com.smarttodo.model.User;
import com.smarttodo.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository xử lý các thao tác liên quan đến Profile user.
 */
public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    private String getUserId() {
        return FirebaseManager.getInstance().getCurrentUserId();
    }

    /**
     * Lấy thông tin user hiện tại (real-time)
     */
    public void getUserProfile(MutableLiveData<User> liveData) {
        String uid = getUserId();
        FirebaseUser fbUser = FirebaseManager.getInstance().getCurrentUser();

        if (uid == null && fbUser != null) {
            uid = fbUser.getUid();
        }

        if (uid == null) return;

        final String finalUid = uid;
        db.collection(Constants.COLLECTION_USERS)
                .document(finalUid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        if (fbUser != null) {
                            String email = fbUser.getEmail() != null ? fbUser.getEmail() : "user@example.com";
                            String name = fbUser.getDisplayName();
                            if (name == null || name.trim().isEmpty()) {
                                if (email.contains("@")) {
                                    name = email.substring(0, email.indexOf("@"));
                                } else {
                                    name = "Người dùng";
                                }
                            }
                            String photoUrl = fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : null;

                            // Tạo/khôi phục document tự động trong Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", finalUid);
                            userData.put("name", name);
                            userData.put("email", email);
                            if (photoUrl != null) {
                                userData.put("avatar", photoUrl);
                            }
                            userData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                            db.collection(Constants.COLLECTION_USERS).document(finalUid).set(userData, SetOptions.merge());

                            User fallback = new User();
                            fallback.setUid(finalUid);
                            fallback.setEmail(email);
                            fallback.setName(name);
                            fallback.setAvatar(photoUrl);
                            liveData.postValue(fallback);
                        }
                        return;
                    }
                    try {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            user.setUid(finalUid);
                            
                            // 1. Lấy name từ Firestore / Auth / Email
                            String name = user.getName();
                            if (name == null || name.trim().isEmpty()) {
                                if (fbUser != null && fbUser.getDisplayName() != null && !fbUser.getDisplayName().trim().isEmpty()) {
                                    name = fbUser.getDisplayName();
                                }
                            }
                            if (name == null || name.trim().isEmpty()) {
                                String email = user.getEmail() != null ? user.getEmail() : (fbUser != null ? fbUser.getEmail() : "");
                                if (email != null && email.contains("@")) {
                                    name = email.substring(0, email.indexOf("@"));
                                } else {
                                    name = "Người dùng";
                                }
                            }
                            user.setName(name);

                            // 2. Lấy email từ Auth nếu thiếu
                            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                                if (fbUser != null && fbUser.getEmail() != null) {
                                    user.setEmail(fbUser.getEmail());
                                }
                            }

                            // 3. Lấy avatar từ Firestore / Auth
                            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                                if (fbUser != null && fbUser.getPhotoUrl() != null) {
                                    user.setAvatar(fbUser.getPhotoUrl().toString());
                                }
                            }

                            liveData.postValue(user);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Cập nhật avatar URL trong Firestore và Firebase Auth
     */
    public void updateAvatar(String avatarUrl, OnCompleteListener listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onFailure("User không tồn tại");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("avatar", avatarUrl);

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật cả Firebase Auth profile photo
                    FirebaseUser fbUser = FirebaseManager.getInstance().getCurrentUser();
                    if (fbUser != null && avatarUrl != null) {
                        try {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(avatarUrl))
                                    .build();
                            fbUser.updateProfile(profileUpdates);
                        } catch (Exception ignored) {}
                    }
                    listener.onSuccess("Ảnh đại diện đã được cập nhật");
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Cập nhật tên user
     */
    public void updateName(String name, OnCompleteListener listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onFailure("User không tồn tại");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    FirebaseUser fbUser = FirebaseManager.getInstance().getCurrentUser();
                    if (fbUser != null && name != null) {
                        try {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            fbUser.updateProfile(profileUpdates);
                        } catch (Exception ignored) {}
                    }
                    listener.onSuccess("Tên đã được cập nhật");
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
}
