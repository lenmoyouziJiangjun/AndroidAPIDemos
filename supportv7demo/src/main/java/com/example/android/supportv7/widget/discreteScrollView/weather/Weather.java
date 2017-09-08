package com.example.android.supportv7.widget.discreteScrollView.weather;

/**
 * Version 1.0
 * Created by lll on 17/9/8.
 * Description
 * copyright generalray4239@gmail.com
 */
public enum Weather {
    PERIODIC_CLOUDS("Periodic Clouds"),
    CLOUDY("Cloudy"),
    MOSTLY_CLOUDY("Mostly Cloudy"),
    PARTLY_CLOUDY("Partly Cloudy"),
    CLEAR("Clear");

    private String displayName;

    Weather(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
