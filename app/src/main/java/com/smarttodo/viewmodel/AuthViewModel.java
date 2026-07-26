package com.smarttodo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smarttodo.listener.OnCompleteListener;
import com.smarttodo.model.User;
import com.smarttodo.repository.AuthRepository;

/**
 * ViewModel xử lý Authentication logic.
 * Kết nối giữa View (Activity) và Repository.
 * Tuân theo MVVM pattern - UI không biết gì về data source.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    // LiveData để UI observe
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String>  errorMessage = new MutableLiveData<>();
    public final MutableLiveData<String>  successMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    public final MutableLiveData<User>    currentUser = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    /**
     * Đăng ký tài khoản
     */
    public void register(String name, String email, String password) {
        isLoading.setValue(true);
        authRepository.register(name, email, password, new OnCompleteListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
                isLoggedIn.postValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Đăng nhập
     */
    public void login(String email, String password) {
        isLoading.setValue(true);
        authRepository.login(email, password, new OnCompleteListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
                isLoggedIn.postValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        authRepository.logout();
        isLoggedIn.setValue(false);
    }

    /**
     * Gửi email reset password
     */
    public void sendPasswordReset(String email) {
        isLoading.setValue(true);
        authRepository.sendPasswordResetEmail(email, new OnCompleteListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
            }

            @Override
            public void onFailure(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public boolean checkIsLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /**
     * Lấy thông tin user hiện tại
     */
    public void loadCurrentUser() {
        authRepository.getCurrentUser(currentUser);
    }

    /**
     * Clear error message sau khi đã hiển thị
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}
