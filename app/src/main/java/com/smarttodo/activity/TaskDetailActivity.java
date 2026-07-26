package com.smarttodo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.adapter.SubtaskAdapter;
import com.smarttodo.databinding.ActivityTaskDetailBinding;
import com.smarttodo.model.Category;
import com.smarttodo.model.Task;
import com.smarttodo.repository.CategoryRepository;
import com.smarttodo.repository.TaskRepository;
import com.smarttodo.utils.DateUtils;
import com.smarttodo.viewmodel.TaskViewModel;

import android.content.Intent;

/**
 * TaskDetailActivity - màn hình xem chi tiết một Task.
 * Hiển thị đầy đủ thông tin task, có thể toggle complete, edit, delete.
 */
public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private TaskViewModel taskViewModel;
    private SubtaskAdapter subtaskAdapter;
    private Task currentTask;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskId = getIntent().getStringExtra("taskId");
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        if (taskId == null) {
            finish();
            return;
        }

        setupObservers();
        setupClickListeners();
        setupSubtasks();
        loadTask();
    }

    private void loadTask() {
        TaskRepository repo = new TaskRepository();
        repo.getTaskById(taskId, new com.smarttodo.listener.OnTaskListener.OnTaskLoaded() {
            @Override
            public void onSuccess(Task task) {
                currentTask = task;
                populateUI(task);
            }

            @Override
            public void onFailure(String error) {
                Snackbar.make(binding.getRoot(), "Lỗi: " + error, Snackbar.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateUI(Task task) {
        // Title
        binding.tvTitle.setText(task.getTitle());

        // Description
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            binding.tvDescription.setText(task.getDescription());
        } else {
            binding.tvDescription.setText("Không có mô tả");
        }

        // Priority
        switch (task.getPriority()) {
            case Task.PRIORITY_HIGH:
                binding.tvPriority.setText("● Cao");
                binding.tvPriority.setTextColor(Color.parseColor("#F44336"));
                binding.tvPriority.setBackgroundResource(com.smarttodo.R.drawable.bg_priority_high);
                break;
            case Task.PRIORITY_MEDIUM:
                binding.tvPriority.setText("● Trung bình");
                binding.tvPriority.setTextColor(Color.parseColor("#FF9800"));
                binding.tvPriority.setBackgroundResource(com.smarttodo.R.drawable.bg_priority_medium);
                break;
            default:
                binding.tvPriority.setText("● Thấp");
                binding.tvPriority.setTextColor(Color.parseColor("#4CAF50"));
                binding.tvPriority.setBackgroundResource(com.smarttodo.R.drawable.bg_priority_low);
        }

        // Status
        if (task.isCompleted()) {
            binding.tvStatus.setText("✅ Hoàn thành");
            binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            binding.btnToggleComplete.setText("↩ Đánh dấu chưa xong");
        } else if (task.isOverdue()) {
            binding.tvStatus.setText("⚠️ Quá hạn");
            binding.tvStatus.setTextColor(Color.parseColor("#F44336"));
            binding.btnToggleComplete.setText("✓ Đánh dấu hoàn thành");
        } else {
            binding.tvStatus.setText("⏳ Đang chờ");
            binding.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            binding.btnToggleComplete.setText("✓ Đánh dấu hoàn thành");
        }

        // Deadline
        binding.tvDeadline.setText(task.getDeadline() != null ?
                DateUtils.formatDateTime(task.getDeadline()) : "Không có");

        // Reminder
        binding.tvReminder.setText(task.getReminderTime() != null ?
                DateUtils.formatDateTime(task.getReminderTime()) : "Không có");

        // Category
        loadCategory(task.getCategoryId());

        // Image
        if (task.getImageUrl() != null && !task.getImageUrl().isEmpty()) {
            Glide.with(this).load(task.getImageUrl()).centerCrop().into(binding.ivTaskImage);
        } else {
            binding.ivTaskImage.setImageResource(com.smarttodo.R.drawable.ic_logo);
        }

        // Recurrence
        if (task.isRecurring()) {
            binding.layoutRecurrence.setVisibility(View.VISIBLE);
            binding.tvRecurrence.setText(task.getRecurrenceText());
        } else {
            binding.layoutRecurrence.setVisibility(View.GONE);
        }

        // Load subtasks
        taskViewModel.loadSubtasks(taskId);
        // Luôn hiện subtask section (cho phép thêm mới)
        binding.dividerSubtask.setVisibility(View.VISIBLE);
        binding.tvSubtaskLabel.setVisibility(View.VISIBLE);
        binding.recyclerViewSubtasks.setVisibility(View.VISIBLE);
        binding.layoutAddSubtask.setVisibility(View.VISIBLE);
    }

    private void loadCategory(String categoryId) {
        if (categoryId == null) return;
        CategoryRepository repo = new CategoryRepository();
        repo.getCategories(new androidx.lifecycle.MutableLiveData<>());
        // Simple approach: just show categoryId for now
        binding.tvCategory.setText("Task");
    }

    private void setupObservers() {
        taskViewModel.successMessage.observe(this, msg -> {
            if (msg != null) {
                if (msg.contains("xóa")) finish();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditTaskActivity.class);
            intent.putExtra("taskId", taskId);
            startActivity(intent);
        });

        binding.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa task")
                    .setMessage("Bạn có chắc muốn xóa task này?")
                    .setPositiveButton("Xóa", (d, w) -> taskViewModel.deleteTask(taskId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        binding.btnToggleComplete.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            if (currentTask != null) {
                boolean newState = !currentTask.isCompleted();
                if (newState && currentTask.hasSubtasks() && currentTask.getSubtaskCompleted() < currentTask.getSubtaskCount()) {
                    Snackbar.make(binding.getRoot(), "Vui lòng hoàn thành toàn bộ " + currentTask.getSubtaskCount() + " task con", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                taskViewModel.toggleComplete(taskId, newState);
                currentTask.setCompleted(newState);
                populateUI(currentTask);
            }
        });
    }

    /**
     * Setup subtask RecyclerView và observers
     */
    private void setupSubtasks() {
        subtaskAdapter = new SubtaskAdapter();
        binding.recyclerViewSubtasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSubtasks.setAdapter(subtaskAdapter);

        subtaskAdapter.setOnSubtaskListener(new SubtaskAdapter.OnSubtaskListener() {
            @Override
            public void onToggleComplete(com.smarttodo.model.Subtask subtask, boolean isCompleted) {
                taskViewModel.toggleSubtaskComplete(taskId, subtask, isCompleted);
            }

            @Override
            public void onDelete(com.smarttodo.model.Subtask subtask) {
                taskViewModel.deleteSubtask(taskId, subtask);
            }
        });

        // Observe subtasks
        taskViewModel.subtasks.observe(this, subtasks -> {
            if (subtasks != null) {
                subtaskAdapter.submitList(subtasks);
                updateSubtaskProgress(subtasks);
            }
        });

        // Nút thêm subtask
        binding.btnAddSubtask.setOnClickListener(v -> addNewSubtask());

        // Nhấn Enter trong ô input cũng thêm subtask
        binding.etSubtaskTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addNewSubtask();
                return true;
            }
            return false;
        });
    }

    private void addNewSubtask() {
        String title = binding.etSubtaskTitle.getText() != null ?
                binding.etSubtaskTitle.getText().toString().trim() : "";
        if (!title.isEmpty()) {
            taskViewModel.addSubtask(taskId, title);
            binding.etSubtaskTitle.setText("");
        }
    }

    private void updateSubtaskProgress(java.util.List<com.smarttodo.model.Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            binding.layoutSubtaskProgress.setVisibility(View.GONE);
            return;
        }

        binding.layoutSubtaskProgress.setVisibility(View.VISIBLE);
        int total = subtasks.size();
        int completed = 0;
        for (com.smarttodo.model.Subtask s : subtasks) {
            if (s.isCompleted()) completed++;
        }
        binding.progressSubtask.setMax(total);
        binding.progressSubtask.setProgress(completed);
        binding.tvSubtaskProgress.setText(completed + "/" + total + " hoàn thành");

        if (total > 0 && currentTask != null) {
            if (completed == total && !currentTask.isCompleted()) {
                taskViewModel.toggleComplete(taskId, true);
                currentTask.setCompleted(true);
                populateUI(currentTask);
            } else if (completed < total && currentTask.isCompleted()) {
                taskViewModel.toggleComplete(taskId, false);
                currentTask.setCompleted(false);
                populateUI(currentTask);
            }
        }
    }
}
