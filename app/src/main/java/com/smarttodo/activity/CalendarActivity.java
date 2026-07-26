package com.smarttodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.applandeo.materialcalendarview.EventDay;
import com.smarttodo.R;
import com.smarttodo.adapter.TaskAdapter;
import com.smarttodo.databinding.ActivityCalendarBinding;
import com.smarttodo.model.Task;
import com.smarttodo.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private ActivityCalendarBinding binding;
    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;
    private long selectedDateMillis;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDateMillis = System.currentTimeMillis();

        setupToolbar();
        setupRecyclerView();
        setupCalendar();
        setupViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(CalendarActivity.this, TaskDetailActivity.class);
                intent.putExtra("taskId", task.getTaskId());
                startActivity(intent);
            }

            @Override
            public void onTaskLongClick(Task task) {}
            
            @Override
            public void onDeleteClick(Task task) {}

            @Override
            public void onCompleteToggle(Task task, boolean isCompleted) {
                Task updatedTask = new Task();
                updatedTask.setTaskId(task.getTaskId());
                updatedTask.setTitle(task.getTitle());
                updatedTask.setDescription(task.getDescription());
                updatedTask.setPriority(task.getPriority());
                updatedTask.setCategoryId(task.getCategoryId());
                updatedTask.setDeadline(task.getDeadline());
                updatedTask.setReminderTime(task.getReminderTime());
                updatedTask.setCompleted(isCompleted);
                updatedTask.setCreatedAt(task.getCreatedAt());
                updatedTask.setUpdatedAt(new Date());
                updatedTask.setDeletedAt(task.getDeletedAt());
                
                taskViewModel.updateTask(updatedTask);
            }
        });
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupCalendar() {
        binding.calendarView.setOnDayClickListener(eventDay -> {
            Calendar calendar = eventDay.getCalendar();
            selectedDateMillis = calendar.getTimeInMillis();
            
            updateSelectedDateText(calendar.getTime());
            filterTasksByDate(taskViewModel.allTasks.getValue());
        });
        updateSelectedDateText(new Date(selectedDateMillis));
    }

    private void setupViewModel() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        
        taskViewModel.allTasks.observe(this, tasks -> {
            filterTasksByDate(tasks);
        });
        
        taskViewModel.loadAllTasks();
    }

    private void updateSelectedDateText(Date date) {
        String dateStr = sdf.format(date);
        binding.tvSelectedDate.setText("Công việc ngày " + dateStr);
    }

    private void filterTasksByDate(List<Task> allTasks) {
        if (allTasks == null) return;

        List<Task> filteredTasks = new ArrayList<>();
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateMillis);
        
        List<EventDay> events = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getDeadline() != null && task.getDeletedAt() == null) {
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTime(task.getDeadline());
                
                // Thêm chấm đỏ cho ngày có deadline chưa hoàn thành
                if (!task.isCompleted()) {
                    Calendar eventCal = Calendar.getInstance();
                    eventCal.setTime(task.getDeadline());
                    events.add(new EventDay(eventCal, R.drawable.ic_dot_red));
                }
                
                if (selectedCal.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.DAY_OF_YEAR) == taskCal.get(Calendar.DAY_OF_YEAR)) {
                    filteredTasks.add(task);
                }
            }
        }
        
        // Gán sự kiện vào lịch
        binding.calendarView.setEvents(events);
        
        taskAdapter.submitList(filteredTasks);
        updateEmptyState(filteredTasks);
    }

    private void updateEmptyState(List<Task> tasks) {
        boolean isEmpty = tasks == null || tasks.isEmpty();
        binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
