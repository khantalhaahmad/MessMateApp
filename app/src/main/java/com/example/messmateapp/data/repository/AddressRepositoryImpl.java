package com.example.messmateapp.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.data.model.AddressResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ✅ Address Repository (Zomato Style Flow)
 */
public class AddressRepositoryImpl implements AddressRepository {

    private final ApiService apiService;


    /* =================================================
       Constructor
       ================================================= */

    public AddressRepositoryImpl(Context context) {

        apiService =
                ApiClient
                        .getAuthClient(context)
                        .create(ApiService.class);
    }


    /* =================================================
       📍 GET ALL ADDRESSES
       ================================================= */

    @Override
    public LiveData<Resource<List<AddressDto>>> getAddresses() {

        MutableLiveData<Resource<List<AddressDto>>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());

        apiService.getAddresses()
                .enqueue(new Callback<AddressResponse>() {

                    @Override
                    public void onResponse(
                            Call<AddressResponse> call,
                            Response<AddressResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().addresses != null) {

                            liveData.setValue(
                                    Resource.success(response.body().addresses)
                            );

                        } else {

                            liveData.setValue(
                                    Resource.error("No address found")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AddressResponse> call,
                            Throwable t) {

                        liveData.setValue(
                                Resource.error("Network Error")
                        );
                    }
                });

        return liveData;
    }


    /* =================================================
       ⭐ GET ACTIVE ADDRESS (BILL PAGE)
       ================================================= */

    @Override
    public LiveData<Resource<AddressDto>> getActiveAddress() {

        MutableLiveData<Resource<AddressDto>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());

        apiService.getActiveAddress()
                .enqueue(new Callback<AddressResponse>() {

                    @Override
                    public void onResponse(
                            Call<AddressResponse> call,
                            Response<AddressResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().address != null) {

                            liveData.setValue(
                                    Resource.success(response.body().address)
                            );

                        } else {

                            liveData.setValue(
                                    Resource.error("No active address")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AddressResponse> call,
                            Throwable t) {

                        liveData.setValue(
                                Resource.error("Network Error")
                        );
                    }
                });

        return liveData;
    }


    /* =================================================
       ➕ ADD ADDRESS (AUTO SELECT)
       ================================================= */

    @Override
    public LiveData<Resource<List<AddressDto>>> addAddress(
            AddressDto address) {

        MutableLiveData<Resource<List<AddressDto>>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());

        apiService.addAddress(address)
                .enqueue(new Callback<AddressResponse>() {

                    @Override
                    public void onResponse(
                            Call<AddressResponse> call,
                            Response<AddressResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().addresses != null) {

                            liveData.setValue(
                                    Resource.success(response.body().addresses)
                            );

                        } else {

                            liveData.setValue(
                                    Resource.error("Add failed")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AddressResponse> call,
                            Throwable t) {

                        liveData.setValue(
                                Resource.error("Network Error")
                        );
                    }
                });

        return liveData;
    }


   /* =================================================
   ⭐ SELECT ADDRESS (ON CLICK)
   ================================================= */

    @Override
    public LiveData<Resource<AddressDto>> selectAddress(String id) {

        MutableLiveData<Resource<AddressDto>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());

        apiService.selectAddress(id)
                .enqueue(new Callback<AddressResponse>() {

                    @Override
                    public void onResponse(
                            Call<AddressResponse> call,
                            Response<AddressResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success
                                && response.body().selectedAddress != null) {

                            // ✅ Return selected address
                            liveData.setValue(
                                    Resource.success(
                                            response.body().selectedAddress
                                    )
                            );

                        } else {

                            liveData.setValue(
                                    Resource.error("Select failed")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AddressResponse> call,
                            Throwable t) {

                        liveData.setValue(
                                Resource.error("Network Error")
                        );
                    }
                });

        return liveData;
    }


    /* =================================================
   ❌ DELETE ADDRESS
   ================================================= */

    @Override
    public LiveData<Resource<List<AddressDto>>> deleteAddress(String id) {

        MutableLiveData<Resource<List<AddressDto>>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());

        apiService.deleteAddress(id)
                .enqueue(new Callback<AddressResponse>() {   // ✅ FIXED

                    @Override
                    public void onResponse(
                            Call<AddressResponse> call,
                            Response<AddressResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success) {

                            liveData.setValue(
                                    Resource.success(response.body().addresses)
                            );

                        } else {

                            liveData.setValue(
                                    Resource.error("Delete failed")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AddressResponse> call,
                            Throwable t) {

                        liveData.setValue(
                                Resource.error("Network Error")
                        );
                    }
                });

        return liveData;
    }

}
