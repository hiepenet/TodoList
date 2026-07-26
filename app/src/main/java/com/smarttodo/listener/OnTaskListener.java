package com.smarttodo.listener;

import com.smarttodo.model.Task;
import java.util.List;

/**
 * Callback interfaces cho các thao tác liên quan đến Task.
 */
public interface OnTaskListener {

    /**
     * Callback khi lấy danh sách task
     */
    interface OnTasksLoaded {
        void onSuccess(List<Task> tasks);
        void onFailure(String error);
    }

    /**
     * Callback khi thao tác CRUD thành công/thất bại
     */
    interface OnTaskOperationComplete {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Callback khi lấy một task đơn lẻ
     */
    interface OnTaskLoaded {
        void onSuccess(Task task);
        void onFailure(String error);
    }
}
