package com.smarttodo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.R;
import com.smarttodo.adapter.CategoryAdapter;
import com.smarttodo.databinding.ActivityCategoryBinding;
import com.smarttodo.model.Category;
import com.smarttodo.viewmodel.CategoryViewModel;

/**
 * CategoryActivity - quản lý danh mục (CRUD).
 * Hiển thị danh sách category với màu sắc, cho phép thêm/sửa/xóa.
 */
public class CategoryActivity extends AppCompatActivity {

    private ActivityCategoryBinding binding;
    private CategoryViewModel       categoryViewModel;
    private CategoryAdapter         categoryAdapter;

    // Màu sắc mặc định cho picker
    private static final String[] COLOR_OPTIONS = {
            "#2196F3", "#9C27B0", "#FF9800", "#E91E63",
            "#00BCD4", "#4CAF50", "#F44336", "#795548"
    };
    private String selectedColor = "#4CAF50";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        categoryViewModel.loadCategories();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onEditClick(Category category) {
                showCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                showDeleteConfirmation(category);
            }
        });
    }

    private void setupObservers() {
        categoryViewModel.categories.observe(this, categories -> {
            if (categories == null) return;
            categoryAdapter.setCategories(categories);
            binding.layoutEmptyState.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
            binding.recyclerViewCategories.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
        });

        categoryViewModel.successMessage.observe(this, msg -> {
            if (msg != null) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#4CAF50")).show();
            }
        });

        categoryViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    /**
     * Hiển thị dialog thêm/sửa category.
     * @param category null nếu thêm mới, có giá trị nếu sửa
     */
    private void showCategoryDialog(Category category) {
        boolean isEdit = category != null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);

        if (isEdit) {
            etName.setText(category.getName());
            selectedColor = category.getColor();
        }

        // Color picker buttons
        setupColorPicker(dialogView);

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa danh mục" : "Thêm danh mục")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Cập nhật" : "Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Snackbar.make(binding.getRoot(), "Tên không được để trống", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (isEdit) {
                        category.setName(name);
                        category.setColor(selectedColor);
                        categoryViewModel.updateCategory(category);
                    } else {
                        categoryViewModel.addCategory(name, selectedColor);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xử lý color picker trong dialog
     */
    private void setupColorPicker(View dialogView) {
        int[] colorButtons = {
                R.id.color1, R.id.color2, R.id.color3, R.id.color4,
                R.id.color5, R.id.color6, R.id.color7, R.id.color8
        };

        for (int i = 0; i < colorButtons.length && i < COLOR_OPTIONS.length; i++) {
            final String color = COLOR_OPTIONS[i];
            View btn = dialogView.findViewById(colorButtons[i]);
            if (btn != null) {
                btn.setBackgroundColor(Color.parseColor(color));
                btn.setOnClickListener(v -> selectedColor = color);
            }
        }
    }

    private void showDeleteConfirmation(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Xóa danh mục \"" + category.getName() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> categoryViewModel.deleteCategory(category.getCategoryId()))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
