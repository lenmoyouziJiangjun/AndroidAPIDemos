package com.example.android.supportv7.widget.discreteScrollView.weather;

import com.example.android.supportv7.R;

import java.util.Arrays;
import java.util.List;

/**
 * Version 1.0
 * Created by lll on 17/9/8.
 * Description
 * copyright generalray4239@gmail.com
 */
public class WeatherStation {
    public static WeatherStation get() {
        return new WeatherStation();
    }

    private WeatherStation() {
    }

    public List<Forecast> getForecasts() {
        return Arrays.asList(
                new Forecast("Pisa", R.drawable.pisa, "16", Weather.PARTLY_CLOUDY),
                new Forecast("Paris", R.drawable.paris, "14", Weather.CLEAR),
                new Forecast("New York", R.drawable.new_york, "9", Weather.MOSTLY_CLOUDY),
                new Forecast("Rome", R.drawable.rome, "18", Weather.PARTLY_CLOUDY),
                new Forecast("London", R.drawable.london, "6", Weather.PERIODIC_CLOUDS),
                new Forecast("Washington", R.drawable.washington, "20", Weather.CLEAR));
    }
}
