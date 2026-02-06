package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DeleteAddressResponse implements Serializable {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;


    /* ================= GETTERS ================= */

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
