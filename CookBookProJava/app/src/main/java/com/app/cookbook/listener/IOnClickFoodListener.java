package com.app.cookbook.listener;

import com.app.cookbook.model.Category;
import com.app.cookbook.model.Food;

public interface IOnClickFoodListener {
    void onClickItemFood(Food food);
    void onClickFavoriteFood(Food food, boolean favorite);
    void onClickCategoryOfFood(Category category);
}
