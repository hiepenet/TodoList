package com.smarttodo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.model.Task;
import com.smarttodo.utils.Constants;
import com.smarttodo.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel tính toán thống kê task (Real-time).
 */
public class StatisticsViewModel extends ViewModel {

    private final FirebaseFirestore db;

    public final MutableLiveData<Integer> totalTasks     = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> completedTasks = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> pendingTasks   = new MutableLiveData<>(0);
    public final MutableLiveData<Float>   completionRate = new MutableLiveData<>(0f);

    public final MutableLiveData<Map<String, Integer>> tasksByCategory = new MutableLiveData<>();
    public final MutableLiveData<Map<String, Integer>> tasksByMonth = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isLoading    = new MutableLiveData<>(false);
    public final MutableLiveData<String>  errorMessage = new MutableLiveData<>();

    public StatisticsViewModel() {
        db = FirebaseManager.getInstance().getFirestore();
    }

    private String getUserId() {
        return FirebaseManager.getInstance().getCurrentUserId();
    }

    /**
     * Load tất cả thống kê với SnapshotListener (Real-Time Sync)
     */
    public void loadStatistics() {
        String uid = getUserId();
        if (uid == null) return;
        isLoading.setValue(true);

        db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo(Constants.FIELD_USER_ID, uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        errorMessage.postValue(error.getMessage());
                        isLoading.postValue(false);
                        return;
                    }

                    List<Task> tasks = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                Task task = doc.toObject(Task.class);
                                if (task != null) {
                                    task.setTaskId(doc.getId());
                                    tasks.add(task);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    calculateStatistics(tasks);
                    isLoading.postValue(false);
                });
    }

    /**
     * Tính toán các thống kê từ danh sách task
     */
    private void calculateStatistics(List<Task> tasks) {
        int total     = tasks.size();
        int completed = 0;

        Map<String, Integer> byCategory = new HashMap<>();
        Map<String, Integer> byMonth    = new HashMap<>();

        for (Task task : tasks) {
            if (task.isCompleted()) completed++;

            String catId = task.getCategoryId();
            if (catId != null) {
                int count = byCategory.containsKey(catId) ? byCategory.get(catId) : 0;
                byCategory.put(catId, count + 1);
            }

            if (task.getCreatedAt() != null) {
                String monthKey = DateUtils.formatMonth(task.getCreatedAt());
                int count = byMonth.containsKey(monthKey) ? byMonth.get(monthKey) : 0;
                byMonth.put(monthKey, count + 1);
            }
        }

        int pending = total - completed;
        float rate  = total > 0 ? (float) completed / total * 100 : 0f;

        totalTasks.postValue(total);
        completedTasks.postValue(completed);
        pendingTasks.postValue(pending);
        completionRate.postValue(rate);
        tasksByCategory.postValue(byCategory);
        tasksByMonth.postValue(byMonth);
    }
}
