package com.smarttodo.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smarttodo.listener.OnCompleteListener;
import com.smarttodo.model.User;
import com.smarttodo.repository.StorageRepository;
import com.smarttodo.repository.UserRepository;

/**
 * ViewModel quản lý Profile user.
 */
public class ProfileViewModel extends ViewModel {

    private final UserRepository    userRepository;
    private final StorageRepository storageRepository;

    public final MutableLiveData<User>    currentUser    = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading      = new MutableLiveData<>(false);
    public final MutableLiveData<String>  errorMessage   = new MutableLiveData<>();
    public final MutableLiveData<String>  successMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        userRepository    = new UserRepository();
        storageRepository = new StorageRepository();
    }

    /**
     * Load thông tin user
     */
    public void loadUserProfile() {
        userRepository.getUserProfile(currentUser);
    }

    /**
     * Cập nhật tên người dùng
     */
    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) return;
        isLoading.setValue(true);
        userRepository.updateName(newName.trim(), new OnCompleteListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue("Tên đã được cập nhật");
                loadUserProfile();
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Upload avatar và cập nhật Firestore
     */
    public void updateAvatar(Context context, Uri imageUri) {
        isLoading.setValue(true);
        storageRepository.uploadAvatar(context, imageUri, new StorageRepository.OnUploadComplete() {
            @Override
            public void onSuccess(String downloadUrl) {
                // Cập nhật URL vào Firestore
                userRepository.updateAvatar(downloadUrl, new OnCompleteListener() {
                    @Override
                    public void onSuccess(String message) {
                        isLoading.postValue(false);
                        successMessage.postValue("Ảnh đại diện đã được cập nhật");
                        loadUserProfile();
                    }

                    @Override
                    public void onFailure(String error) {
                        isLoading.postValue(false);
                        errorMessage.postValue(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }

            @Override
            public void onProgress(int progress) {}
        });
    }
}
