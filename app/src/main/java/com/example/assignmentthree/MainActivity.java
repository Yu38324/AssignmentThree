package com.example.assignmentthree;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BaiduLocation";
    private static final int REQUEST_LOC_PERMISSION = 1001; // 权限请求码

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 百度SDK全局初始化（必须放在setContentView之前）
        //SDKInitializer.initialize(getApplicationContext());
        //SDKInitializer.setCoordType(CoordType.BD09LL); // 设置坐标类型为百度经纬度
        //LocationClient.setAgreePrivacy(true); // 同意隐私政策

        setContentView(R.layout.activity_main);

        // 2. 初始化地图控件
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true); // 开启定位图层

        // 3. 检查并申请定位权限
        try {
            checkAndRequestLocationPermission();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查并动态申请定位权限
     */
    private void checkAndRequestLocationPermission() throws Exception {
        // 检查精细定位权限是否授予
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 未授予，申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOC_PERMISSION);
        } else {
            // 已授予，初始化并启动定位
            initLocation();
        }
    }

    /**
     * 初始化定位客户端
     */
    private void initLocation() throws Exception {
        // 创建定位客户端
        mLocationClient = new LocationClient(getApplicationContext());

        // 设置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 强制打开GPS
        option.setCoorType("bd09ll"); // 百度经纬度坐标
        option.setScanSpan(1000); // 1秒定位一次（测试用，实际建议3000+）
        option.setIsNeedAddress(true); // 是否需要地址信息（可选）
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 高精度模式（GPS+网络）
        mLocationClient.setLocOption(option);

        // 注册定位监听器（核心：处理定位结果）
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null || mMapView == null) {
                    Log.e(TAG, "定位结果为空");
                    return;
                }

                // 打印定位结果日志（排查问题关键）
                Log.d(TAG, "定位结果：");
                Log.d(TAG, "纬度：" + location.getLatitude());
                Log.d(TAG, "经度：" + location.getLongitude());
                Log.d(TAG, "定位类型：" + getLocationTypeDesc(location.getLocType()));
                Log.d(TAG, "地址：" + location.getAddrStr());

                // 定位失败判断（关键）
                if (location.getLocType() == BDLocation.TypeNone) {
                    Toast.makeText(MainActivity.this, "定位失败：未获取到位置信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 构造定位数据并设置到地图
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius()) // 定位精度
                        .direction(location.getDirection()) // 方向（0-360）
                        .latitude(location.getLatitude()) // 纬度
                        .longitude(location.getLongitude()) // 经度
                        .build();
                mBaiduMap.setMyLocationData(locData);

                // 可选：将地图视角移动到当前定位位置
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latLng, 18); // 18为缩放级别
                mBaiduMap.animateMapStatus(update);
            }
        });

        // 启动定位
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
            Log.d(TAG, "定位客户端已启动");
        }
    }

    /**
     * 解析定位类型描述（方便排查问题）
     */
    private String getLocationTypeDesc(int locType) {
        switch (locType) {
            case BDLocation.TypeGpsLocation:
                return "GPS定位（成功）";
            case BDLocation.TypeNetWorkLocation:
                return "网络定位（成功）";
            case BDLocation.TypeOffLineLocation:
                return "离线定位（成功）";
            case BDLocation.TypeNone:
                return "定位失败（无位置）";
            case BDLocation.TypeServerError:
                return "定位失败（服务器错误）";
            case BDLocation.TypeNetWorkException:
                return "定位失败（网络异常）";
            case BDLocation.TypeCriteriaException:
                return "定位失败（GPS/网络未开启）";
            default:
                return "未知类型：" + locType;
        }
    }

    /**
     * 处理权限申请结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限申请成功，初始化定位
                try {
                    initLocation();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(this, "定位权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                // 权限被拒绝
                Toast.makeText(this, "定位权限被拒绝，无法使用定位功能", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "用户拒绝定位权限");
            }
        }
    }

    // 地图生命周期管理（必须保留）
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
        // 停止定位并释放资源
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }
}