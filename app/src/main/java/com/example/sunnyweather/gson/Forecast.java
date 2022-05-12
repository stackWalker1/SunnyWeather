package com.example.sunnyweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecast {

    @SerializedName("code")
    public String status;

    @SerializedName("daily")
    public List<OneDay> forecastList;
}
