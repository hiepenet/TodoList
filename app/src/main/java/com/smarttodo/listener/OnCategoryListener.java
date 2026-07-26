package com.smarttodo.listener;

import com.smarttodo.model.Category;
import java.util.List;

/**
 * Callback interfaces cho các thao tác liên quan đến Category.
 */
public interface OnCategoryListener {

    /**
     * Callback khi lấy danh sách category
     */
    interface OnCategoriesLoaded {
        void onSuccess(List<Category> categories);
        void onFailure(String error);
    }

    /**
     * Callback khi thao tác CRUD thành công/thất bại
     */
    interface OnCategoryOperationComplete {
        void onSuccess();
        void onFailure(String error);
    }
}
