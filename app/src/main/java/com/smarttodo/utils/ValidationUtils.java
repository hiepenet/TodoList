package com.smarttodo.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.Date;

/**
 * Utility class xử lý validation cho form input.
 * Trả về null nếu hợp lệ, trả về message lỗi nếu không hợp lệ.
 */
public final class ValidationUtils {

    private ValidationUtils() {} // Ngăn khởi tạo

    /**
     * Validate email
     */
    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email không được để trống";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Email không hợp lệ";
        }
        return null;
    }

    /**
     * Validate password
     */
    public static String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Mật khẩu không được để trống";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        return null;
    }

    /**
     * Validate confirm password
     */
    public static String validateConfirmPassword(String password, String confirmPassword) {
        String result = validatePassword(confirmPassword);
        if (result != null) return result;
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu không khớp";
        }
        return null;
    }

    /**
     * Validate full name
     */
    public static String validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Tên không được để trống";
        }
        if (name.trim().length() < 2) {
            return "Tên phải có ít nhất 2 ký tự";
        }
        return null;
    }

    /**
     * Validate task title
     */
    public static String validateTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return "Tiêu đề không được để trống";
        }
        if (title.trim().length() < 1) {
            return "Tiêu đề không hợp lệ";
        }
        return null;
    }

    /**
     * Validate category selection
     */
    public static String validateCategory(String categoryId) {
        if (TextUtils.isEmpty(categoryId)) {
            return "Vui lòng chọn danh mục";
        }
        return null;
    }

    /**
     * Validate deadline phải lớn hơn thời gian hiện tại
     */
    public static String validateDeadline(Date deadline) {
        if (deadline == null) {
            return null; // Deadline không bắt buộc
        }
        if (deadline.before(new Date())) {
            return "Hạn chót phải lớn hơn thời gian hiện tại";
        }
        return null;
    }

    /**
     * Validate category name
     */
    public static String validateCategoryName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Tên danh mục không được để trống";
        }
        return null;
    }
}
