package com.smarttodo.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.R;
import com.smarttodo.databinding.ActivityAddTaskBinding;
import com.smarttodo.listener.OnTaskListener;
import com.smarttodo.model.Category;
import com.smarttodo.model.Task;
import com.smarttodo.notification.NotificationHelper;
import com.smarttodo.repository.TaskRepository;
import com.smarttodo.utils.DateUtils;
import com.smarttodo.utils.ValidationUtils;
import com.smarttodo.viewmodel.CategoryViewModel;
import com.smarttodo.viewmodel.TaskViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * EditTaskActivity - màn hình sửa task.
 * Tái sử dụng layout của AddTaskActivity.
 * Load dữ liệu task hiện tại rồi cho phép sửa.
 */
public class EditTaskActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;
    private TaskViewModel          taskViewModel;
    private CategoryViewModel      categoryViewModel;
    private Task                   currentTask;
    private String                 taskId;

    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId;
    private int    selectedPriority   = Task.PRIORITY_MEDIUM;
    private Date   selectedDeadline   = null;
    private Date   selectedReminder   = null;

    // Recurrence state
    private int    selectedRecurrenceType     = Task.RECURRENCE_NONE;
    private int    selectedRecurrenceInterval = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskId = getIntent().getStringExtra("taskId");
        if (taskId == null) { finish(); return; }

        taskViewModel     = new ViewModelProvider(this).get(TaskViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Change toolbar title
        binding.toolbar.setTitle("Sửa Task");
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setText("Cập nhật Task");

        setupObservers();
        setupClickListeners();
        categoryViewModel.loadCategories();
        loadTask();
    }

    private void loadTask() {
        TaskRepository repo = new TaskRepository();
        repo.getTaskById(taskId, new OnTaskListener.OnTaskLoaded() {
            @Override
            public void onSuccess(Task task) {
                currentTask = task;
                populateForm(task);
            }

            @Override
            public void onFailure(String error) {
                Snackbar.make(binding.getRoot(), "Lỗi: " + error, Snackbar.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateForm(Task task) {
        binding.etTitle.setText(task.getTitle());
        binding.etDescription.setText(task.getDescription());

        selectedCategoryId = task.getCategoryId();
        selectedPriority   = task.getPriority();
        selectedDeadline   = task.getDeadline();
        selectedReminder   = task.getReminderTime();

        // Set priority chip
        binding.chipHigh.setChecked(task.getPriority() == Task.PRIORITY_HIGH);
        binding.chipMedium.setChecked(task.getPriority() == Task.PRIORITY_MEDIUM);
        binding.chipLow.setChecked(task.getPriority() == Task.PRIORITY_LOW);

        // Set recurrence
        selectedRecurrenceType = task.getRecurrenceType();
        selectedRecurrenceInterval = Math.max(1, task.getRecurrenceInterval());
        updateRecurrenceUI();

        // Set deadline/reminder text
        if (task.getDeadline() != null) {
            binding.tvDeadlineValue.setText("📅 " + DateUtils.formatDateTime(task.getDeadline()));
        }
        if (task.getReminderTime() != null) {
            binding.tvReminderValue.setText("🔔 " + DateUtils.formatDateTime(task.getReminderTime()));
        }

        // Show image if exists
        if (task.getImageUrl() != null && !task.getImageUrl().isEmpty()) {
            binding.ivTaskImage.setVisibility(View.VISIBLE);
            binding.ivTaskImage.clearColorFilter();
            Glide.with(this).load(task.getImageUrl()).centerCrop().into(binding.ivTaskImage);
        }
    }

    private void setupObservers() {
        categoryViewModel.categories.observe(this, cats -> {
            if (cats == null) return;
            categories = cats;
            List<String> names = new ArrayList<>();
            for (Category c : cats) names.add(c.getName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, names);
            binding.spinnerCategory.setAdapter(adapter);

            // Set selected category
            if (selectedCategoryId != null) {
                for (int i = 0; i < cats.size(); i++) {
                    if (cats.get(i).getCategoryId().equals(selectedCategoryId)) {
                        binding.spinnerCategory.setText(cats.get(i).getName(), false);
                        break;
                    }
                }
            }

            binding.spinnerCategory.setOnItemClickListener((parent, view, pos, id) -> {
                selectedCategoryId = categories.get(pos).getCategoryId();
            });
        });

        taskViewModel.successMessage.observe(this, msg -> {
            if (msg != null && msg.contains("cập nhật")) {
                setResult(RESULT_OK);
                finish();
            }
        });

        taskViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
                taskViewModel.clearMessages();
            }
        });
    }

    private void setupClickListeners() {
        binding.chipHigh.setOnClickListener(v   -> selectedPriority = Task.PRIORITY_HIGH);
        binding.chipMedium.setOnClickListener(v -> selectedPriority = Task.PRIORITY_MEDIUM);
        binding.chipLow.setOnClickListener(v    -> selectedPriority = Task.PRIORITY_LOW);

        binding.btnSelectDeadline.setOnClickListener(v -> showDateTimePicker(true));
        binding.btnSelectReminder.setOnClickListener(v -> showDateTimePicker(false));

        // Ẩn camera/gallery buttons (giữ ảnh cũ)
        binding.btnCamera.setVisibility(View.GONE);
        binding.btnGallery.setVisibility(View.GONE);

        binding.btnSave.setOnClickListener(v -> updateTask());

        // Recurrence
        binding.btnSelectRecurrence.setOnClickListener(v -> showRecurrencePicker());
    }

    /**
     * Hiển thị Recurrence Picker Dialog
     */
    private void showRecurrencePicker() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recurrence_picker, null);
        RadioGroup rgType = dialogView.findViewById(R.id.rgRecurrenceType);
        View layoutCustom = dialogView.findViewById(R.id.layoutCustomInterval);
        android.widget.EditText etInterval = dialogView.findViewById(R.id.etInterval);

        switch (selectedRecurrenceType) {
            case Task.RECURRENCE_DAILY:   rgType.check(R.id.rbDaily); break;
            case Task.RECURRENCE_WEEKLY:  rgType.check(R.id.rbWeekly); break;
            case Task.RECURRENCE_MONTHLY: rgType.check(R.id.rbMonthly); break;
            case Task.RECURRENCE_CUSTOM:
                rgType.check(R.id.rbCustom);
                layoutCustom.setVisibility(View.VISIBLE);
                etInterval.setText(String.valueOf(selectedRecurrenceInterval));
                break;
            default: rgType.check(R.id.rbNone); break;
        }

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            layoutCustom.setVisibility(checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE);
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    int checkedId = rgType.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbDaily) {
                        selectedRecurrenceType = Task.RECURRENCE_DAILY;
                        selectedRecurrenceInterval = 1;
                    } else if (checkedId == R.id.rbWeekly) {
                        selectedRecurrenceType = Task.RECURRENCE_WEEKLY;
                        selectedRecurrenceInterval = 1;
                    } else if (checkedId == R.id.rbMonthly) {
                        selectedRecurrenceType = Task.RECURRENCE_MONTHLY;
                        selectedRecurrenceInterval = 1;
                    } else if (checkedId == R.id.rbCustom) {
                        selectedRecurrenceType = Task.RECURRENCE_CUSTOM;
                        try {
                            selectedRecurrenceInterval = Integer.parseInt(etInterval.getText().toString());
                            if (selectedRecurrenceInterval < 1) selectedRecurrenceInterval = 1;
                        } catch (NumberFormatException e) {
                            selectedRecurrenceInterval = 2;
                        }
                    } else {
                        selectedRecurrenceType = Task.RECURRENCE_NONE;
                        selectedRecurrenceInterval = 0;
                    }
                    updateRecurrenceUI();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateRecurrenceUI() {
        Task tempTask = new Task();
        tempTask.setRecurrenceType(selectedRecurrenceType);
        tempTask.setRecurrenceInterval(selectedRecurrenceInterval);
        String text = tempTask.getRecurrenceText();
        if (text != null) {
            binding.tvRecurrenceValue.setText("🔄 " + text);
            binding.btnSelectRecurrence.setText("🔄 " + text);
        } else {
            binding.tvRecurrenceValue.setText("Không lặp lại");
            binding.btnSelectRecurrence.setText("🔄 Không lặp lại");
        }
    }

    private void showDateTimePicker(boolean isDeadline) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            new TimePickerDialog(this, (tv, hour, minute) -> {
                Date date = DateUtils.createDate(year, month, day, hour, minute);
                if (isDeadline) {
                    selectedDeadline = date;
                    binding.tvDeadlineValue.setText("📅 " + DateUtils.formatDateTime(date));
                } else {
                    selectedReminder = date;
                    binding.tvReminderValue.setText("🔔 " + DateUtils.formatDateTime(date));
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateTask() {
        if (currentTask == null) return;

        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String desc  = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        String titleError    = ValidationUtils.validateTitle(title);
        String categoryError = ValidationUtils.validateCategory(selectedCategoryId);

        binding.tilTitle.setError(titleError);
        binding.tilCategory.setError(categoryError);
        if (titleError != null || categoryError != null) return;

        currentTask.setTitle(title);
        currentTask.setDescription(desc);
        currentTask.setCategoryId(selectedCategoryId);
        currentTask.setPriority(selectedPriority);
        currentTask.setDeadline(selectedDeadline);
        currentTask.setReminderTime(selectedReminder);
        currentTask.setRecurrenceType(selectedRecurrenceType);
        currentTask.setRecurrenceInterval(selectedRecurrenceInterval);

        taskViewModel.updateTask(currentTask);

        // Update alarm nếu reminder thay đổi
        if (selectedReminder != null) {
            NotificationHelper helper = new NotificationHelper(this);
            helper.cancelReminder(taskId);
            helper.scheduleReminder(currentTask);
        }
    }
}
