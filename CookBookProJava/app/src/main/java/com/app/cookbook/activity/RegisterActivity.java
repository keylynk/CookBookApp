package com.app.cookbook.activity;

import static com.app.cookbook.constant.GlobalFunction.showToastMessage;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.app.cookbook.R;
import com.app.cookbook.activity.admin.AdminMainActivity;
import com.app.cookbook.constant.Constant;
import com.app.cookbook.constant.GlobalFunction;
import com.app.cookbook.databinding.ActivityRegisterBinding;
import com.app.cookbook.model.User;
import com.app.cookbook.prefs.DataStoreManager;
import com.app.cookbook.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding mActivityRegisterBinding;
    private boolean isEnableButtonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityRegisterBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(mActivityRegisterBinding.getRoot());

        initListener();
    }

    private void initListener() {
        mActivityRegisterBinding.rdbUser.setChecked(true);
        mActivityRegisterBinding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityRegisterBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityRegisterBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                String strPassword = mActivityRegisterBinding.edtPassword.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strPassword)) {
                    isEnableButtonRegister = true;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonRegister = false;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityRegisterBinding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityRegisterBinding.edtPassword.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityRegisterBinding.edtPassword.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                String strEmail = mActivityRegisterBinding.edtEmail.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strEmail)) {
                    isEnableButtonRegister = true;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonRegister = false;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityRegisterBinding.layoutLogin.setOnClickListener(v -> finish());
        mActivityRegisterBinding.btnRegister.setOnClickListener(v -> onClickValidateRegister());
    }

    private void onClickValidateRegister() {
        if (!isEnableButtonRegister) return;

        String strEmail = mActivityRegisterBinding.edtEmail.getText().toString().trim();
        String strPassword = mActivityRegisterBinding.edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(this, getString(R.string.msg_email_require));
        } else if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(this, getString(R.string.msg_password_require));
        } else if (!StringUtil.isValidEmail(strEmail)) {
            showToastMessage(this, getString(R.string.msg_email_invalid));
        } else {
            if (mActivityRegisterBinding.rdbAdmin.isChecked()) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    showToastMessage(this, getString(R.string.msg_email_invalid_admin));
                } else {
                    registerUserFirebase(strEmail, strPassword);
                }
            } else {
                if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    showToastMessage(this, getString(R.string.msg_email_invalid_user));
                } else {
                    registerUserFirebase(strEmail, strPassword);
                }
            }
        }
    }

    private void registerUserFirebase(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
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
                        showToastMessage(this, getString(R.string.msg_register_error));
                    }
                });
    }

    private void goToMainActivity() {
        if (DataStoreManager.getUser().isAdmin()) {
            GlobalFunction.startActivity(RegisterActivity.this, AdminMainActivity.class);
        } else {
            GlobalFunction.startActivity(RegisterActivity.this, MainActivity.class);
        }
        finishAffinity();
    }
}