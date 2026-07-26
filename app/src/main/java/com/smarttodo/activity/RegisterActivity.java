package com.smarttodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.databinding.ActivityRegisterBinding;
import com.smarttodo.repository.CategoryRepository;
import com.smarttodo.utils.ValidationUtils;
import com.smarttodo.viewmodel.AuthViewModel;

/**
 * RegisterActivity - màn hình đăng ký tài khoản mới.
 * Sau khi đăng ký thành công, tạo danh mục mặc định cho user.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        authViewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        authViewModel.isLoggedIn.observe(this, isLoggedIn -> {
            if (Boolean.TRUE.equals(isLoggedIn)) {
                // Tạo danh mục mặc định cho user mới
                createDefaultCategories();
                navigateToMain();
            }
        });

        authViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLogin.setOnClickListener(v -> finish()); // Quay lại LoginActivity
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String name     = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email    = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        String confirm  = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        // Validate tất cả fields
        String nameError    = ValidationUtils.validateName(name);
        String emailError   = ValidationUtils.validateEmail(email);
        String passError    = ValidationUtils.validatePassword(password);
        String confirmError = ValidationUtils.validateConfirmPassword(password, confirm);

        binding.tilName.setError(nameError);
        binding.tilEmail.setError(emailError);
        binding.tilPassword.setError(passError);
        binding.tilConfirmPassword.setError(confirmError);

        if (nameError != null || emailError != null || passError != null || confirmError != null) return;

        com.smarttodo.utils.PreferenceManager.getInstance(this).saveUserName(name);
        com.smarttodo.utils.PreferenceManager.getInstance(this).saveUserEmail(email);
        authViewModel.register(name, email, password);
    }

    /**
     * Tạo các danh mục mặc định (Work, Study, Personal, ...) cho user mới
     */
    private void createDefaultCategories() {
        CategoryRepository repo = new CategoryRepository();
        repo.createDefaultCategories(new com.smarttodo.listener.OnCategoryListener.OnCategoryOperationComplete() {
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
