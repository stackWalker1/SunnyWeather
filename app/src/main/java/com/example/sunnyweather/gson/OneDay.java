package com.example.sunnyweather.gson;

import com.google.gson.annotations.SerializedName;

public class OneDay {

    @SerializedName("fxDate")
    public String date;

    @SerializedName("tempMax")
    public String tempMax;

    @SerializedName("tempMin")
    public String tempMin;

    @SerializedName("textDay")
    public String skycon;
}
