package com.smarttodo.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.smarttodo.R;

/**
 * ItemTouchHelper.Callback xử lý vuốt trái/phải trên task item.
 * Swipe phải → Hoàn thành (xanh lá)
 * Swipe trái → Xóa (đỏ)
 */
public class TaskSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface SwipeListener {
        void onSwipeComplete(int position);
        void onSwipeDelete(int position);
    }

    private final SwipeListener listener;
    private final Paint paintComplete;
    private final Paint paintDelete;
    private final Drawable iconComplete;
    private final Drawable iconDelete;
    private final int cornerRadius;

    public TaskSwipeCallback(Context context, SwipeListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;

        // Paint cho swipe phải (complete - xanh lá)
        paintComplete = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintComplete.setColor(Color.parseColor("#4CAF50"));

        // Paint cho swipe trái (delete - đỏ)
        paintDelete = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDelete.setColor(Color.parseColor("#F44336"));

        // Icons
        iconComplete = ContextCompat.getDrawable(context, R.drawable.ic_check_circle);
        iconDelete = ContextCompat.getDrawable(context, R.drawable.ic_delete);

        // Corner radius (16dp)
        cornerRadius = (int) (16 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           @NonNull RecyclerView.ViewHolder target) {
        return false; // Không hỗ trợ drag & drop
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;
        
        viewHolder.itemView.performHapticFeedback(android.view.HapticFeedbackConstants.REJECT);

        if (direction == ItemTouchHelper.RIGHT) {
            listener.onSwipeComplete(position);
        } else if (direction == ItemTouchHelper.LEFT) {
            listener.onSwipeDelete(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                             @NonNull RecyclerView.ViewHolder viewHolder,
                             float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        if (dX > 0) {
            // Swipe phải → Complete (xanh lá)
            RectF bg = new RectF(
                    itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + dX + cornerRadius, itemView.getBottom()
            );
            c.drawRoundRect(bg, cornerRadius, cornerRadius, paintComplete);

            // Vẽ icon check
            if (iconComplete != null) {
                int iconMargin = (itemHeight - iconComplete.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + iconComplete.getIntrinsicHeight();
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + iconComplete.getIntrinsicWidth();
                iconComplete.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                iconComplete.setTint(Color.WHITE);
                iconComplete.draw(c);
            }

        } else if (dX < 0) {
            // Swipe trái → Delete (đỏ)
            RectF bg = new RectF(
                    itemView.getRight() + dX - cornerRadius, itemView.getTop(),
                    itemView.getRight(), itemView.getBottom()
            );
            c.drawRoundRect(bg, cornerRadius, cornerRadius, paintDelete);

            // Vẽ icon delete
            if (iconDelete != null) {
                int iconMargin = (itemHeight - iconDelete.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + iconDelete.getIntrinsicHeight();
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = iconRight - iconDelete.getIntrinsicWidth();
                iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                iconDelete.setTint(Color.WHITE);
                iconDelete.draw(c);
            }
        }

        // Giảm alpha khi vuốt xa
        float alpha = 1.0f - Math.abs(dX) / (float) itemView.getWidth();
        itemView.setAlpha(Math.max(alpha, 0.3f));
        itemView.setTranslationX(dX);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.35f; // Vuốt 35% chiều rộng để trigger action
    }
}
