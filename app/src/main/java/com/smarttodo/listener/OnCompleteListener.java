package com.smarttodo.listener;

/**
 * Generic callback interface cho các thao tác (CRUD) đơn giản.
 */
public interface OnCompleteListener {
    void onSuccess(String message);
    void onFailure(String error);
}
