package com.smarttodo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.R;
import com.smarttodo.adapter.TaskAdapter;
import com.smarttodo.databinding.ActivityTrashBinding;
import com.smarttodo.model.Task;
import com.smarttodo.viewmodel.TaskViewModel;

import java.util.List;

public class TrashActivity extends AppCompatActivity {

    private ActivityTrashBinding binding;
    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        setupViews();
        setupRecyclerView();
        setupObservers();

        taskViewModel.loadTrashedTasks();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        binding.recyclerViewTrash.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTrash.setAdapter(taskAdapter);

        // Click trên item -> Hiện dialog Khôi phục / Xóa vĩnh viễn
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                showTrashOptionsDialog(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                showTrashOptionsDialog(task);
            }

            @Override
            public void onCompleteToggle(Task task, boolean isCompleted) {
                // Không làm gì cả
                taskAdapter.notifyDataSetChanged();
                Snackbar.make(binding.getRoot(), "Khôi phục task để chỉnh sửa", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Task task) {
                showHardDeleteConfirm(task);
            }
        });
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
            taskViewModel.loadTrashedTasks();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupObservers() {
        taskViewModel.trashedTasks.observe(this, tasks -> {
            taskAdapter.submitList(tasks);
            updateEmptyState(tasks);
        });

        taskViewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        taskViewModel.successMessage.observe(this, msg -> {
            if (msg != null) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#4CAF50")).show();
                taskViewModel.clearMessages();
            }
        });
        
        taskViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
                taskViewModel.clearMessages();
            }
        });
    }

    private void updateEmptyState(List<Task> tasks) {
        boolean isEmpty = tasks == null || tasks.isEmpty();
        binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewTrash.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showTrashOptionsDialog(Task task) {
        String[] options = {"Khôi phục", "Xóa vĩnh viễn"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(task.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        taskViewModel.restoreTask(task.getTaskId());
                    } else {
                        showHardDeleteConfirm(task);
                    }
                })
                .show();
    }

    private void showHardDeleteConfirm(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa vĩnh viễn")
                .setMessage("Bạn có chắc muốn xóa vĩnh viễn task này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                    taskViewModel.hardDeleteTask(task.getTaskId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
