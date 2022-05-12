package com.example.sunnyweather.gson;

import com.google.gson.annotations.SerializedName;

public class Air {

    @SerializedName("code")
    public String status;

    @SerializedName("now")
    public Now now;

    public class Now {
        @SerializedName("aqi")
        public String aqi;

        @SerializedName("pm2p5")
        public String pm25;
    }
}
