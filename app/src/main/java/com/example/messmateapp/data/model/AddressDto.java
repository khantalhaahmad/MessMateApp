package com.example.messmateapp.data.model;

import java.io.Serializable;

public class AddressDto implements Serializable {

    public String _id;

    /* Label */
    public String label;

    /* Address Info */
    public String house;
    public String area;
    public String landmark;

    public String city;
    public String state;     // ✅ REQUIRED (Backend needs this)
    public String pincode;

    /* Location */
    public Double lat;
    public Double lng;

    /* Default Address */
    public boolean isDefault = false;
}
