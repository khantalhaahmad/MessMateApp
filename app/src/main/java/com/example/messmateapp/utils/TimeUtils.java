package com.example.messmateapp.utils;

import java.time.Instant;

public class TimeUtils {

    public static String getTimeAgo(String date) {

        try {

            long time = Instant.parse(date).toEpochMilli();
            long now = System.currentTimeMillis();

            long diff = now - time;

            long mins = diff / (60 * 1000);
            long hrs  = diff / (60 * 60 * 1000);

            if (mins < 60) return mins + " min ago";
            if (hrs < 24) return hrs + " hrs ago";

            return (hrs / 24) + " days ago";

        } catch (Exception e) {

            return "";
        }
    }
}
