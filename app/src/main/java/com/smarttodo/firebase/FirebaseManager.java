package com.smarttodo.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Singleton helper quản lý các Firebase instances.
 * Cung cấp truy cập tập trung đến FirebaseAuth, Firestore.
 * (Hình ảnh được quản lý qua Cloudinary).
 */
public class FirebaseManager {

    private static FirebaseManager instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    private FirebaseManager() {
        auth      = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bật offline persistence để hỗ trợ multi-device sync
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);
    }

    /**
     * Lấy instance singleton
     */
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // ========== Auth ==========

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // ========== Firestore ==========

    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}
