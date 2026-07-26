package com.smarttodo.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smarttodo.R;
import com.smarttodo.model.Subtask;

import java.util.Objects;

/**
 * Adapter hiển thị danh sách Subtask (công việc con) trong RecyclerView.
 * Sử dụng ListAdapter với DiffUtil để update hiệu quả.
 */
public class SubtaskAdapter extends ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder> {

    public interface OnSubtaskListener {
        void onToggleComplete(Subtask subtask, boolean isCompleted);
        void onDelete(Subtask subtask);
    }

    private OnSubtaskListener listener;

    private static final DiffUtil.ItemCallback<Subtask> DIFF_CALLBACK = new DiffUtil.ItemCallback<Subtask>() {
        @Override
        public boolean areItemsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            return oldItem.getSubtaskId() != null && oldItem.getSubtaskId().equals(newItem.getSubtaskId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            return oldItem.isCompleted() == newItem.isCompleted()
                    && Objects.equals(oldItem.getTitle(), newItem.getTitle());
        }
    };

    public SubtaskAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnSubtaskListener(OnSubtaskListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtask, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class SubtaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbSubtask;
        private final TextView tvTitle;
        private final ImageButton btnDelete;

        SubtaskViewHolder(View itemView) {
            super(itemView);
            cbSubtask = itemView.findViewById(R.id.cbSubtask);
            tvTitle = itemView.findViewById(R.id.tvSubtaskTitle);
            btnDelete = itemView.findViewById(R.id.btnDeleteSubtask);
        }

        void bind(Subtask subtask) {
            tvTitle.setText(subtask.getTitle());

            // Gạch ngang khi completed
            if (subtask.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(1f);
            }

            // Set checkbox state mà không trigger listener
            cbSubtask.setOnCheckedChangeListener(null);
            cbSubtask.setChecked(subtask.isCompleted());

            cbSubtask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleComplete(subtask, isChecked);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(subtask);
                }
            });
        }
    }
}
