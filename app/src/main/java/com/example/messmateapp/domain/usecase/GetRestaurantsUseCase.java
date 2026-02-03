package com.example.messmateapp.domain.usecase;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.domain.model.Restaurant;
import com.example.messmateapp.domain.repository.HomeRepository;
import com.example.messmateapp.utils.Resource;

import java.util.List;

public class GetRestaurantsUseCase {

    private final HomeRepository repo;

    public GetRestaurantsUseCase(HomeRepository repo) {
        this.repo = repo;
    }

    public LiveData<Resource<List<Restaurant>>> execute() {
        return repo.getRestaurants();
    }
}
