package com.example.messmateapp.domain.usecase;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.domain.model.Category;
import com.example.messmateapp.domain.repository.HomeRepository;
import com.example.messmateapp.utils.Resource;

import java.util.List;

public class GetCategoriesUseCase {

    private final HomeRepository repo;

    public GetCategoriesUseCase(HomeRepository repo) {
        this.repo = repo;
    }

    public LiveData<Resource<List<Category>>> execute() {
        return repo.getCategories();
    }
}
