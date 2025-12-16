package com.example.assignmentthree;

import android.app.Application;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 1. 地图SDK隐私同意（必须先调用）
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        // 2. 初始化地图SDK
        SDKInitializer.initialize(getApplicationContext());
        // 3. 定位模块隐私同意（用到LocationClient时加）
        LocationClient.setAgreePrivacy(true);
    }
}