package com.app.cookbook.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.app.cookbook.activity.admin.AdminMainActivity;
import com.app.cookbook.constant.AboutUsConfig;
import com.app.cookbook.constant.GlobalFunction;
import com.app.cookbook.databinding.ActivitySplashBinding;
import com.app.cookbook.prefs.DataStoreManager;
import com.app.cookbook.utils.StringUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding mActivitySplashBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(mActivitySplashBinding.getRoot());

        initUi();

        Handler handler = new Handler();
        handler.postDelayed(this::goToActivity, 2000);
    }

    private void initUi() {
        mActivitySplashBinding.tvAboutUsTitle.setText(AboutUsConfig.ABOUT_US_TITLE);
        mActivitySplashBinding.tvAboutUsSlogan.setText(AboutUsConfig.ABOUT_US_SLOGAN);
    }

    private void goToActivity() {
        if (DataStoreManager.getUser() != null
                && !StringUtil.isEmpty(DataStoreManager.getUser().getEmail())) {
            if (DataStoreManager.getUser().isAdmin()) {
                GlobalFunction.startActivity(this, AdminMainActivity.class);
            } else {
                GlobalFunction.startActivity(this, MainActivity.class);
            }
        } else {
            GlobalFunction.startActivity(this, LoginActivity.class);
        }
        finish();
    }
}
