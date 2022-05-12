package com.example.sunnyweather.gson;

import com.google.gson.annotations.SerializedName;

public class RealTimeWeather {

    @SerializedName("code")
    public String status;

    public Now now;

    public class Now {
        @SerializedName("obsTime")
        public String obsTime;

        @SerializedName("temp")
        public String temperature;

        @SerializedName("text")
        public String skycon;
    }
}
