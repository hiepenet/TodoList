package com.smarttodo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smarttodo.R;
import com.smarttodo.databinding.ItemTaskBinding;
import com.smarttodo.model.Category;
import com.smarttodo.model.Task;
import com.smarttodo.utils.DateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adapter hiển thị danh sách Task trong RecyclerView.
 * Sử dụng ListAdapter với DiffUtil toàn diện để update real-time hiệu quả.
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    // Interface callback cho click events
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onCompleteToggle(Task task, boolean isCompleted);
        void onDeleteClick(Task task);
    }

    private final Context context;
    private OnTaskClickListener clickListener;

    // Cache category info để hiển thị trong item
    private Map<String, Category> categoryMap = new HashMap<>();

    // DiffUtil callback toàn diện - so sánh tất cả thuộc tính (Deadline, Warning, Title, Desc, Cat, Priority, Status)
    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTaskId() != null && oldItem.getTaskId().equals(newItem.getTaskId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            boolean sameTitle    = Objects.equals(oldItem.getTitle(), newItem.getTitle());
            boolean sameDesc     = Objects.equals(oldItem.getDescription(), newItem.getDescription());
            boolean sameCat      = Objects.equals(oldItem.getCategoryId(), newItem.getCategoryId());
            boolean sameImage    = Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl());
            boolean sameDeadline = Objects.equals(oldItem.getDeadline(), newItem.getDeadline());

            return oldItem.isCompleted() == newItem.isCompleted()
                    && oldItem.getPriority() == newItem.getPriority()
                    && oldItem.getSubtaskCount() == newItem.getSubtaskCount()
                    && oldItem.getSubtaskCompleted() == newItem.getSubtaskCompleted()
                    && oldItem.getRecurrenceType() == newItem.getRecurrenceType()
                    && sameTitle
                    && sameDesc
                    && sameCat
                    && sameImage
                    && sameDeadline;
        }
    };

    public TaskAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Cập nhật category map để hiển thị tên category trong item
     */
    public void setCategoryMap(Map<String, Category> categoryMap) {
        this.categoryMap = categoryMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);

        // Micro-animation cho item khi xuất hiện
        holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemTaskBinding binding;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task) {
            // ===== Title =====
            binding.tvTaskTitle.setText(task.getTitle());
            if (task.isCompleted()) {
                binding.tvTaskTitle.setPaintFlags(binding.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvTaskTitle.setAlpha(0.6f);
            } else {
                binding.tvTaskTitle.setPaintFlags(
                        binding.tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvTaskTitle.setAlpha(1f);
            }

            // ===== Description =====
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                binding.tvTaskDesc.setVisibility(View.VISIBLE);
                binding.tvTaskDesc.setText(task.getDescription());
            } else {
                binding.tvTaskDesc.setVisibility(View.GONE);
            }

            // ===== Priority Badge =====
            switch (task.getPriority()) {
                case Task.PRIORITY_HIGH:
                    binding.tvPriority.setText("● Cao");
                    binding.tvPriority.setTextColor(Color.parseColor("#F44336"));
                    binding.tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
                    break;
                case Task.PRIORITY_MEDIUM:
                    binding.tvPriority.setText("● Trung bình");
                    binding.tvPriority.setTextColor(Color.parseColor("#FF9800"));
                    binding.tvPriority.setBackgroundResource(R.drawable.bg_priority_medium);
                    break;
                case Task.PRIORITY_LOW:
                    binding.tvPriority.setText("● Thấp");
                    binding.tvPriority.setTextColor(Color.parseColor("#4CAF50"));
                    binding.tvPriority.setBackgroundResource(R.drawable.bg_priority_low);
                    break;
            }

            // ===== Deadline & Overdue Warning =====
            if (task.getDeadline() != null) {
                binding.tvDeadline.setVisibility(View.VISIBLE);

                if (task.isOverdue() && !task.isCompleted()) {
                    binding.tvDeadline.setText("⚠️ Quá hạn: " + DateUtils.getRelativeTime(task.getDeadline()));
                    binding.tvDeadline.setTextColor(Color.parseColor("#F44336"));
                } else {
                    binding.tvDeadline.setText("📅 " + DateUtils.getRelativeTime(task.getDeadline()));
                    binding.tvDeadline.setTextColor(Color.parseColor("#AAAAAA"));
                }
            } else {
                binding.tvDeadline.setVisibility(View.GONE);
            }

            // ===== Category Badge =====
            Category cat = categoryMap.get(task.getCategoryId());
            if (cat != null) {
                binding.tvCategory.setVisibility(View.VISIBLE);
                binding.tvCategory.setText(cat.getName());
                try {
                    int color = Color.parseColor(cat.getColor());
                    binding.tvCategory.setTextColor(color);
                    binding.tvCategory.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                    Color.argb(30, Color.red(color), Color.green(color), Color.blue(color))));
                } catch (Exception e) {
                    binding.tvCategory.setTextColor(Color.parseColor("#4CAF50"));
                }
            } else {
                binding.tvCategory.setVisibility(View.GONE);
            }

            // ===== Task Image =====
            if (task.getImageUrl() != null && !task.getImageUrl().trim().isEmpty()) {
                binding.ivTaskImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(task.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_image)
                        .into(binding.ivTaskImage);
            } else {
                binding.ivTaskImage.setVisibility(View.GONE);
            }

            // ===== Subtask Progress =====
            if (task.hasSubtasks()) {
                binding.layoutSubtaskProgress.setVisibility(View.VISIBLE);
                int total = task.getSubtaskCount();
                int done = task.getSubtaskCompleted();
                int progress = total > 0 ? (done * 100 / total) : 0;
                binding.progressSubtask.setProgress(progress);
                binding.tvSubtaskCount.setText(done + "/" + total + " ✓");
            } else {
                binding.layoutSubtaskProgress.setVisibility(View.GONE);
            }

            // ===== Recurrence Badge =====
            if (task.isRecurring()) {
                binding.tvRecurrence.setVisibility(View.VISIBLE);
                binding.tvRecurrence.setText("🔄 " + task.getRecurrenceText());
            } else {
                binding.tvRecurrence.setVisibility(View.GONE);
            }

            // ===== Complete Toggle Button =====
            binding.btnComplete.setChecked(task.isCompleted());

            // ===== Click Listeners =====
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onTaskClick(task);
            });
            binding.getRoot().setOnLongClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                if (clickListener != null) clickListener.onTaskLongClick(task);
                return true;
            });
            binding.btnComplete.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                boolean targetStatus = binding.btnComplete.isChecked();
                task.setCompleted(targetStatus);
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
                }
                if (clickListener != null) clickListener.onCompleteToggle(task, targetStatus);
            });
            binding.btnDelete.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onDeleteClick(task);
            });

            // ===== Card opacity =====
            binding.getRoot().setAlpha(task.isCompleted() ? 0.7f : 1f);
        }
    }
}
