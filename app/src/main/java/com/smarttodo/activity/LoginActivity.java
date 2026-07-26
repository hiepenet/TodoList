package com.smarttodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttodo.databinding.ActivityLoginBinding;
import com.smarttodo.utils.ValidationUtils;
import com.smarttodo.viewmodel.AuthViewModel;

/**
 * LoginActivity - màn hình đăng nhập.
 * Validation đầu vào, observe LiveData từ ViewModel.
 * Không chứa logic nghiệp vụ - chỉ gọi ViewModel.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        // Observe loading state
        authViewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        // Observe success -> Navigate to MainActivity
        authViewModel.isLoggedIn.observe(this, isLoggedIn -> {
            if (Boolean.TRUE.equals(isLoggedIn)) {
                navigateToMain();
            }
        });

        // Observe error message
        authViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar(error);
                authViewModel.clearError();
            }
        });
    }

    private void setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Register link
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    /**
     * Validate input rồi gọi ViewModel để login
     */
    private void attemptLogin() {
        String email    = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        // Validate
        String emailError = ValidationUtils.validateEmail(email);
        String passError  = ValidationUtils.validatePassword(password);

        binding.tilEmail.setError(emailError);
        binding.tilPassword.setError(passError);

        if (emailError != null || passError != null) return;

        // Gọi ViewModel
        authViewModel.login(email, password);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(com.smarttodo.R.color.error, null))
                .show();
    }
}
