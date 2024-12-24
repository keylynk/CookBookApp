package com.app.cookbook.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.afollestad.materialdialogs.MaterialDialog;
import com.app.cookbook.MyApplication;
import com.app.cookbook.R;
import com.app.cookbook.adapter.CategoryHomeAdapter;
import com.app.cookbook.adapter.CategoryMenuAdapter;
import com.app.cookbook.adapter.FoodAdapter;
import com.app.cookbook.adapter.FoodFeaturedAdapter;
import com.app.cookbook.constant.Constant;
import com.app.cookbook.constant.GlobalFunction;
import com.app.cookbook.databinding.ActivityMainBinding;
import com.app.cookbook.listener.IOnClickFoodListener;
import com.app.cookbook.model.Category;
import com.app.cookbook.model.Food;
import com.app.cookbook.model.RequestFood;
import com.app.cookbook.model.User;
import com.app.cookbook.prefs.DataStoreManager;
import com.app.cookbook.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMainBinding mBinding;
    private CategoryMenuAdapter mCategoryMenuAdapter;
    private List<Category> mListCategory;
    private List<Category> mListCategoryHome;
    private List<Food> mListFood;
    private List<Food> mListFoodFeatured;
    private ValueEventListener mCategoryValueEventListener;
    private ValueEventListener mFoodValueEventListener;
    private final Handler mHandlerBanner = new Handler(Looper.getMainLooper());
    private final Runnable mRunnableBanner = new Runnable() {
        @Override
        public void run() {
            if (mListFoodFeatured == null || mListFoodFeatured.isEmpty()) return;
            if (mBinding.viewPager.getCurrentItem() == mListFoodFeatured.size() - 1) {
                mBinding.viewPager.setCurrentItem(0);
                return;
            }
            mBinding.viewPager.setCurrentItem(mBinding.viewPager.getCurrentItem() + 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initListener();
        initNavigationMenuLeft();
    }

    private void initToolbar() {
        mBinding.header.imgToolbar.setImageResource(R.drawable.ic_menu);
        mBinding.header.tvToolbarTitle.setText(getString(R.string.app_name));
    }

    private void initListener() {
        mBinding.header.imgToolbar.setOnClickListener(this);
        mBinding.tvRequestFood.setOnClickListener(this);
        mBinding.layoutFeedback.setOnClickListener(this);
        mBinding.layoutContact.setOnClickListener(this);
        mBinding.layoutChangePassword.setOnClickListener(this);
        mBinding.layoutSignOut.setOnClickListener(this);
        mBinding.viewAllCategory.setOnClickListener(this);
        mBinding.viewAllFood.setOnClickListener(this);
        mBinding.layoutSearch.setOnClickListener(this);
        mBinding.layoutFavorite.setOnClickListener(this);
        mBinding.layoutHistory.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_toolbar:
                mBinding.drawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.tv_request_food:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                showDialogRequestFood();
                break;

            case R.id.layout_feedback:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                GlobalFunction.startActivity(this, FeedbackActivity.class);
                break;

            case R.id.layout_contact:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                GlobalFunction.startActivity(this, ContactActivity.class);
                break;

            case R.id.layout_change_password:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                GlobalFunction.startActivity(this, ChangePasswordActivity.class);
                break;

            case R.id.layout_sign_out:
                onClickSignOut();
                break;

            case R.id.view_all_category:
                GlobalFunction.startActivity(this, ListCategoryActivity.class);
                break;

            case R.id.view_all_food:
                GlobalFunction.startActivity(this, ListFoodActivity.class);
                break;

            case R.id.layout_search:
                GlobalFunction.startActivity(this, SearchActivity.class);
                break;

            case R.id.layout_favorite:
                GlobalFunction.startActivity(this, FavoriteActivity.class);
                break;

            case R.id.layout_history:
                GlobalFunction.startActivity(this, HistoryActivity.class);
                break;
        }
    }

    private void initNavigationMenuLeft() {
        displayUserInformation();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.rcvCategory.setLayoutManager(linearLayoutManager);
        mListCategory = new ArrayList<>();
        mCategoryMenuAdapter = new CategoryMenuAdapter(mListCategory,
                category -> GlobalFunction.goToFoodByCategory(MainActivity.this, category));
        mBinding.rcvCategory.setAdapter(mCategoryMenuAdapter);

        loadListCategoryFromFirebase();
    }

    private void displayUserInformation() {
        User user = DataStoreManager.getUser();
        mBinding.tvUserEmail.setText(user.getEmail());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadListCategoryFromFirebase() {
        showProgressDialog(true);
        mCategoryValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resetListCategory();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category == null) return;
                    mListCategory.add(0, category);
                }
                if (mCategoryMenuAdapter != null) mCategoryMenuAdapter.notifyDataSetChanged();
                displayListCategoryHome();

                loadListFoodFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(true);
            }
        };
        MyApplication.get(this).categoryDatabaseReference().addValueEventListener(mCategoryValueEventListener);
    }

    private void displayListCategoryHome() {
        LinearLayoutManager layoutManagerHorizontal = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mBinding.rcvCategoryHome.setLayoutManager(layoutManagerHorizontal);
        CategoryHomeAdapter categoryHomeAdapter = new CategoryHomeAdapter(loadListCategoryHome(),
                category -> GlobalFunction.goToFoodByCategory(MainActivity.this, category));
        mBinding.rcvCategoryHome.setAdapter(categoryHomeAdapter);
    }

    private List<Category> loadListCategoryHome() {
        resetListCategoryHome();
        for (Category category : mListCategory) {
            if (mListCategoryHome.size() < Constant.MAX_SIZE_LIST_CATEGORY) {
                mListCategoryHome.add(category);
            }
        }
        return mListCategoryHome;
    }

    private void resetListCategory() {
        if (mListCategory == null) {
            mListCategory = new ArrayList<>();
        } else {
            mListCategory.clear();
        }
    }

    private void resetListCategoryHome() {
        if (mListCategoryHome == null) {
            mListCategoryHome = new ArrayList<>();
        } else {
            mListCategoryHome.clear();
        }
    }

    private void resetListFood() {
        if (mListFood == null) {
            mListFood = new ArrayList<>();
        } else {
            mListFood.clear();
        }
    }

    private void resetListFoodFeatured() {
        if (mListFoodFeatured == null) {
            mListFoodFeatured = new ArrayList<>();
        } else {
            mListFoodFeatured.clear();
        }
    }

    private void loadListFoodFromFirebase() {
        mFoodValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showProgressDialog(false);
                resetListFood();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Food food = dataSnapshot.getValue(Food.class);
                    if (food == null) return;
                    mListFood.add(0, food);
                }
                displayListFoodFeatured();
                displayListPopularFood();
                displayCountFoodOfCategory();
                displayCountFavorite();
                displayCountHistory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressDialog(false);
                GlobalFunction.showToastMessage(MainActivity.this, getString(R.string.msg_get_date_error));
            }
        };
        MyApplication.get(this).foodDatabaseReference().addValueEventListener(mFoodValueEventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayCountFoodOfCategory() {
        if (mListCategory == null || mListCategory.isEmpty()) return;
        for (Category category : mListCategory) {
            category.setCount(loadCountFoodOfCategory(category.getId()));
        }
        if (mCategoryMenuAdapter != null) mCategoryMenuAdapter.notifyDataSetChanged();
    }

    private int loadCountFoodOfCategory(long categoryId) {
        if (mListFood == null || mListFood.isEmpty()) return 0;
        List<Food> listFoods = new ArrayList<>();
        for (Food food : mListFood) {
            if (categoryId == food.getCategoryId()) {
                listFoods.add(food);
            }
        }
        return listFoods.size();
    }

    private void displayCountFavorite() {
        int countFavorite = 0;
        if (mListFood != null && !mListFood.isEmpty()) {
            List<Food> listFavorite = new ArrayList<>();
            for (Food food : mListFood) {
                if (GlobalFunction.isFavoriteFood(food)) {
                    listFavorite.add(food);
                }
            }
            countFavorite = listFavorite.size();
        }
        mBinding.tvCountFavorite.setText(String.valueOf(countFavorite));
    }

    private void displayCountHistory() {
        int countHistory = 0;
        if (mListFood != null && !mListFood.isEmpty()) {
            List<Food> listHistory = new ArrayList<>();
            for (Food food : mListFood) {
                if (GlobalFunction.isHistoryFood(food)) {
                    listHistory.add(food);
                }
            }
            countHistory = listHistory.size();
        }
        mBinding.tvCountHistory.setText(String.valueOf(countHistory));
    }

    private void displayListFoodFeatured() {
        FoodFeaturedAdapter foodFeaturedAdapter = new FoodFeaturedAdapter(loadListFoodFeatured(), new IOnClickFoodListener() {

            @Override
            public void onClickItemFood(Food food) {
                GlobalFunction.goToFoodDetail(MainActivity.this, food.getId());
            }

            @Override
            public void onClickFavoriteFood(Food food, boolean favorite) {}

            @Override
            public void onClickCategoryOfFood(Category category) {}
        });
        mBinding.viewPager.setAdapter(foodFeaturedAdapter);
        mBinding.indicator.setViewPager(mBinding.viewPager);

        mBinding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mHandlerBanner.removeCallbacks(mRunnableBanner);
                mHandlerBanner.postDelayed(mRunnableBanner, 3000);
            }
        });
    }

    private List<Food> loadListFoodFeatured() {
        resetListFoodFeatured();
        for (Food food : mListFood) {
            if (food.isFeatured() && mListFoodFeatured.size() < Constant.MAX_SIZE_LIST_FEATURED) {
                mListFoodFeatured.add(food);
            }
        }
        return mListFoodFeatured;
    }

    private void displayListPopularFood() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.rcvFoodPopular.setLayoutManager(linearLayoutManager);

        FoodAdapter foodAdapter = new FoodAdapter(loadListPopularFood(), new IOnClickFoodListener() {
            @Override
            public void onClickItemFood(Food food) {
                GlobalFunction.goToFoodDetail(MainActivity.this, food.getId());
            }

            @Override
            public void onClickFavoriteFood(Food food, boolean favorite) {
                GlobalFunction.onClickFavoriteFood(MainActivity.this, food, favorite);
            }

            @Override
            public void onClickCategoryOfFood(Category category) {
                GlobalFunction.goToFoodByCategory(MainActivity.this, category);
            }
        });
        mBinding.rcvFoodPopular.setAdapter(foodAdapter);
    }

    private List<Food> loadListPopularFood() {
        List<Food> list = new ArrayList<>();
        List<Food> allFoods = new ArrayList<>(mListFood);
        Collections.sort(allFoods, (food1, food2) -> food2.getCount() - food1.getCount());
        for (Food food : allFoods) {
            if (list.size() < Constant.MAX_SIZE_LIST_POPULAR) {
                list.add(food);
            }
        }
        return list;
    }

    private void showDialogRequestFood() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_request_food);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);

        // Get view
        final ImageView imgClose = dialog.findViewById(R.id.img_close);
        final EditText edtFoodName = dialog.findViewById(R.id.edt_food_name);
        final TextView tvSendRequest = dialog.findViewById(R.id.tv_send_request);

        imgClose.setOnClickListener(v -> dialog.dismiss());

        tvSendRequest.setOnClickListener(v -> {
            String strFoodName = edtFoodName.getText().toString().trim();
            if (StringUtil.isEmpty(strFoodName)) {
                GlobalFunction.showToastMessage(this,
                        getString(R.string.msg_name_food_request));
            } else {
                showProgressDialog(true);
                long requestId = System.currentTimeMillis();
                RequestFood requestFood = new RequestFood(requestId, strFoodName);
                MyApplication.get(this).requestFoodDatabaseReference()
                        .child(String.valueOf(System.currentTimeMillis()))
                        .setValue(requestFood, (databaseError, databaseReference) -> {
                            showProgressDialog(false);
                            GlobalFunction.hideSoftKeyboard(this);
                            GlobalFunction.showToastMessage(this,
                                    getString(R.string.msg_send_request_food_success));
                            dialog.dismiss();
                        });
            }
        });
        dialog.show();
    }

    private void onClickSignOut() {
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(this, LoginActivity.class);
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        showConfirmExitApp();
    }

    private void showConfirmExitApp() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive((dialog, which) -> finish())
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCategoryValueEventListener != null) {
            MyApplication.get(this).categoryDatabaseReference().removeEventListener(mCategoryValueEventListener);
        }
        if (mFoodValueEventListener != null) {
            MyApplication.get(this).foodDatabaseReference().removeEventListener(mFoodValueEventListener);
        }
    }
}
