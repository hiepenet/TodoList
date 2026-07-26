package com.smarttodo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class xử lý ngày giờ cho ứng dụng.
 */
public final class DateUtils {

    private DateUtils() {} // Ngăn khởi tạo

    /**
     * Định dạng Date thành chuỗi hiển thị đầy đủ "dd/MM/yyyy HH:mm"
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Định dạng Date chỉ lấy ngày "dd/MM/yyyy"
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DATE, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Định dạng Date chỉ lấy giờ "HH:mm"
     */
    public static String formatTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_TIME, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Định dạng Date thành tháng/năm "MM/yyyy"
     */
    public static String formatMonth(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_MONTH, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Kiểm tra date có phải hôm nay không
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Lấy start of day cho một date
     */
    public static Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Lấy end of day cho một date
     */
    public static Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * Lấy start of month
     */
    public static Date getStartOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Lấy end of month
     */
    public static Date getEndOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 23, 59, 59);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    /**
     * Tạo Date từ year, month, day, hour, minute
     */
    public static Date createDate(int year, int month, int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Lấy millis từ Date (dùng cho AlarmManager)
     */
    public static long getMillis(Date date) {
        return date != null ? date.getTime() : 0;
    }

    /**
     * Hiển thị thời gian tương đối ("Hôm nay", "Ngày mai", "2 ngày nữa", ...)
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "Chưa có hạn chót";
        
        long now = System.currentTimeMillis();
        long diff = date.getTime() - now;
        
        if (diff < 0) return "Đã quá hạn";
        
        long days = diff / (1000 * 60 * 60 * 24);
        
        if (days == 0) return "Hôm nay lúc " + formatTime(date);
        if (days == 1) return "Ngày mai lúc " + formatTime(date);
        if (days < 7)  return days + " ngày nữa";
        return formatDate(date);
    }
}
