package com.example.messmateapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.messmateapp.data.model.BannerDto;
import com.example.messmateapp.data.model.RestaurantDto;
import com.example.messmateapp.data.model.RestaurantMapper;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.domain.model.Banner;
import com.example.messmateapp.domain.model.Category;
import com.example.messmateapp.domain.model.Restaurant;
import com.example.messmateapp.domain.repository.HomeRepository;
import com.example.messmateapp.utils.Resource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepositoryImpl implements HomeRepository {

    private final ApiService api =
            ApiClient.getClient().create(ApiService.class);

    // ================= BANNERS =================
    @Override
    public LiveData<Resource<List<Banner>>> getBanners() {

        MutableLiveData<Resource<List<Banner>>> liveData =
                new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        api.getGuestRecommendations().enqueue(new Callback<BannerDto.Response>() {
            @Override
            public void onResponse(
                    Call<BannerDto.Response> call,
                    Response<BannerDto.Response> response
            ) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null) {

                    List<Banner> banners = new ArrayList<>();

                    for (BannerDto.Item item : response.body().data) {
                        banners.add(
                                new Banner(
                                        item.name,
                                        item.image,
                                        item.name
                                )
                        );
                    }

                    liveData.setValue(Resource.success(banners));
                } else {
                    liveData.setValue(Resource.error("Failed to load banners"));
                }
            }

            @Override
            public void onFailure(Call<BannerDto.Response> call, Throwable t) {
                liveData.setValue(Resource.error(t.getMessage()));
            }
        });

        return liveData;
    }

    // ================= CATEGORIES =================
    @Override
    public LiveData<Resource<List<Category>>> getCategories() {

        MutableLiveData<Resource<List<Category>>> liveData =
                new MutableLiveData<>();

        List<Category> list = new ArrayList<>();
        list.add(new Category("1", "All"));
        list.add(new Category("2", "Veg"));
        list.add(new Category("3", "Non-Veg"));
        list.add(new Category("4", "Biryani"));
        list.add(new Category("5", "Chicken"));
        list.add(new Category("6", "Fish"));

        liveData.setValue(Resource.success(list));
        return liveData;
    }

    // ================= RESTAURANTS =================
    @Override
    public LiveData<Resource<List<Restaurant>>> getRestaurants() {

        MutableLiveData<Resource<List<Restaurant>>> liveData =
                new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        api.getAllMesses().enqueue(new Callback<List<RestaurantDto>>() {
            @Override
            public void onResponse(
                    Call<List<RestaurantDto>> call,
                    Response<List<RestaurantDto>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Restaurant> restaurants = new ArrayList<>();

                    for (RestaurantDto dto : response.body()) {
                        // ✅ USE MAPPER (single source of truth)
                        restaurants.add(RestaurantMapper.map(dto));
                    }

                    liveData.setValue(Resource.success(restaurants));
                } else {
                    liveData.setValue(
                            Resource.error("Server Error: " + response.code())
                    );
                }
            }

            @Override
            public void onFailure(Call<List<RestaurantDto>> call, Throwable t) {
                liveData.setValue(
                        Resource.error("Network Error: " + t.getMessage())
                );
            }
        });

        return liveData;
    }
}
