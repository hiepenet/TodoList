package com.smarttodo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smarttodo.listener.OnCategoryListener;
import com.smarttodo.model.Category;
import com.smarttodo.repository.CategoryRepository;

import java.util.List;

/**
 * ViewModel xử lý Category logic.
 */
public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;

    public final MutableLiveData<List<Category>> categories    = new MutableLiveData<>();
    public final MutableLiveData<Boolean>        isLoading     = new MutableLiveData<>(false);
    public final MutableLiveData<String>         errorMessage  = new MutableLiveData<>();
    public final MutableLiveData<String>         successMessage= new MutableLiveData<>();

    public CategoryViewModel() {
        categoryRepository = new CategoryRepository();
    }

    /**
     * Load categories (real-time)
     */
    public void loadCategories() {
        categoryRepository.getCategories(categories);
    }

    /**
     * Thêm category
     */
    public void addCategory(String name, String color) {
        isLoading.setValue(true);
        Category category = new Category(null, name, color);
        categoryRepository.addCategory(category, new OnCategoryListener.OnCategoryOperationComplete() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                successMessage.postValue("Danh mục đã được thêm");
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Cập nhật category
     */
    public void updateCategory(Category category) {
        isLoading.setValue(true);
        categoryRepository.updateCategory(category, new OnCategoryListener.OnCategoryOperationComplete() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                successMessage.postValue("Danh mục đã được cập nhật");
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Xóa category
     */
    public void deleteCategory(String categoryId) {
        categoryRepository.deleteCategory(categoryId, new OnCategoryListener.OnCategoryOperationComplete() {
            @Override
            public void onSuccess() {
                successMessage.postValue("Danh mục đã được xóa");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Tạo categories mặc định cho user mới
     */
    public void createDefaultCategories() {
        categoryRepository.createDefaultCategories(new OnCategoryListener.OnCategoryOperationComplete() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String error) {}
        });
    }
}
