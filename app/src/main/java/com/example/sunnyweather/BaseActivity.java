package com.example.sunnyweather;

import android.os.Bundle;

import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HeConfig.init("HE2205092234001764", "e39161c0ebee4cdcbb966376699e1048");
        HeConfig.switchToDevService();
    }
}
