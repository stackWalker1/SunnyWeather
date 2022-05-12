package com.example.sunnyweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.sunnyweather.WeatherActivity;
import com.google.gson.Gson;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    //更新天气
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = prefs.getString("weather_id", null);
        if (weatherId != null) {
            QWeather.getWeatherNow(this, weatherId, new QWeather.OnResultWeatherNowListener() {
                @Override
                public void onError(Throwable throwable) {
                     throwable.printStackTrace();
                }

                @Override
                public void onSuccess(WeatherNowBean weatherNowBean) {
                    String weatherNowResponse = new Gson().toJson(weatherNowBean);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                            (AutoUpdateService.this).edit();
                    editor.putString("weather_now_save", weatherNowResponse);
                    editor.apply();
                }
            });
            QWeather.getAirNow(this,weatherId, Lang.ZH_HANS, new QWeather.OnResultAirNowListener() {
                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(AirNowBean airNowBean) {
                    String airNowResponse = new Gson().toJson(airNowBean);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                            (AutoUpdateService.this).edit();
                    editor.putString("air_now_save", airNowResponse);
                    editor.apply();
                }
            });
            QWeather.getWeather3D(this, weatherId, new QWeather.OnResultWeatherDailyListener() {
                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onSuccess(WeatherDailyBean weatherDailyBean) {
                    String weatherDailyResponse = new Gson().toJson(weatherDailyBean);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                            (AutoUpdateService.this).edit();
                    editor.putString("daily_save", weatherDailyResponse);
                    editor.apply();
                }
            });
        }
    }
}