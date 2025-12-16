package com.example.assignmentthree;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 只显示地图，不开启定位
        showMapOnly();
    }

    private void showMapOnly() {
        // 设置地图类型为普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        // 设置地图中心点（例如：北京市中心）
        LatLng center = new LatLng(39.915, 116.404);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(center);
        mBaiduMap.animateMapStatus(update);

        // 设置缩放级别
        MapStatusUpdate zoom = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.animateMapStatus(zoom);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}