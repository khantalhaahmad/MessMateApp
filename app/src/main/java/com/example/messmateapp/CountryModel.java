package com.example.messmateapp;

public class CountryModel {

    private String code;
    private int flag;

    public CountryModel(String code, int flag) {
        this.code = code;
        this.flag = flag;
    }

    public String getCode() {
        return code;
    }

    public int getFlag() {
        return flag;
    }
}
