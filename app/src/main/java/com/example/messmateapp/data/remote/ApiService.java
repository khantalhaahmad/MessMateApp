package com.example.messmateapp.data.remote;

import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.data.model.AddressResponse;
import com.example.messmateapp.data.model.AuthResponse;
import com.example.messmateapp.data.model.BannerDto;
import com.example.messmateapp.data.model.CreateOrderResponse;
import com.example.messmateapp.data.model.DeleteAddressResponse;
import com.example.messmateapp.data.model.MenuResponse;
import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.data.model.RecommendationResponse;
import com.example.messmateapp.data.model.RestaurantDto;
import com.example.messmateapp.data.model.VerifyResponse;
import com.example.messmateapp.data.model.UserResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;

public interface ApiService {

    /* ================= AUTH ================= */

    @POST("auth/firebase-login")
    Call<AuthResponse> firebaseLogin(
            @Header("Authorization") String firebaseToken
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


    /* ================= PAYMENT ================= */

    // ✅ Create Razorpay Order
    @POST("payment/create-order")
    Call<CreateOrderResponse> createOrder(
            @Body Map<String, Object> body
    );


    // ✅ Verify Razorpay Payment
    @POST("payment/verify")
    Call<VerifyResponse> verifyPayment(
            @Body Map<String, String> body
    );


    /* ================= ADDRESS (ZOMATO FLOW) ================= */

    // ✅ Get All Addresses
    @GET("address")
    Call<AddressResponse> getAddresses();


    // ✅ Get Active Address
    @GET("address/active")
    Call<AddressResponse> getActiveAddress();


    // ✅ Add New Address
    @POST("address")
    Call<AddressResponse> addAddress(
            @Body AddressDto body
    );


    // ✅ Select Address
    @PATCH("address/select/{id}")
    Call<AddressResponse> selectAddress(
            @Path("id") String id
    );


    // ✅ Delete Address (WITH TOKEN 🔥)
    @DELETE("address/{id}")
    Call<DeleteAddressResponse> deleteAddress(
            @Path("id") String id,
            @Header("Authorization") String token
    );


    /* ================= SINGLE MESS ================= */

    @GET("messes/{messId}")
    Call<RestaurantDto> getMessById(
            @Path("messId") String messId
    );


    /* ================= PROFILE ================= */

    @GET("users/me")
    Call<UserResponse> getProfile(
            @Header("Authorization") String token
    );

    @PUT("users/me")
    Call<UserResponse> updateProfile(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );
}
