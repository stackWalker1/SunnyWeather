package com.example.sunnyweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.sunnyweather.gson.Weather;
import com.example.sunnyweather.service.AutoUpdateService;
import com.example.sunnyweather.util.HttpUtil;
import com.example.sunnyweather.util.Utility;
import com.google.gson.Gson;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;


import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity {



    private static final String DAILY_SAVE = "daily_save";
    private static final String AIR_SAVE = "air_now_save";
    private static final String NOW_SAVE = "weather_now_save";

    private String weatherId;
    private String countyName;
    private String bingPic;

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;

    private TextView titleCity;
    private Button navButton;
    private ScrollView weatherLayout;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private ImageView bingPicImg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById (R.id.title_city);
        titleUpdateTime = (TextView) findViewById (R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id .weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text );
        swipeRefresh = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        bingPicImg = findViewById(R.id.bing_pic_img);

        swipeRefresh.setColorSchemeResources(R.color.purple_500);
        navButton.setOnClickListener(view->{
            drawerLayout.openDrawer(GravityCompat.START);
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        countyName = prefs.getString("county_name", null);
        weatherId = prefs.getString("weather_id", null);
        bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        if (weatherId == null && countyName == null) {
            countyName = getIntent().getStringExtra("county_name");
            weatherId = getIntent().getStringExtra("weather_id");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                    (WeatherActivity.this).edit();
            editor.putString("county_name", countyName);
            editor.putString("weather_id", weatherId);
            editor.apply();
        }
        titleCity.setText(countyName);

        String airString = prefs.getString(AIR_SAVE, null);
        String dailyString = prefs.getString(DAILY_SAVE, null);
        String weatherNowString = prefs.getString(NOW_SAVE,null);

        if (airString != null) {
            AirNowBean airNowBean = Utility.handleAirResponse(airString);
            try {
                showAirInfo(airNowBean.getNow());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            requestAirNow(weatherId);
        }

        if (dailyString != null) {
            WeatherDailyBean weatherDailyBean = Utility.handleForecastResponse(dailyString);
            try {
                showForecastInfo(weatherDailyBean.getDaily());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            requestWeather3D(weatherId);
        }

        if (weatherNowString != null) {
            WeatherNowBean weatherNowBean = Utility.handleRealTimeWeatherResponse(weatherNowString);
            try {
                showWeatherNowInfo(weatherNowBean.getNow());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            requestWeatherNow(weatherId);
        }

        swipeRefresh.setOnRefreshListener(() -> {
            requestWeather(weatherId);
            swipeRefresh.setRefreshing(false);
        });

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    //获取实时空气数据
    public void requestAirNow(String weatherId) {
        QWeather.getAirNow(this, weatherId, Lang.ZH_HANS, new QWeather.OnResultAirNowListener() {
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取空气质量信息失败(onFailure)", Toast.LENGTH_SHORT)
                            .show();
                });
            }
            @Override
            public void onSuccess(AirNowBean airNowBean) {
                String airNowResponse = new Gson().toJson(airNowBean);
                Log.i("get air now onSuccess", airNowResponse);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString(AIR_SAVE, airNowResponse);
                editor.apply();
                if (airNowBean.getCode() == Code.OK) {
                    runOnUiThread(() -> {
                        AirNowBean.NowBean airNow = airNowBean.getNow();
                        showAirInfo(airNow);
                    });
                } else {
                    Code code = airNowBean.getCode();
                    runOnUiThread(() -> {
                        Toast.makeText(WeatherActivity.this, "未来天气信息信息失败onSuccess" + code, Toast.LENGTH_SHORT)
                                .show();
                    });
                }
            }
        });
    }

    //获取未来三天天气数据
    public void requestWeather3D(String weatherId) {
        QWeather.getWeather3D(this, weatherId, new QWeather.OnResultWeatherDailyListener() {
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(()->{
                    Toast.makeText(WeatherActivity.this, "未来天气信息信息失败(onFailure)", Toast.LENGTH_SHORT)
                            .show();
                });
            }
            @Override
            public void onSuccess(WeatherDailyBean weatherDailyBean) {
                String dailyResponse = new Gson().toJson(weatherDailyBean);
                Log.i("get daily onSuccess", dailyResponse);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString(DAILY_SAVE, dailyResponse);
                editor.apply();
                if (Code.OK == weatherDailyBean.getCode()) {
                    runOnUiThread(()->{
                        List<WeatherDailyBean.DailyBean> dailyBeanList = weatherDailyBean.getDaily();
                        showForecastInfo(dailyBeanList);
                    });
                } else {
                    Code code = weatherDailyBean.getCode();
                    runOnUiThread(()->{
                        Toast.makeText(WeatherActivity.this, "未来天气信息信息失败onSuccess" + code, Toast.LENGTH_SHORT)
                                .show();
                    });
                }
            }
        });
    }

    //获取当前天气数据
    public void requestWeatherNow(String weatherId) {
        QWeather.getWeatherNow(this, weatherId, new QWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(()->{
                    Toast.makeText(WeatherActivity.this, "实时天气获取失败(onFailure)", Toast.LENGTH_SHORT)
                            .show();
                });
            }

            @Override
            public void onSuccess(WeatherNowBean weatherNowBean) {
                String weatherNowResponse = new Gson().toJson(weatherNowBean);
                Log.i("get now onSuccess", weatherNowResponse);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString(NOW_SAVE, weatherNowResponse);
                editor.apply();
                if (Code.OK == weatherNowBean.getCode()) {
                    runOnUiThread(() -> {
                        WeatherNowBean.NowBaseBean nowBaseBean = weatherNowBean.getNow();
                        showWeatherNowInfo(nowBaseBean);
                    });
                } else {
                    Code code = weatherNowBean.getCode();
                    runOnUiThread(()->{
                        Toast.makeText(WeatherActivity.this, "获取实时天气信息失败onSuccess"
                                + code, Toast.LENGTH_SHORT).show();
                    });
                }

            }
        });
    }

    //改变区域
    public void onChangeArea(String weatherId, String countyName) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                (WeatherActivity.this).edit();
        editor.putString("county_name", countyName);
        editor.putString("weather_id", weatherId);
        editor.apply();
        this.weatherId = weatherId;
        this.countyName = countyName;
        requestWeather(weatherId);
        titleCity.setText(countyName);
        this.drawerLayout.closeDrawers();
        this.swipeRefresh.setRefreshing(false);
    }

    //请求汇总
    public void requestWeather(String weatherId) {
        requestAirNow(weatherId);
        requestWeatherNow(weatherId);
        requestWeather3D(weatherId);

    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        (WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(()->{
                    Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                });
            }
        });
    }

    private void showAirInfo(AirNowBean.NowBean airNow) {
        pm25Text.setText(airNow.getPm2p5());
        aqiText.setText(airNow.getAqi());
    }

    private void showWeatherNowInfo(WeatherNowBean.NowBaseBean nowBaseBean) {
        titleUpdateTime.setText(nowBaseBean.getObsTime());

        String celsius = nowBaseBean.getTemp() + "℃";
        degreeText.setText(celsius);
        weatherInfoText.setText(nowBaseBean.getText());
    }

    private void showForecastInfo(List<WeatherDailyBean.DailyBean> forecast) {
        forecastLayout.removeAllViews();
        for (WeatherDailyBean.DailyBean oneDay : forecast) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text) ;
            TextView maxText = (TextView) view.findViewById(R.id.max_text ) ;
            TextView minText = (TextView) view.findViewById(R.id.min_text) ;
            dateText.setText(oneDay.getFxDate());
            infoText.setText(oneDay.getTextDay());
            maxText.setText(oneDay.getTempMax());
            minText.setText(oneDay.getTempMin());
            forecastLayout.addView(view);
        }
    }
}