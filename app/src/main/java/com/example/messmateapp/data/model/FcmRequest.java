package com.example.messmateapp.data.model;

public class FcmRequest {

    private String token;
    private String deviceType;

    public FcmRequest(String token, String deviceType) {
        this.token = token;
        this.deviceType = deviceType;
    }

    public String getToken() {
        return token;
    }

    public String getDeviceType() {
        return deviceType;
    }
}
