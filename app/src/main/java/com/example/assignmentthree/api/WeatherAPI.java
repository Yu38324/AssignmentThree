package com.example.assignmentthree.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.assignmentthree.models.Weather;
import org.json.JSONObject;

public class WeatherAPI {
    private static final String API_KEY = "你的WeatherAPI.com密钥";
    private static final String BASE_URL = "http://api.weatherapi.com/v1/current.json";
    private Context context;

    public WeatherAPI(Context context) {
        this.context = context;
    }

    /**
     * 根据经纬度获取天气
     */
    public void getWeatherByLatLng(double lat, double lon, WeatherCallback callback) {
        String url = String.format(
                "%s?key=%s&q=%f,%f&aqi=no",
                BASE_URL, API_KEY, lat, lon
        );

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Weather weather = new Weather();
                            // 解析天气数据
                            JSONObject current = response.getJSONObject("current");
                            weather.setTempC(current.getInt("temp_c"));
                            weather.setCondition(current.getJSONObject("condition").getString("text"));
                            weather.setWindDir(current.getString("wind_dir"));
                            weather.setWindKph(current.getInt("wind_kph"));
                            weather.setHumidity(current.getInt("humidity"));

                            callback.onSuccess(weather);
                        } catch (Exception e) {
                            callback.onFailure("解析天气数据失败");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onFailure("获取天气失败：" + error.getMessage());
                    }
                }
        );

        // 添加到请求队列
        APIManager.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * 天气回调接口
     */
    public interface WeatherCallback {
        void onSuccess(Weather weather);
        void onFailure(String errorMsg);
    }
}