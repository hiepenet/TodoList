package com.smarttodo.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.listener.OnCompleteListener;
import com.smarttodo.model.User;
import com.smarttodo.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository xử lý tất cả các thao tác Authentication.
 * Tuân theo Repository Pattern - tách biệt data layer khỏi UI layer.
 */
public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        FirebaseManager manager = FirebaseManager.getInstance();
        this.auth = manager.getAuth();
        this.db   = manager.getFirestore();
    }

    /**
     * Đăng ký tài khoản mới với email và password.
     * Sau khi đăng ký thành công, cập nhật display name và tạo document trong Firestore.
     */
    public void register(String name, String email, String password, OnCompleteListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        listener.onFailure("Đăng ký thất bại");
                        return;
                    }

                    // Cập nhật Display Name trong Firebase Auth
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    firebaseUser.updateProfile(profileUpdates);

                    // Lưu thông tin user vào Firestore
                    saveUserToFirestore(firebaseUser.getUid(), name, email, listener);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lưu thông tin user mới vào Firestore
     */
    private void saveUserToFirestore(String uid, String name, String email, OnCompleteListener listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("avatar", null);
        userData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Đăng ký thành công"))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Đăng nhập bằng email và password
     */
    public void login(String email, String password, OnCompleteListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> listener.onSuccess("Đăng nhập thành công"))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    public void sendPasswordResetEmail(String email, OnCompleteListener listener) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Email đặt lại mật khẩu đã được gửi"))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lấy thông tin user hiện tại từ Firestore
     */
    public void getCurrentUser(MutableLiveData<User> userLiveData) {
        String uid = FirebaseManager.getInstance().getCurrentUserId();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    User user = snapshot.toObject(User.class);
                    if (user != null) {
                        user.setUid(uid);
                        userLiveData.postValue(user);
                    }
                });
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Lấy FirebaseUser hiện tại
     */
    public FirebaseUser getFirebaseUser() {
        return auth.getCurrentUser();
    }
}
