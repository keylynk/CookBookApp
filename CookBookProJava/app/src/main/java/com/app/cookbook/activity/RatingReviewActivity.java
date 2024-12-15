package com.app.cookbook.activity;

import android.os.Bundle;

import com.app.cookbook.MyApplication;
import com.app.cookbook.R;
import com.app.cookbook.constant.Constant;
import com.app.cookbook.constant.GlobalFunction;
import com.app.cookbook.databinding.ActivityRatingReviewBinding;
import com.app.cookbook.model.Rating;

public class RatingReviewActivity extends BaseActivity {

    private ActivityRatingReviewBinding mBinding;
    private long mFoodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRatingReviewBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;
        mFoodId = bundle.getLong(Constant.FOOD_ID);
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_rate_review));
    }

    private void initView() {
        mBinding.ratingbar.setRating(5f);
        mBinding.tvSendReview.setOnClickListener(v -> {
            float rate = mBinding.ratingbar.getRating();
            String review = mBinding.edtReview.getText().toString().trim();
            Rating rating = new Rating(review, Double.parseDouble(String.valueOf(rate)));
            sendRatingFood(rating);
        });
    }

    private void sendRatingFood(Rating rating) {
        MyApplication.get(this).ratingFoodDatabaseReference(mFoodId)
                .child(String.valueOf(GlobalFunction.encodeEmailUser()))
                .setValue(rating, (error, ref) -> {
                    GlobalFunction.showToastMessage(RatingReviewActivity.this,
                            getString(R.string.msg_send_review_success));
                    mBinding.ratingbar.setRating(5f);
                    mBinding.edtReview.setText("");
                    GlobalFunction.hideSoftKeyboard(RatingReviewActivity.this);
                });
    }
}
