package com.example.messmateapp.domain.usecase;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.domain.model.Banner;
import com.example.messmateapp.domain.repository.HomeRepository;
import com.example.messmateapp.utils.Resource;

import java.util.List;

public class GetBannersUseCase {

    private final HomeRepository repo;

    public GetBannersUseCase(HomeRepository repo) {
        this.repo = repo;
    }

    // ✅ FIX: RETURN LiveData<Resource<List<Banner>>>
    public LiveData<Resource<List<Banner>>> execute() {
        return repo.getBanners();
    }
}
