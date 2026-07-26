package com.smarttodo.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.smarttodo.firebase.FirebaseManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Repository xử lý upload file lên Cloudinary.
 * Chuyển đổi content:// Uri sang Temp File để tránh lỗi Android URI Permission Denial.
 */
public class StorageRepository {

    private static final String UPLOAD_PRESET = "ufkijqov";

    public StorageRepository() {}

    private String getUserId() {
        return FirebaseManager.getInstance().getCurrentUserId();
    }

    public interface OnUploadComplete {
        void onSuccess(String url);
        void onProgress(int progress);
        void onFailure(String error);
    }

    /**
     * Tạo File tạm từ content:// Uri để Cloudinary có thể đọc tệp an toàn
     */
    private File getFileFromUri(Context context, Uri uri) {
        if (uri == null || context == null) return null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            Log.e("StorageRepository", "Error copying Uri to file: ", e);
            return null;
        }
    }

    /**
     * Upload ảnh của Task lên Cloudinary
     */
    public void uploadTaskImage(Context context, Uri imageUri, OnUploadComplete listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onFailure("User chưa đăng nhập");
            return;
        }

        File file = getFileFromUri(context, imageUri);
        if (file == null || !file.exists()) {
            listener.onFailure("Không thể đọc tệp ảnh từ thiết bị");
            return;
        }

        MediaManager.get().upload(file.getAbsolutePath())
                .unsigned(UPLOAD_PRESET)
                .option("folder", "smarttodo/tasks/" + uid)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        listener.onProgress(0);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        listener.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        try { file.delete(); } catch (Exception ignored) {}
                        listener.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload error: " + error.getDescription());
                        try { file.delete(); } catch (Exception ignored) {}
                        listener.onFailure(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        try { file.delete(); } catch (Exception ignored) {}
                        listener.onFailure("Upload bị hoãn: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Upload Avatar của User lên Cloudinary
     */
    public void uploadAvatar(Context context, Uri imageUri, OnUploadComplete listener) {
        String uid = getUserId();
        if (uid == null) {
            listener.onFailure("User chưa đăng nhập");
            return;
        }

        File file = getFileFromUri(context, imageUri);
        if (file == null || !file.exists()) {
            listener.onFailure("Không thể đọc tệp ảnh từ thiết bị");
            return;
        }

        MediaManager.get().upload(file.getAbsolutePath())
                .unsigned(UPLOAD_PRESET)
                .option("folder", "smarttodo/avatars")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        listener.onProgress(0);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        listener.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        try { file.delete(); } catch (Exception ignored) {}
                        listener.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload avatar error: " + error.getDescription());
                        try { file.delete(); } catch (Exception ignored) {}
                        listener.onFailure(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        try { file.delete(); } catch (Exception ignored) {}
                        listener.onFailure("Upload avatar bị hoãn: " + error.getDescription());
                    }
                })
                .dispatch();
    }
}
