package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddressDto implements Serializable {

    /* ================= ID ================= */

    @SerializedName("_id")
    private String id;


    /* ================= LABEL ================= */

    @SerializedName("label")
    private String label;


    /* ================= ADDRESS INFO ================= */

    @SerializedName("house")
    private String house;

    @SerializedName("area")
    private String area;

    @SerializedName("landmark")
    private String landmark;

    @SerializedName("city")
    private String city;

    @SerializedName("state")
    private String state;

    @SerializedName("pincode")
    private String pincode;


    /* ================= LOCATION ================= */

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lng")
    private Double lng;


    /* ================= DEFAULT ================= */

    @SerializedName("isDefault")
    private boolean isDefault = false;


    /* ================= GETTERS ================= */

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getHouse() {
        return house;
    }

    public String getArea() {
        return area;
    }

    public String getLandmark() {
        return landmark;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public boolean isDefault() {
        return isDefault;
    }


    /* ================= SETTERS ================= */

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
