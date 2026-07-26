package com.smarttodo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarttodo.databinding.ItemCategoryBinding;
import com.smarttodo.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách Category trong RecyclerView (màn hình Category).
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.tvCategoryName.setText(category.getName());

            // Hiển thị màu category
            try {
                int color = Color.parseColor(category.getColor());
                binding.viewColorIndicator.setBackgroundColor(color);
                binding.tvCategoryName.setTextColor(color);
            } catch (Exception e) {
                binding.viewColorIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
            }

            binding.btnEditCategory.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(category);
            });
            binding.btnDeleteCategory.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(category);
            });
        }
    }
}
