package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddressResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    // ✅ For: GET /address
    @SerializedName("addresses")
    public List<AddressDto> addresses;

    // ✅ For: GET /address/active OR /default
    @SerializedName("address")
    public AddressDto address;

    // ✅ For: PATCH /address/select/{id}
    @SerializedName("selectedAddress")
    public AddressDto selectedAddress;
}
