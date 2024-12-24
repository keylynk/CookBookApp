package com.app.cookbook.activity;

import static com.app.cookbook.constant.GlobalFunction.showToastMessage;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.app.cookbook.R;
import com.app.cookbook.activity.admin.AdminMainActivity;
import com.app.cookbook.constant.Constant;
import com.app.cookbook.constant.GlobalFunction;
import com.app.cookbook.databinding.ActivityLogInBinding;
import com.app.cookbook.model.User;
import com.app.cookbook.prefs.DataStoreManager;
import com.app.cookbook.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private ActivityLogInBinding mActivityLogInBinding;
    private boolean isEnableButtonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityLogInBinding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(mActivityLogInBinding.getRoot());

        initListener();
    }

    private void initListener() {
        mActivityLogInBinding.rdbUser.setChecked(true);
        mActivityLogInBinding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityLogInBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityLogInBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                String strPassword = mActivityLogInBinding.edtPassword.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strPassword)) {
                    isEnableButtonLogin = true;
                    mActivityLogInBinding.btnLogin.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonLogin = false;
                    mActivityLogInBinding.btnLogin.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityLogInBinding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityLogInBinding.edtPassword.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityLogInBinding.edtPassword.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                String strEmail = mActivityLogInBinding.edtEmail.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strEmail)) {
                    isEnableButtonLogin = true;
                    mActivityLogInBinding.btnLogin.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonLogin = false;
                    mActivityLogInBinding.btnLogin.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityLogInBinding.layoutRegister.setOnClickListener(
                v -> GlobalFunction.startActivity(this, RegisterActivity.class));

        mActivityLogInBinding.btnLogin.setOnClickListener(v -> onClickValidateLogin());
        mActivityLogInBinding.tvForgotPassword.setOnClickListener(
                v -> GlobalFunction.startActivity(this, ForgotPasswordActivity.class));
    }

    private void onClickValidateLogin() {
        if (!isEnableButtonLogin) return;

        String strEmail = mActivityLogInBinding.edtEmail.getText().toString().trim();
        String strPassword = mActivityLogInBinding.edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(this, getString(R.string.msg_email_require));
        } else if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(this, getString(R.string.msg_password_require));
        } else if (!StringUtil.isValidEmail(strEmail)) {
            showToastMessage(this, getString(R.string.msg_email_invalid));
        } else {
            if (mActivityLogInBinding.rdbAdmin.isChecked()) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    showToastMessage(this, getString(R.string.msg_email_invalid_admin));
                } else {
                    loginUserFirebase(strEmail, strPassword);
                }
            } else {
                if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    showToastMessage(this, getString(R.string.msg_email_invalid_user));
                } else {
                    loginUserFirebase(strEmail, strPassword);
                }
            }
        }
    }

    private void loginUserFirebase(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            User userObject = new User(user.getEmail(), password);
                            if (user.getEmail() != null && user.getEmail().contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.setAdmin(true);
                            }
                            DataStoreManager.setUser(userObject);
                            goToMainActivity();
                        }
                    } else {
                        showToastMessage(this, getString(R.string.msg_login_error));
                    }
                });
    }

    private void goToMainActivity() {
        if (DataStoreManager.getUser().isAdmin()) {
            GlobalFunction.startActivity(LoginActivity.this, AdminMainActivity.class);
        } else {
            GlobalFunction.startActivity(LoginActivity.this, MainActivity.class);
        }
        finishAffinity();
    }
}