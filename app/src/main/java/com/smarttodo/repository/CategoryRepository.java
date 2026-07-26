package com.smarttodo.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.listener.OnCategoryListener;
import com.smarttodo.model.Category;
import com.smarttodo.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository xử lý CRUD operations cho Category trên Firestore.
 */
public class CategoryRepository {

    private final FirebaseFirestore db;

    public CategoryRepository() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    private String getUserId() {
        return FirebaseManager.getInstance().getCurrentUserId();
    }

    /**
     * Lấy tất cả categories của user, real-time listener
     */
    public void getCategories(MutableLiveData<List<Category>> liveData) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_CATEGORIES)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Category> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Category cat = doc.toObject(Category.class);
                            if (cat != null) {
                                cat.setCategoryId(doc.getId());
                                categories.add(cat);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Collections.sort(categories, (c1, c2) -> {
                        if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
                        if (c1.getCreatedAt() == null) return 1;
                        if (c2.getCreatedAt() == null) return -1;
                        return c1.getCreatedAt().compareTo(c2.getCreatedAt());
                    });
                    liveData.postValue(categories);
                });
    }

    /**
     * Thêm category mới
     */
    public void addCategory(Category category, OnCategoryListener.OnCategoryOperationComplete listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onFailure("Chưa đăng nhập");
            return;
        }
        category.setUserId(uid);

        db.collection(Constants.COLLECTION_CATEGORIES)
                .add(category)
                .addOnSuccessListener(docRef -> {
                    category.setCategoryId(docRef.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Cập nhật category
     */
    public void updateCategory(Category category, OnCategoryListener.OnCategoryOperationComplete listener) {
        if (category.getCategoryId() == null) {
            listener.onFailure("Category ID không hợp lệ");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",  category.getName());
        updates.put("color", category.getColor());

        db.collection(Constants.COLLECTION_CATEGORIES)
                .document(category.getCategoryId())
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Xóa category
     */
    public void deleteCategory(String categoryId, OnCategoryListener.OnCategoryOperationComplete listener) {
        db.collection(Constants.COLLECTION_CATEGORIES)
                .document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Tạo các danh mục mặc định cho user mới
     */
    public void createDefaultCategories(OnCategoryListener.OnCategoryOperationComplete listener) {
        String uid = getUserId();
        if (uid == null) return;
        String[] names  = Constants.DEFAULT_CATEGORY_NAMES;
        String[] colors = Constants.DEFAULT_CATEGORY_COLORS;

        for (int i = 0; i < names.length; i++) {
            Category cat = new Category(uid, names[i], colors[i]);
            db.collection(Constants.COLLECTION_CATEGORIES).add(cat);
        }
        listener.onSuccess();
    }
}
