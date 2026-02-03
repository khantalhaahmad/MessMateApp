package com.example.messmateapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.messmateapp.domain.model.Banner;
import com.example.messmateapp.domain.model.Category;
import com.example.messmateapp.domain.model.Restaurant;
import com.example.messmateapp.domain.usecase.GetBannersUseCase;
import com.example.messmateapp.domain.usecase.GetCategoriesUseCase;
import com.example.messmateapp.domain.usecase.GetRestaurantsUseCase;
import com.example.messmateapp.utils.Resource;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final GetBannersUseCase bannersUseCase;
    private final GetCategoriesUseCase categoriesUseCase;
    private final GetRestaurantsUseCase restaurantsUseCase;

    public HomeViewModel(
            GetBannersUseCase bannersUseCase,
            GetCategoriesUseCase categoriesUseCase,
            GetRestaurantsUseCase restaurantsUseCase
    ) {
        this.bannersUseCase = bannersUseCase;
        this.categoriesUseCase = categoriesUseCase;
        this.restaurantsUseCase = restaurantsUseCase;
    }

    public LiveData<Resource<List<Banner>>> banners() {
        return bannersUseCase.execute();
    }

    public LiveData<Resource<List<Category>>> categories() {
        return categoriesUseCase.execute();
    }

    public LiveData<Resource<List<Restaurant>>> restaurants() {
        return restaurantsUseCase.execute();
    }
}
