package com.example.assignmentthree.api;

import com.example.assignmentthree.models.Weather;

public class WeatherAPI {
    private APIManager apiManager;

    public WeatherAPI(APIManager apiManager) {
        this.apiManager = apiManager;
    }

    // 获取天气数据的方法
    public void fetchWeatherData(double latitude, double longitude, WeatherCallback callback) {
        // 这里实现天气API调用
    }

    public interface WeatherCallback {
        void onSuccess(Weather weather);
        void onError(String error);
    }
}