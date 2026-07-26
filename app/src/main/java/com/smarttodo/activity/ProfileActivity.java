package com.smarttodo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.smarttodo.R;
import com.smarttodo.databinding.ActivityProfileBinding;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.utils.PreferenceManager;
import com.smarttodo.utils.ThemeUtils;
import com.smarttodo.viewmodel.ProfileViewModel;
import com.smarttodo.viewmodel.StatisticsViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Activity hiển thị và chỉnh sửa hồ sơ người dùng.
 * Cho phép xem thống kê, cập nhật tên và ảnh đại diện.
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel       profileViewModel;
    private StatisticsViewModel    statisticsViewModel;

    private final ActivityResultLauncher<Intent> avatarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null && profileViewModel != null) {
                        // 1. Sao chép ảnh vào bộ nhớ trong máy vĩnh viễn
                        String localFilePath = copyUriToInternalStorage(imageUri);
                        if (localFilePath != null) {
                            PreferenceManager.getInstance(this).saveUserAvatar(localFilePath);
                        } else {
                            PreferenceManager.getInstance(this).saveUserAvatar(imageUri.toString());
                        }

                        // 2. Hiển thị xem trước ảnh đại diện mới ngay lập tức (0ms độ trễ)
                        if (binding != null && binding.ivAvatar != null) {
                            try {
                                Object loadTarget = localFilePath != null ? new File(localFilePath) : imageUri;
                                Glide.with(getApplicationContext())
                                        .load(loadTarget)
                                        .circleCrop()
                                        .into(binding.ivAvatar);
                            } catch (Exception ignored) {}
                        }
                        profileViewModel.updateAvatar(this, imageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityProfileBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            profileViewModel    = new ViewModelProvider(this).get(ProfileViewModel.class);
            statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

            setupToolbar();
            loadInitialUserInfo(); // Tải ngay thông tin tên, email và avatar từ bộ nhớ máy
            setupObservers();
            setupClickListeners();

            profileViewModel.loadUserProfile();
            statisticsViewModel.loadStatistics();
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error in onCreate: ", e);
        }
    }

    /**
     * Copy content Uri vào bộ nhớ trong app vĩnh viễn
     */
    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File avatarFile = new File(getFilesDir(), "user_avatar_permanent.jpg");
            FileOutputStream outputStream = new FileOutputStream(avatarFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return avatarFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error saving local avatar file: ", e);
            return null;
        }
    }

    /**
     * Tải ngay lập tức thông tin tên, email & avatar từ FirebaseUser & PreferenceManager
     */
    private void loadInitialUserInfo() {
        FirebaseUser fbUser = FirebaseManager.getInstance().getCurrentUser();
        String email = "";
        String name = "";

        if (fbUser != null) {
            email = fbUser.getEmail() != null ? fbUser.getEmail() : "";
            name  = fbUser.getDisplayName();
        }

        if (name == null || name.trim().isEmpty()) {
            name = PreferenceManager.getInstance(this).getUserName();
        }

        if (name == null || name.trim().isEmpty()) {
            if (!email.isEmpty() && email.contains("@")) {
                name = email.substring(0, email.indexOf("@"));
            } else {
                name = "Người dùng";
            }
        }

        if (email.isEmpty()) {
            email = PreferenceManager.getInstance(this).getUserEmail();
        }

        if (binding.tvUserName != null && !name.isEmpty()) {
            binding.tvUserName.setText(name);
        }
        if (binding.tvEmail != null && !email.isEmpty()) {
            binding.tvEmail.setText(email);
        }

        // Tải avatar local/saved
        String savedAvatar = PreferenceManager.getInstance(this).getUserAvatar();
        if ((savedAvatar == null || savedAvatar.trim().isEmpty()) && fbUser != null && fbUser.getPhotoUrl() != null) {
            savedAvatar = fbUser.getPhotoUrl().toString();
        }

        if (savedAvatar != null && !savedAvatar.trim().isEmpty() && binding.ivAvatar != null) {
            try {
                Object loadTarget = savedAvatar.startsWith("/") ? new File(savedAvatar) : savedAvatar;
                Glide.with(getApplicationContext())
                        .load(loadTarget)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.ivAvatar);
            } catch (Exception ignored) {}
        }
    }

    private void setupToolbar() {
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupObservers() {
        if (profileViewModel == null || statisticsViewModel == null) return;

        profileViewModel.currentUser.observe(this, user -> {
            if (user == null) return;
            try {
                if (binding.tvUserName != null && user.getName() != null && !user.getName().isEmpty()) {
                    binding.tvUserName.setText(user.getName());
                    PreferenceManager.getInstance(this).saveUserName(user.getName());
                }
                if (binding.tvEmail != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                    binding.tvEmail.setText(user.getEmail());
                    PreferenceManager.getInstance(this).saveUserEmail(user.getEmail());
                }

                String avatarUrl = user.getAvatar();
                if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                    FirebaseUser fbUser = FirebaseManager.getInstance().getCurrentUser();
                    if (fbUser != null && fbUser.getPhotoUrl() != null) {
                        avatarUrl = fbUser.getPhotoUrl().toString();
                    }
                }
                if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                    avatarUrl = PreferenceManager.getInstance(this).getUserAvatar();
                }

                if (binding.ivAvatar != null && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    PreferenceManager.getInstance(this).saveUserAvatar(avatarUrl);
                    if (!isFinishing() && !isDestroyed()) {
                        Object loadTarget = avatarUrl.startsWith("/") ? new File(avatarUrl) : avatarUrl;
                        Glide.with(getApplicationContext())
                                .load(loadTarget)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(binding.ivAvatar);
                    }
                }
            } catch (Exception e) {
                Log.e("ProfileActivity", "Error setting user info: ", e);
            }
        });

        profileViewModel.isLoading.observe(this, isLoading -> {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            }
        });

        profileViewModel.successMessage.observe(this, msg -> {
            if (msg != null && binding != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.errorMessage.observe(this, err -> {
            if (err != null && binding != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        // Statistics Observers
        statisticsViewModel.totalTasks.observe(this, total -> {
            if (binding.tvTotalTasks != null) binding.tvTotalTasks.setText(String.valueOf(total));
        });

        statisticsViewModel.completedTasks.observe(this, completed -> {
            if (binding.tvCompletedTasks != null) binding.tvCompletedTasks.setText(String.valueOf(completed));
        });

        statisticsViewModel.pendingTasks.observe(this, pending -> {
            if (binding.tvPendingTasks != null) binding.tvPendingTasks.setText(String.valueOf(pending));
        });
    }

    private void setupClickListeners() {
        if (binding.btnChangeAvatar != null) {
            binding.btnChangeAvatar.setOnClickListener(v -> openGallery());
        }

        if (binding.layoutEditName != null) {
            binding.layoutEditName.setOnClickListener(v -> showEditNameDialog());
        }

        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> logout());
        }

        // Theme Switch listener
        if (binding.switchDarkMode != null) {
            boolean isDark = ThemeUtils.isDarkMode(this);
            binding.switchDarkMode.setChecked(isDark);
            updateThemeUI(isDark);

            binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateThemeUI(isChecked);
                ThemeUtils.setDarkMode(this, isChecked);
            });
        }
    }

    private void updateThemeUI(boolean isDark) {
        if (binding.tvThemeTitle != null) {
            binding.tvThemeTitle.setText(isDark ? "☀️ Chế độ sáng (Light Mode)" : "🌙 Chế độ tối (Dark Mode)");
        }
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi tên người dùng");

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextColor(android.graphics.Color.BLACK);
        String currentName = binding.tvUserName != null ? binding.tvUserName.getText().toString() : "";
        input.setText(currentName);
        input.setSelection(currentName.length());

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        builder.setView(input);
        input.setPadding(padding, padding, padding, padding);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                if (binding.tvUserName != null) binding.tvUserName.setText(newName);
                PreferenceManager.getInstance(this).saveUserName(newName);
                profileViewModel.updateName(newName);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        avatarLauncher.launch(intent);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    FirebaseManager.getInstance().getAuth().signOut();
                    PreferenceManager.getInstance(this).clearAll();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
