package com.example.messmateapp.utils;

public class Resource<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    // ✅ Keep existing fields (so old code won't break)
    public final Status status;
    public final T data;
    public final String message;

    private Resource(Status s, T d, String m) {
        status = s;
        data = d;
        message = m;
    }

    /* ================= FACTORY ================= */

    public static <T> Resource<T> success(T d) {
        return new Resource<>(Status.SUCCESS, d, null);
    }

    public static <T> Resource<T> error(String m) {
        return new Resource<>(Status.ERROR, null, m);
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    /* ================= NEW HELPERS (NO BREAKING) ================= */

    // For: res.isSuccess()
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    // For: res.isLoading()
    public boolean isLoading() {
        return status == Status.LOADING;
    }

    // For: res.isError()
    public boolean isError() {
        return status == Status.ERROR;
    }

    // For: res.getData()
    public T getData() {
        return data;
    }

    // For: res.getMessage()
    public String getMessage() {
        return message;
    }

    // Optional (future use)
    public Status getStatus() {
        return status;
    }
}
