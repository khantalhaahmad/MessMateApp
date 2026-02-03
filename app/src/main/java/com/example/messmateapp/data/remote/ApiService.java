package com.example.messmateapp.data.remote;

import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.data.model.AddressResponse;
import com.example.messmateapp.data.model.AuthResponse;
import com.example.messmateapp.data.model.BannerDto;
import com.example.messmateapp.data.model.MenuResponse;
import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.data.model.RecommendationResponse;
import com.example.messmateapp.data.model.RestaurantDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    /* ================= AUTH ================= */

    @POST("auth/firebase-login")
    Call<AuthResponse> firebaseLogin(
            @retrofit2.http.Header("Authorization") String firebaseToken
    );


    /* ================= MESSES ================= */

    @GET("messes")
    Call<List<RestaurantDto>> getAllMesses();


    /* ================= BANNERS ================= */

    @GET("recommendations/guest")
    Call<BannerDto.Response> getGuestRecommendations();


    /* ================= RECOMMENDATION ================= */

    @GET("recommendations/{userId}")
    Call<RecommendationResponse> getUserRecommendations(
            @Path("userId") String userId
    );


    @GET("recommendations/mess/{messId}")
    Call<RecommendationResponse> getMessRecommendations(
            @Path("messId") String messId
    );


    /* ================= MENU ================= */

    @GET("menu/{messId}")
    Call<MenuResponse> getMenu(
            @Path("messId") String messId
    );


    /* ================= ORDER ================= */

    @POST("orders")
    Call<Object> placeOrder(
            @Body OrderRequestDto body
    );


    /* ================= ADDRESS (ZOMATO FLOW) ================= */

    // ✅ Get All Addresses
    @GET("address")
    Call<AddressResponse> getAddresses();


    // ✅ Get Active Address (Bill Page)
    @GET("address/active")
    Call<AddressResponse> getActiveAddress();


    // ✅ Add New Address (Auto Select)
    @POST("address")
    Call<AddressResponse> addAddress(
            @Body AddressDto body
    );


    // ✅ Select Address (On Click)
    @PATCH("address/select/{id}")
    Call<AddressResponse> selectAddress(
            @Path("id") String id
    );

    /* ================= SINGLE MESS ================= */

    @GET("messes/{messId}")
    Call<RestaurantDto> getMessById(
            @Path("messId") String messId
    );

    // ✅ Delete Address
    @DELETE("address/{id}")
    Call<AddressResponse> deleteAddress(
            @Path("id") String id
    );
}
