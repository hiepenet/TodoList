package com.smarttodo.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.R;
import com.smarttodo.adapter.SubtaskAdapter;
import com.smarttodo.databinding.ActivityAddTaskBinding;
import com.smarttodo.model.Category;
import com.smarttodo.model.Subtask;
import com.smarttodo.model.Task;
import com.smarttodo.notification.NotificationHelper;
import com.smarttodo.utils.Constants;
import com.smarttodo.utils.DateUtils;
import com.smarttodo.utils.ValidationUtils;
import com.smarttodo.viewmodel.CategoryViewModel;
import com.smarttodo.viewmodel.TaskViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * AddTaskActivity - màn hình thêm task mới.
 * Xử lý: title, description, category, priority, deadline, reminder, image.
 * Upload ảnh lên Firebase Storage.
 * Đặt AlarmManager nếu có reminder.
 */
public class AddTaskActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;
    private TaskViewModel          taskViewModel;
    private CategoryViewModel      categoryViewModel;

    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId = null;
    private int    selectedPriority   = Task.PRIORITY_MEDIUM;
    private Date   selectedDeadline   = null;
    private Date   selectedReminder   = null;
    private String uploadedImageUrl   = null;
    private Uri    cameraImageUri     = null;

    // Recurrence state
    private int    selectedRecurrenceType     = Task.RECURRENCE_NONE;
    private int    selectedRecurrenceInterval = 1;

    // Subtask list (local, lưu khi save task)
    private java.util.List<Subtask> localSubtasks = new ArrayList<>();
    private SubtaskAdapter subtaskAdapter;

    // Activity Result Launchers
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    handleImageSelected(imageUri);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    handleImageSelected(cameraImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskViewModel     = new ViewModelProvider(this).get(TaskViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        setupToolbar();
        setupObservers();
        setupClickListeners();
        setupSubtaskSection();
        categoryViewModel.loadCategories();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupObservers() {
        // Load categories vào Spinner
        categoryViewModel.categories.observe(this, cats -> {
            if (cats == null) return;
            categories = cats;
            List<String> names = new ArrayList<>();
            for (Category c : cats) names.add(c.getName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, names);
            binding.spinnerCategory.setAdapter(adapter);
            binding.spinnerCategory.setOnItemClickListener((parent, view, pos, id) -> {
                selectedCategoryId = categories.get(pos).getCategoryId();
            });
        });

        // Observe loading
        taskViewModel.isLoading.observe(this, isLoading -> {
            binding.progressUpload.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!isLoading);
        });

        // Observe upload progress
        taskViewModel.uploadProgress.observe(this, progress -> {
            if (progress != null) {
                binding.progressUpload.setProgress(progress);
            }
        });

        // Observe uploaded image URL
        taskViewModel.uploadedImageUrl.observe(this, url -> {
            if (url != null) {
                uploadedImageUrl = url;
            }
        });

        // Observe success - save subtasks then close activity
        taskViewModel.successMessage.observe(this, msg -> {
            if (msg != null && msg.contains("thêm")) {
                // Lưu subtasks nếu có
                if (!localSubtasks.isEmpty()) {
                    String taskId = taskViewModel.uploadedImageUrl.getValue() != null ?
                            null : null; // Subtasks sẽ được lưu qua repository
                    // Lấy task ID vừa tạo từ allTasks
                    java.util.List<Task> tasks = taskViewModel.allTasks.getValue();
                    if (tasks != null && !tasks.isEmpty()) {
                        String newTaskId = tasks.get(0).getTaskId();
                        if (newTaskId != null) {
                            for (Subtask sub : localSubtasks) {
                                taskViewModel.addSubtask(newTaskId, sub.getTitle());
                            }
                        }
                    }
                }
                setResult(RESULT_OK);
                finish();
            }
        });

        // Observe error
        taskViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                taskViewModel.clearMessages();
            }
        });
    }

    private void setupClickListeners() {
        // Priority Chips
        binding.chipHigh.setOnClickListener(v -> selectedPriority = Task.PRIORITY_HIGH);
        binding.chipMedium.setOnClickListener(v -> selectedPriority = Task.PRIORITY_MEDIUM);
        binding.chipLow.setOnClickListener(v -> selectedPriority = Task.PRIORITY_LOW);

        // Deadline picker
        binding.btnSelectDeadline.setOnClickListener(v -> showDateTimePicker(true));

        // Reminder picker
        binding.btnSelectReminder.setOnClickListener(v -> showDateTimePicker(false));

        // Camera
        binding.btnCamera.setOnClickListener(v -> openCamera());

        // Gallery
        binding.btnGallery.setOnClickListener(v -> openGallery());

        // Save
        binding.btnSave.setOnClickListener(v -> saveTask());

        // Recurrence
        binding.btnSelectRecurrence.setOnClickListener(v -> showRecurrencePicker());
    }

    /**
     * Setup subtask section (local list trước khi save)
     */
    private void setupSubtaskSection() {
        subtaskAdapter = new SubtaskAdapter();
        binding.recyclerViewSubtasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSubtasks.setAdapter(subtaskAdapter);

        subtaskAdapter.setOnSubtaskListener(new SubtaskAdapter.OnSubtaskListener() {
            @Override
            public void onToggleComplete(Subtask subtask, boolean isCompleted) {
                subtask.setCompleted(isCompleted);
                subtaskAdapter.submitList(new ArrayList<>(localSubtasks));
            }

            @Override
            public void onDelete(Subtask subtask) {
                localSubtasks.remove(subtask);
                subtaskAdapter.submitList(new ArrayList<>(localSubtasks));
            }
        });

        binding.btnAddSubtask.setOnClickListener(v -> addLocalSubtask());

        binding.etSubtaskTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addLocalSubtask();
                return true;
            }
            return false;
        });
    }

    private void addLocalSubtask() {
        String title = binding.etSubtaskTitle.getText() != null ?
                binding.etSubtaskTitle.getText().toString().trim() : "";
        if (!title.isEmpty()) {
            Subtask sub = new Subtask(title, localSubtasks.size());
            sub.setSubtaskId("local_" + System.currentTimeMillis());
            localSubtasks.add(sub);
            subtaskAdapter.submitList(new ArrayList<>(localSubtasks));
            binding.etSubtaskTitle.setText("");
        }
    }

    /**
     * Hiển thị Recurrence Picker Dialog
     */
    private void showRecurrencePicker() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recurrence_picker, null);
        RadioGroup rgType = dialogView.findViewById(R.id.rgRecurrenceType);
        View layoutCustom = dialogView.findViewById(R.id.layoutCustomInterval);
        android.widget.EditText etInterval = dialogView.findViewById(R.id.etInterval);

        // Set current selection
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

        // Show/hide custom interval
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

    /**
     * Hiển thị DatePicker + TimePicker.
     * @param isDeadline true = deadline, false = reminder
     */
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

    /**
     * Validate rồi lưu task vào Firestore
     */
    private void saveTask() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String desc  = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        // Validate
        String titleError    = ValidationUtils.validateTitle(title);
        String categoryError = ValidationUtils.validateCategory(selectedCategoryId);
        String deadlineError = ValidationUtils.validateDeadline(selectedDeadline);

        binding.tilTitle.setError(titleError);
        binding.tilCategory.setError(categoryError);

        if (titleError != null || categoryError != null || deadlineError != null) {
            if (deadlineError != null) Snackbar.make(binding.getRoot(), deadlineError, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Tạo Task object
        Task task = new Task();
        String newTaskId = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_TASKS).document().getId();
        task.setTaskId(newTaskId);
        task.setTitle(title);
        task.setDescription(desc);
        task.setCategoryId(selectedCategoryId);
        task.setPriority(selectedPriority);
        task.setDeadline(selectedDeadline);
        task.setReminderTime(selectedReminder);
        task.setImageUrl(uploadedImageUrl);
        task.setCompleted(false);
        task.setRecurrenceType(selectedRecurrenceType);
        task.setRecurrenceInterval(selectedRecurrenceInterval);
        task.setSubtaskCount(0);
        task.setSubtaskCompleted(0);

        // Đặt alarm nếu có reminder
        if (selectedReminder != null) {
            checkAndRequestNotificationPermission();
            scheduleReminder(task);
        }

        // Lưu task
        taskViewModel.addTask(task);
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * Đặt AlarmManager cho task reminder
     */
    private void scheduleReminder(Task task) {
        NotificationHelper helper = new NotificationHelper(this);
        helper.scheduleReminder(task);
    }

    /**
     * Mở Camera để chụp ảnh
     */
    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_PERMISSION_CAMERA);
            return;
        }

        File photoFile = new File(getExternalFilesDir(null), "task_" + System.currentTimeMillis() + ".jpg");
        cameraImageUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(intent);
    }

    /**
     * Mở Gallery để chọn ảnh
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    /**
     * Xử lý ảnh được chọn - hiển thị preview rồi upload
     */
    private void handleImageSelected(Uri imageUri) {
        // Hiển thị preview
        binding.ivTaskImage.setVisibility(View.VISIBLE);
        binding.ivTaskImage.clearColorFilter();
        binding.ivTaskImage.setImageURI(imageUri);

        // Upload lên Firebase / Cloudinary
        taskViewModel.uploadTaskImage(this, imageUri);
    }
}
