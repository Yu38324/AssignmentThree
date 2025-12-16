package com.example.assignmentthree;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BaiduMapSearch";
    private static final int REQUEST_LOC_PERMISSION = 1001;

    // UI组件
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private SearchView searchView;
    private RecyclerView recyclerSearchResults;
    private CardView cardSearchResults;
    private ProgressBar progressBar;
    private ImageButton btnMyLocation;

    // 定位相关
    private LocationClient mLocationClient;
    private LatLng currentLocation;

    // POI搜索相关
    private PoiSearch mPoiSearch;
    private List<PoiInfo> searchResults = new ArrayList<>();
    private SearchResultAdapter searchResultAdapter;

    // 标记相关
    private Marker destinationMarker;  // 红色标记 - 目的地
    private List<Marker> parkMarkers = new ArrayList<>();  // 绿色标记 - 公园
    private BitmapDescriptor redMarkerIcon;
    private BitmapDescriptor greenMarkerIcon;

    // 当前城市
    private String currentCity = "北京市";  // 默认值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI组件
        initUI();

        // 初始化地图
        initMap();

        // 初始化POI搜索
        initPoiSearch();

        // 初始化标记图标
        initMarkerIcons();

        // 检查并申请权限
        checkAndRequestLocationPermission();
    }

    private void initUI() {
        mMapView = findViewById(R.id.bmapView);
        searchView = findViewById(R.id.searchView);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        cardSearchResults = findViewById(R.id.cardSearchResults);
        progressBar = findViewById(R.id.progressBar);
        btnMyLocation = findViewById(R.id.btnMyLocation);

        // 设置搜索框监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchDestination(query);
                    searchView.clearFocus();  // 隐藏键盘
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 可以在这里实现实时搜索建议
                if (!TextUtils.isEmpty(newText) && newText.length() >= 2) {
                    // 可以在这里添加实时搜索功能
                }
                return false;
            }
        });

        // 设置返回我的位置按钮
        btnMyLocation.setOnClickListener(v -> {
            if (currentLocation != null) {
                moveToLocation(currentLocation, 18);
            } else {
                Toast.makeText(this, "无法获取当前位置", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化搜索结果列表
        searchResultAdapter = new SearchResultAdapter();
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchResultAdapter);
    }

    private void initMap() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        // 设置地图点击监听器
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 点击地图空白区域隐藏搜索结果
                if (cardSearchResults != null) {
                    cardSearchResults.setVisibility(View.GONE);
                }
                if (searchView != null) {
                    searchView.clearFocus();
                }
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                // 不处理POI点击
            }
        });

        // 设置标记点击监听器
        mBaiduMap.setOnMarkerClickListener(marker -> {
            if (marker != null && marker.getTitle() != null) {
                String title = marker.getTitle();
                if (title.contains("公园")) {
                    Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "目的地：" + title, Toast.LENGTH_SHORT).show();
                }
            }
            // 显示信息窗口
            marker.showInfoWindow();
            return true;
        });
    }

    private void initPoiSearch() {
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                progressBar.setVisibility(View.GONE);

                if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Toast.makeText(MainActivity.this, "未找到相关地点", Toast.LENGTH_SHORT).show();
                    cardSearchResults.setVisibility(View.GONE);
                    return;
                }

                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    searchResults.clear();
                    searchResults.addAll(poiResult.getAllPoi());

                    // 显示搜索结果
                    searchResultAdapter.notifyDataSetChanged();
                    cardSearchResults.setVisibility(View.VISIBLE);

                    // 如果只有一个结果，自动选择（可选）
                    if (searchResults.size() == 1) {
                        // 可以在这里自动选择第一个结果
                        // selectDestination(searchResults.get(0));
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                // 不处理详细结果
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
                // 不处理详细结果
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                // 不处理室内结果
            }
        });
    }

    private void initMarkerIcons() {
        // 红色标记 - 目的地
        redMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_searched);
        // 绿色标记 - 公园
        greenMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_park);
    }

    /**
     * 搜索目的地
     */
    private void searchDestination(String keyword) {
        progressBar.setVisibility(View.VISIBLE);

        // 清除之前的搜索结果
        searchResults.clear();
        searchResultAdapter.notifyDataSetChanged();

        // 在城市范围内搜索
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption()
                .city(currentCity)
                .keyword(keyword)
                .pageNum(0)
                .pageCapacity(20);  // 显示20个结果供用户选择

        mPoiSearch.searchInCity(citySearchOption);
    }

    /**
     * 选择目的地并显示附近公园
     */
    private void selectDestination(PoiInfo poiInfo) {
        if (poiInfo == null || poiInfo.location == null) {
            Toast.makeText(this, "无效的地点信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 隐藏搜索结果列表
        cardSearchResults.setVisibility(View.GONE);

        // 清除之前的标记
        clearMarkers();

        // 添加目的地标记（红色）
        LatLng destination = poiInfo.location;
        addDestinationMarker(destination, poiInfo.name);

        // 更新搜索框显示
        searchView.setQuery(poiInfo.name, false);

        // 搜索附近的公园
        searchNearbyParks(destination);
    }

    /**
     * 搜索附近的公园
     */
    private void searchNearbyParks(LatLng center) {
        progressBar.setVisibility(View.VISIBLE);

        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption()
                .location(center)
                .keyword("公园")
                .radius(5000)  // 5公里半径
                .pageNum(0)
                .pageCapacity(10);  // 获取10个结果

        // 创建一个新的POI搜索实例专门搜索公园
        PoiSearch parkPoiSearch = PoiSearch.newInstance();
        parkPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                progressBar.setVisibility(View.GONE);

                if (poiResult != null && poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    // 获取公园结果
                    List<PoiInfo> parks = poiResult.getAllPoi();
                    int count = Math.min(parks.size(), 10);

                    // 保存所有标记的位置
                    List<LatLng> allLatLngs = new ArrayList<>();
                    allLatLngs.add(center); // 添加目的地位置

                    for (int i = 0; i < count; i++) {
                        PoiInfo park = parks.get(i);
                        addParkMarker(park.location, park.name, i + 1);
                        allLatLngs.add(park.location); // 添加公园位置
                    }

                    // 调整地图视图以显示所有标记
                    adjustMapView(allLatLngs);

                    if (count > 0) {
                        Toast.makeText(MainActivity.this,
                                "找到" + count + "个附近公园", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 如果没有找到公园，只显示目的地
                    List<LatLng> onlyDestination = new ArrayList<>();
                    onlyDestination.add(center);
                    adjustMapView(onlyDestination);
                    Toast.makeText(MainActivity.this, "未找到附近公园", Toast.LENGTH_SHORT).show();
                }

                parkPoiSearch.destroy();  // 释放资源
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {}

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {}

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {}
        });

        parkPoiSearch.searchNearby(nearbySearchOption);
    }

    /**
     * 调整地图视图以显示所有标记
     */
    private void adjustMapView(List<LatLng> latLngs) {
        if (latLngs == null || latLngs.isEmpty()) {
            return;
        }

        if (latLngs.size() == 1) {
            // 如果只有一个点，放大到合适级别
            moveToLocation(latLngs.get(0), 16);
            return;
        }

        // 计算所有点的边界
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;

        for (LatLng latLng : latLngs) {
            if (latLng == null) continue;

            minLat = Math.min(minLat, latLng.latitude);
            maxLat = Math.max(maxLat, latLng.latitude);
            minLng = Math.min(minLng, latLng.longitude);
            maxLng = Math.max(maxLng, latLng.longitude);
        }

        // 计算中心点
        double centerLat = (minLat + maxLat) / 2;
        double centerLng = (minLng + maxLng) / 2;
        LatLng center = new LatLng(centerLat, centerLng);

        // 计算距离并确定合适的缩放级别
        double latDiff = maxLat - minLat;
        double lngDiff = maxLng - minLng;

        // 根据距离计算缩放级别
        float zoomLevel = calculateZoomLevel(latDiff, lngDiff);

        // 移动地图到中心点
        moveToLocation(center, zoomLevel);
    }

    /**
     * 根据经纬度差计算合适的缩放级别
     */
    private float calculateZoomLevel(double latDiff, double lngDiff) {
        // 计算最大的差异
        double maxDiff = Math.max(latDiff, lngDiff * 1.5); // 经度差异需要乘以系数

        if (maxDiff < 0.001) { // 非常小的范围
            return 18;
        } else if (maxDiff < 0.005) {
            return 17;
        } else if (maxDiff < 0.01) {
            return 16;
        } else if (maxDiff < 0.02) {
            return 15;
        } else if (maxDiff < 0.05) {
            return 14;
        } else if (maxDiff < 0.1) {
            return 13;
        } else if (maxDiff < 0.2) {
            return 12;
        } else if (maxDiff < 0.5) {
            return 11;
        } else if (maxDiff < 1.0) {
            return 10;
        } else if (maxDiff < 2.0) {
            return 9;
        } else {
            return 8; // 最小缩放级别
        }
    }

    /**
     * 添加目的地标记（红色）
     */
    private void addDestinationMarker(LatLng location, String title) {
        OverlayOptions options = new MarkerOptions()
                .position(location)
                .icon(redMarkerIcon)
                .title(title)
                .zIndex(10);  // 最高层级

        destinationMarker = (Marker) mBaiduMap.addOverlay(options);

        // 显示信息窗口
        if (destinationMarker != null) {
            destinationMarker.showInfoWindow();
        }
    }

    /**
     * 添加公园标记（绿色带数字）
     */
    /**
     * 添加公园标记（使用您的图片）
     */
    private void addParkMarker(LatLng location, String title, int number) {
        if (location == null) {
            return;
        }

        if (greenMarkerIcon == null) {
            greenMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_park);
        }

        OverlayOptions options = new MarkerOptions()
                .position(location)
                .icon(greenMarkerIcon)
                .title("公园" + number + ": " + title)
                .zIndex(5);

        Marker parkMarker = (Marker) mBaiduMap.addOverlay(options);
        parkMarkers.add(parkMarker);
    }

    /**
     * 清除所有标记
     */
    private void clearMarkers() {
        // 清除目的地标记
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }

        // 清除公园标记
        for (Marker marker : parkMarkers) {
            if (marker != null) {
                marker.remove();
            }
        }
        parkMarkers.clear();
    }

    /**
     * 移动到指定位置
     */
    private void moveToLocation(LatLng latLng, float zoomLevel) {
        if (latLng == null) {
            return;
        }

        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mBaiduMap.animateMapStatus(update);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        try {
            mLocationClient = new LocationClient(getApplicationContext());

            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);
            option.setCoorType("bd09ll");
            option.setScanSpan(5000);  // 5秒更新一次
            option.setIsNeedAddress(true);
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            mLocationClient.setLocOption(option);

            mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // 更新城市信息
                        if (!TextUtils.isEmpty(location.getCity())) {
                            currentCity = location.getCity().replace("市", "");
                        }

                        // 更新地图位置
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(location.getRadius())
                                .direction(location.getDirection())
                                .latitude(location.getLatitude())
                                .longitude(location.getLongitude())
                                .build();
                        mBaiduMap.setMyLocationData(locData);

                        // 第一次定位时移动到当前位置
                        if (destinationMarker == null) {
                            moveToLocation(currentLocation, 18);
                        }
                    }
                }
            });

            mLocationClient.start();

        } catch (Exception e) {
            Toast.makeText(this, "定位服务初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 搜索结果适配器
     */
    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPoiName);
                tvAddress = itemView.findViewById(R.id.tvPoiAddress);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_poi_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PoiInfo poiInfo = searchResults.get(position);
            holder.tvName.setText(poiInfo.name);
            holder.tvAddress.setText(poiInfo.address);

            holder.itemView.setOnClickListener(v -> {
                selectDestination(poiInfo);
            });
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOC_PERMISSION);
        } else {
            initLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocation();
                Toast.makeText(this, "定位权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
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
        // 停止定位
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }

        // 释放POI搜索
        if (mPoiSearch != null) {
            mPoiSearch.destroy();
        }

        // 释放地图资源
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
        }

        // 释放标记图标
        if (redMarkerIcon != null) {
            redMarkerIcon.recycle();
        }
        if (greenMarkerIcon != null) {
            greenMarkerIcon.recycle();
        }

        // 销毁地图视图
        if (mMapView != null) {
            mMapView.onDestroy();
        }

        super.onDestroy();
    }
}