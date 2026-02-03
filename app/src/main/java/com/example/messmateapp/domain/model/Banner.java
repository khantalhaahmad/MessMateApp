package com.example.messmateapp.domain.model;

import java.util.Objects;

public class Banner {

    private final String id;
    private final String imageUrl;
    private final String title;

    public Banner(String id, String imageUrl, String title) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Banner)) return false;
        Banner banner = (Banner) o;
        return Objects.equals(id, banner.id) &&
                Objects.equals(imageUrl, banner.imageUrl) &&
                Objects.equals(title, banner.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageUrl, title);
    }
}
