package com.example.messmateapp.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmateapp.domain.usecase.GetBannersUseCase;
import com.example.messmateapp.domain.usecase.GetCategoriesUseCase;
import com.example.messmateapp.domain.usecase.GetRestaurantsUseCase;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final GetBannersUseCase bannersUseCase;
    private final GetCategoriesUseCase categoriesUseCase;
    private final GetRestaurantsUseCase restaurantsUseCase;

    public HomeViewModelFactory(
            GetBannersUseCase bannersUseCase,
            GetCategoriesUseCase categoriesUseCase,
            GetRestaurantsUseCase restaurantsUseCase
    ) {
        this.bannersUseCase = bannersUseCase;
        this.categoriesUseCase = categoriesUseCase;
        this.restaurantsUseCase = restaurantsUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(
                    bannersUseCase,
                    categoriesUseCase,
                    restaurantsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
