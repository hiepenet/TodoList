package com.smarttodo.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.smarttodo.databinding.ActivitySplashBinding;
import com.smarttodo.firebase.FirebaseManager;
import com.smarttodo.notification.NotificationHelper;
import com.smarttodo.utils.ThemeUtils;

/**
 * SplashActivity - màn hình khởi động.
 * Hiển thị logo, kiểm tra trạng thái đăng nhập và chuyển hướng.
 * Logic nghiệp vụ: kiểm tra Firebase auth state.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2000; // 2 giây
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Notification Channel ngay từ đầu
        new NotificationHelper(this);

        // Chạy animation cho logo
        runLogoAnimation();

        // Sau SPLASH_DELAY_MS, kiểm tra auth và chuyển màn hình
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndNavigate, SPLASH_DELAY_MS);
    }

    /**
     * Animation fade in + scale up cho logo
     */
    private void runLogoAnimation() {
        binding.ivLogo.setAlpha(0f);
        binding.ivLogo.setScaleX(0.5f);
        binding.ivLogo.setScaleY(0.5f);

        ObjectAnimator fadeIn   = ObjectAnimator.ofFloat(binding.ivLogo, "alpha",  0f, 1f);
        ObjectAnimator scaleX   = ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY   = ObjectAnimator.ofFloat(binding.ivLogo, "scaleY", 0.5f, 1f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, scaleX, scaleY);
        animSet.setDuration(800);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.start();

        // Fade in text sau khi logo xuất hiện
        binding.tvAppName.setAlpha(0f);
        binding.tvTagline.setAlpha(0f);
        binding.tvAppName.animate().alpha(1f).setStartDelay(400).setDuration(600).start();
        binding.tvTagline.animate().alpha(1f).setStartDelay(600).setDuration(600).start();
    }

    /**
     * Kiểm tra Firebase Authentication state.
     * Nếu đã đăng nhập -> MainActivity.
     * Nếu chưa -> LoginActivity.
     */
    private void checkAuthAndNavigate() {
        boolean isLoggedIn = FirebaseManager.getInstance().isUserLoggedIn();

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Không cho quay lại Splash screen
    }
}
