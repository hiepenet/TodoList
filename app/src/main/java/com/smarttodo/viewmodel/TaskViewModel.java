package com.smarttodo.viewmodel;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smarttodo.listener.OnTaskListener;
import com.smarttodo.model.Subtask;
import com.smarttodo.model.Task;
import com.smarttodo.repository.StorageRepository;
import com.smarttodo.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel xử lý Task logic (Optimistic UI Update + Real-Time Sync).
 * Cung cấp LiveData cho UI và filter tasks.
 */
public class TaskViewModel extends ViewModel {

    private final TaskRepository    taskRepository;
    private final StorageRepository storageRepository;

    // LiveData
    public final MutableLiveData<List<Task>> allTasks       = new MutableLiveData<>();
    public final MutableLiveData<List<Task>> filteredTasks  = new MutableLiveData<>();
    public final MutableLiveData<List<Task>> trashedTasks   = new MutableLiveData<>();
    public final MutableLiveData<Boolean>    isLoading      = new MutableLiveData<>(false);
    public final MutableLiveData<String>     errorMessage   = new MutableLiveData<>();
    public final MutableLiveData<String>     successMessage = new MutableLiveData<>();
    public final MutableLiveData<String>     uploadedImageUrl = new MutableLiveData<>();
    public final MutableLiveData<Integer>    uploadProgress = new MutableLiveData<>();

    // Subtask LiveData
    public final MutableLiveData<List<Subtask>> subtasks = new MutableLiveData<>();

    // Filter state
    private String currentSearch    = "";
    private String filterCategoryId = null;
    private int    filterPriority   = 0; // 0 = all
    private int    filterStatus     = 0; // 0 = all, 1 = completed, 2 = pending
    private boolean filterToday     = false;
    private int    sortOption       = 0; // 0=Mặc định(Mới nhất), 1=Hạn chót, 2=Độ ưu tiên, 3=Tên

    public TaskViewModel() {
        taskRepository    = new TaskRepository();
        storageRepository = new StorageRepository();
    }

    /**
     * Tải tất cả task và observe real-time changes từ Firestore
     */
    public void loadAllTasks() {
        taskRepository.getAllTasks(allTasks);
    }

    /**
     * Áp dụng filter và search trên danh sách task hiện tại
     */
    public void applyFilter() {
        List<Task> tasks = allTasks.getValue();
        if (tasks == null) {
            filteredTasks.setValue(new ArrayList<>());
            return;
        }

        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            // Filter by search query
            if (!currentSearch.isEmpty()) {
                if (task.getTitle() == null || !task.getTitle().toLowerCase().contains(currentSearch.toLowerCase())) {
                    continue;
                }
            }
            // Filter by category
            if (filterCategoryId != null && !filterCategoryId.isEmpty()) {
                if (!filterCategoryId.equals(task.getCategoryId())) {
                    continue;
                }
            }
            // Filter by priority
            if (filterPriority > 0 && task.getPriority() != filterPriority) {
                continue;
            }
            // Filter by status
            if (filterStatus == 1 && !task.isCompleted()) continue;
            if (filterStatus == 2 && task.isCompleted())  continue;
            
            // Filter by Today (My Day)
            if (filterToday) {
                if (task.getDeadline() == null) continue;
                java.util.Date startOfDay = com.smarttodo.utils.DateUtils.getStartOfDay(null);
                java.util.Date endOfDay   = com.smarttodo.utils.DateUtils.getEndOfDay(null);
                // Include overdue tasks or tasks due today
                if (task.getDeadline().after(endOfDay) && !task.isCompleted()) {
                    continue; // Skip future tasks
                }
                // If it's a completed task, only show if it was due today (don't show old completed tasks in My Day)
                if (task.isCompleted() && task.getDeadline().before(startOfDay)) {
                    continue;
                }
            }

            result.add(task);
        }

        // Apply Sorting
        java.util.Collections.sort(result, (t1, t2) -> {
            // Luôn đưa task hoàn thành xuống dưới cùng
            if (t1.isCompleted() && !t2.isCompleted()) return 1;
            if (!t1.isCompleted() && t2.isCompleted()) return -1;

            switch (sortOption) {
                case 1: // Hạn chót (Deadline gần nhất lên đầu)
                    if (t1.getDeadline() == null && t2.getDeadline() == null) return 0;
                    if (t1.getDeadline() == null) return 1;
                    if (t2.getDeadline() == null) return -1;
                    return t1.getDeadline().compareTo(t2.getDeadline());
                case 2: // Độ ưu tiên (Cao -> Thấp)
                    return Integer.compare(t1.getPriority(), t2.getPriority());
                case 3: // Tên (A-Z)
                    if (t1.getTitle() == null) return 1;
                    if (t2.getTitle() == null) return -1;
                    return t1.getTitle().compareToIgnoreCase(t2.getTitle());
                case 0:
                default: // Mặc định (Ngày tạo mới nhất lên đầu)
                    if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
                    if (t1.getCreatedAt() == null) return 1;
                    if (t2.getCreatedAt() == null) return -1;
                    return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            }
        });

        filteredTasks.setValue(new ArrayList<>(result));
    }

    /**
     * Cập nhật search query và áp dụng filter
     */
    public void setSearchQuery(String query) {
        this.currentSearch = query != null ? query : "";
        applyFilter();
    }

    /**
     * Đặt filter category
     */
    public void setFilterCategory(String categoryId) {
        this.filterCategoryId = categoryId;
        applyFilter();
    }

    /**
     * Đặt filter priority (0=all, 1=high, 2=medium, 3=low)
     */
    public void setFilterPriority(int priority) {
        this.filterPriority = priority;
        applyFilter();
    }

    /**
     * Đặt filter status (0=all, 1=completed, 2=pending)
     */
    public void setFilterStatus(int status) {
        this.filterStatus = status;
        applyFilter();
    }

    /**
     * Xóa tất cả filter
     */
    public void clearFilters() {
        currentSearch    = "";
        filterCategoryId = null;
        filterPriority   = 0;
        filterStatus     = 0;
        filterToday      = false;
        applyFilter();
    }

    public void setFilterToday(boolean today) {
        this.filterToday = today;
        applyFilter();
    }

    public void setSortOption(int option) {
        this.sortOption = option;
        applyFilter();
    }

    // ========== CRUD Operations (Optimistic Real-Time UI Updates) ==========

    /**
     * Thêm task mới (Cập nhật UI tức thì 0ms)
     */
    public void addTask(Task task) {
        isLoading.setValue(true);

        // Optimistic UI Add
        List<Task> currentList = allTasks.getValue();
        List<Task> newList = currentList != null ? new ArrayList<>(currentList) : new ArrayList<>();
        newList.add(0, cloneTask(task));
        allTasks.setValue(newList);
        applyFilter();

        taskRepository.addTask(task, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                successMessage.postValue("Task đã được thêm");
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                loadAllTasks();
            }
        });
    }

    /**
     * Cập nhật task (Deadline, Tên, Mô tả, Cảnh báo quá hạn, Độ ưu tiên... Cập nhật UI tức thì 0ms)
     */
    public void updateTask(Task task) {
        if (task == null || task.getTaskId() == null) return;
        isLoading.setValue(true);

        // Optimistic UI Update cho tất cả thuộc tính
        List<Task> currentList = allTasks.getValue();
        if (currentList != null) {
            List<Task> newList = new ArrayList<>();
            for (Task t : currentList) {
                if (task.getTaskId().equals(t.getTaskId())) {
                    newList.add(cloneTask(task));
                } else {
                    newList.add(t);
                }
            }
            allTasks.setValue(newList);
            applyFilter();
        }

        taskRepository.updateTask(task, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                successMessage.postValue("Task đã được cập nhật");
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                loadAllTasks();
            }
        });
    }

    /**
     * Xóa task (Xóa khỏi UI tức thì 0ms)
     */
    public void deleteTask(String taskId) {
        if (taskId == null) return;

        // Optimistic UI Delete
        List<Task> currentList = allTasks.getValue();
        if (currentList != null) {
            List<Task> newList = new ArrayList<>();
            for (Task t : currentList) {
                if (!taskId.equals(t.getTaskId())) {
                    newList.add(t);
                }
            }
            allTasks.setValue(newList);
            applyFilter();
        }

        taskRepository.deleteTask(taskId, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() {
                successMessage.postValue("Task đã được xóa");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                loadAllTasks();
            }
        });
    }

    /**
     * Toggle complete/incomplete (Cập nhật UI tức thì 0ms)
     */
    public void toggleComplete(String taskId, boolean completed) {
        if (taskId == null) return;

        // Optimistic UI Toggle
        List<Task> currentList = allTasks.getValue();
        if (currentList != null) {
            List<Task> newList = new ArrayList<>();
            for (Task t : currentList) {
                if (taskId.equals(t.getTaskId())) {
                    Task copy = cloneTask(t);
                    copy.setCompleted(completed);
                    newList.add(copy);
                } else {
                    newList.add(t);
                }
            }
            allTasks.setValue(newList);
            applyFilter();
        }

        // Gửi cập nhật lên Firestore
        taskRepository.toggleTaskComplete(taskId, completed, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() {
                // Nếu task recurring và được mark complete, tạo task tiếp theo
                if (completed) {
                    Task task = findTaskById(taskId);
                    if (task != null && task.isRecurring()) {
                        taskRepository.createNextRecurrence(task, new OnTaskListener.OnTaskOperationComplete() {
                            @Override
                            public void onSuccess() {
                                successMessage.postValue("🔄 Task lặp lại đã được tạo");
                            }
                            @Override
                            public void onFailure(String error) {
                                errorMessage.postValue("Lỗi tạo task lặp: " + error);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                loadAllTasks();
            }
        });
    }

    private Task cloneTask(Task orig) {
        Task task = new Task();
        task.setTaskId(orig.getTaskId());
        task.setUserId(orig.getUserId());
        task.setTitle(orig.getTitle());
        task.setDescription(orig.getDescription());
        task.setCategoryId(orig.getCategoryId());
        task.setPriority(orig.getPriority());
        task.setCompleted(orig.isCompleted());
        task.setDeadline(orig.getDeadline());
        task.setReminderTime(orig.getReminderTime());
        task.setImageUrl(orig.getImageUrl());
        task.setCreatedAt(orig.getCreatedAt());
        task.setUpdatedAt(orig.getUpdatedAt());
        task.setSubtaskCount(orig.getSubtaskCount());
        task.setSubtaskCompleted(orig.getSubtaskCompleted());
        task.setRecurrenceType(orig.getRecurrenceType());
        task.setRecurrenceInterval(orig.getRecurrenceInterval());
        task.setRecurrenceDays(orig.getRecurrenceDays());
        return task;
    }

    /**
     * Tìm task theo ID trong danh sách hiện tại
     */
    private Task findTaskById(String taskId) {
        List<Task> tasks = allTasks.getValue();
        if (tasks == null || taskId == null) return null;
        for (Task t : tasks) {
            if (taskId.equals(t.getTaskId())) return t;
        }
        return null;
    }

    // ========== Subtask Operations ==========

    /**
     * Load subtasks cho một task cụ thể
     */
    public void loadSubtasks(String taskId) {
        taskRepository.getSubtasks(taskId, subtasks);
    }

    /**
     * Thêm subtask mới
     */
    public void addSubtask(String taskId, String title) {
        if (title == null || title.trim().isEmpty()) return;

        List<Subtask> currentList = subtasks.getValue();
        int order = currentList != null ? currentList.size() : 0;

        Subtask subtask = new Subtask(title.trim(), order);
        taskRepository.addSubtask(taskId, subtask, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() { /* Real-time listener sẽ cập nhật */ }
            @Override
            public void onFailure(String error) {
                errorMessage.postValue("Lỗi thêm công việc con: " + error);
            }
        });
    }

    /**
     * Toggle subtask complete
     */
    public void toggleSubtaskComplete(String taskId, Subtask subtask, boolean completed) {
        taskRepository.toggleSubtaskComplete(taskId, subtask.getSubtaskId(), completed,
                new OnTaskListener.OnTaskOperationComplete() {
                    @Override
                    public void onSuccess() { /* Real-time listener sẽ cập nhật */ }
                    @Override
                    public void onFailure(String error) {
                        errorMessage.postValue("Lỗi cập nhật: " + error);
                    }
                });
    }

    /**
     * Xóa subtask
     */
    public void deleteSubtask(String taskId, Subtask subtask) {
        taskRepository.deleteSubtask(taskId, subtask.getSubtaskId(), subtask.isCompleted(),
                new OnTaskListener.OnTaskOperationComplete() {
                    @Override
                    public void onSuccess() { /* Real-time listener sẽ cập nhật */ }
                    @Override
                    public void onFailure(String error) {
                        errorMessage.postValue("Lỗi xóa công việc con: " + error);
                    }
                });
    }

    /**
     * Upload ảnh cho task
     */
    public void uploadTaskImage(android.content.Context context, Uri imageUri) {
        isLoading.setValue(true);
        storageRepository.uploadTaskImage(context, imageUri, new StorageRepository.OnUploadComplete() {
            @Override
            public void onSuccess(String downloadUrl) {
                isLoading.postValue(false);
                uploadedImageUrl.postValue(downloadUrl);
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }

            @Override
            public void onProgress(int progress) {
                uploadProgress.postValue(progress);
            }
        });
    }

    /**
     * Clear messages
     */
    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    /**
     * Tải danh sách task trong thùng rác
     */
    public void loadTrashedTasks() {
        taskRepository.getTrashedTasks(trashedTasks);
    }

    /**
     * Khôi phục task từ thùng rác
     */
    public void restoreTask(String taskId) {
        if (taskId == null) return;
        
        // Optimistic update for Trash UI
        List<Task> currentTrash = trashedTasks.getValue();
        if (currentTrash != null) {
            List<Task> newTrash = new ArrayList<>();
            for (Task t : currentTrash) {
                if (!taskId.equals(t.getTaskId())) {
                    newTrash.add(t);
                }
            }
            trashedTasks.setValue(newTrash);
        }

        taskRepository.restoreTask(taskId, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() {
                successMessage.postValue("Đã khôi phục task");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                loadTrashedTasks();
            }
        });
    }

    /**
     * Xóa vĩnh viễn task
     */
    public void hardDeleteTask(String taskId) {
        if (taskId == null) return;
        
        // Optimistic update for Trash UI
        List<Task> currentTrash = trashedTasks.getValue();
        if (currentTrash != null) {
            List<Task> newTrash = new ArrayList<>();
            for (Task t : currentTrash) {
                if (!taskId.equals(t.getTaskId())) {
                    newTrash.add(t);
                }
            }
            trashedTasks.setValue(newTrash);
        }

        taskRepository.hardDeleteTask(taskId, new OnTaskListener.OnTaskOperationComplete() {
            @Override
            public void onSuccess() {
                successMessage.postValue("Đã xóa vĩnh viễn task");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                loadTrashedTasks();
            }
        });
    }
}
