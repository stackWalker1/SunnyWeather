package com.example.sunnyweather.db;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {
    private int provinceId;

    private String cityName;

    private int id;

    private int cityCode;

    public int getProvinceId() {
        return provinceId;
    }

    public String getCityName() {
        return cityName;
    }

    public int getId() {
        return id;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }
}
