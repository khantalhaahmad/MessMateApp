package com.example.messmateapp.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.domain.model.Banner;
import com.example.messmateapp.domain.model.Category;
import com.example.messmateapp.domain.model.Restaurant;
import com.example.messmateapp.utils.Resource;

import java.util.List;

public interface HomeRepository {

    LiveData<Resource<List<Banner>>> getBanners();

    LiveData<Resource<List<Category>>> getCategories();

    LiveData<Resource<List<Restaurant>>> getRestaurants();
}
