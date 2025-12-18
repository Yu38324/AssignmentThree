package com.example.assignmentthree;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.example.assignmentthree.models.Park;
import com.example.assignmentthree.utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    private static final int REQUEST_LOC_PERMISSION = 1001;

    // UI组件
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private EditText etSearch;
    private ImageButton ivSearchBtn;
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
    private List<PoiInfo> nearbyParks = new ArrayList<>();  // 存储附近公园列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 初始化UI组件
        initUI();

        // 初始化地图
        initMap();

        // 初始化POI搜索
        initPoiSearch();

        // 初始化标记图标
        initMarkerIcons();
        initLocation();

        // 检查并申请权限
        PermissionManager.checkAndRequestLocationPermission(this, REQUEST_LOC_PERMISSION);
    }

    private void initUI() {
        mMapView = findViewById(R.id.bmapView);
        etSearch = findViewById(R.id.etSearch);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        ivSearchBtn = findViewById(R.id.ivSearchBtn);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        cardSearchResults = findViewById(R.id.cardSearchResults);
        progressBar = findViewById(R.id.progressBar);

        // 设置搜索按钮监听
        ivSearchBtn.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                searchDestination(query);
                hideKeyboard();
            } else {
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置EditText的编辑器动作
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    searchDestination(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        etSearch.setOnClickListener(v -> {
            etSearch.requestFocus();
            showKeyboard();
        });

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard();
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

        // 初始化搜索结果列表适配器
        searchResultAdapter = new SearchResultAdapter();
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchResultAdapter);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initMap() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        // 设置地图点击监听器
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideSearchResults();
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                hideSearchResults();
            }
        });

        // 设置标记点击监听器（跳转详情页）
        mBaiduMap.setOnMarkerClickListener(marker -> {
            if (marker != null && marker.getTitle() != null) {
                // 获取标记的真实位置
                LatLng markerPosition = marker.getPosition();
                if (markerPosition == null) {
                    Log.e(TAG, "Marker position is null!");
                    return false;
                }

                Log.d(TAG, "Marker clicked: " + marker.getTitle() +
                        " at " + markerPosition.latitude + ", " + markerPosition.longitude);

                // 检查是否是公园标记（标题以"公园"开头）
                String title = marker.getTitle();
                if (title.startsWith("公园")) {
                    // 从附近公园列表中查找对应的公园
                    int parkNumber = 0;
                    try {
                        // 从标题中提取编号，如"公园1: xxx"
                        String numberStr = title.substring(2, title.indexOf(":"));
                        parkNumber = Integer.parseInt(numberStr);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse park number from title: " + title);
                    }

                    // 创建Park对象
                    Park park = new Park();
                    if (parkNumber > 0 && parkNumber <= nearbyParks.size()) {
                        PoiInfo parkInfo = nearbyParks.get(parkNumber - 1);
                        park.setName(parkInfo.name);
                        park.setAddress(parkInfo.address);
                        park.setLatLng(parkInfo.location);
                    } else {
                        // 如果无法从列表中获取，使用标记信息
                        park.setName(title);
                        park.setAddress("");
                        park.setLatLng(markerPosition);
                    }

                    park.setOpeningHours("全天开放");

                    Intent intent = new Intent(MapActivity.this, DetailActivity.class);
                    intent.putExtra("PARK_DATA", park);
                    startActivity(intent);
                    return true;
                }
            }
            return false;
        });
    }

    private void hideSearchResults() {
        if (cardSearchResults != null) {
            cardSearchResults.setVisibility(View.GONE);
        }
        if (etSearch != null) {
            etSearch.clearFocus();
        }
        hideKeyboard();
    }

    private void initPoiSearch() {
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                progressBar.setVisibility(View.GONE);

                if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Toast.makeText(MapActivity.this, "未找到相关地点", Toast.LENGTH_SHORT).show();
                    cardSearchResults.setVisibility(View.GONE);
                    return;
                }

                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    searchResults.clear();
                    searchResults.addAll(poiResult.getAllPoi());
                    searchResultAdapter.notifyDataSetChanged();
                    cardSearchResults.setVisibility(View.VISIBLE);

                    // 单个结果自动选择
                    if (searchResults.size() == 1) {
                        selectDestination(searchResults.get(0));
                    }
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {}
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {}
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {}
        });
    }

    private void initMarkerIcons() {
        // 红色标记 - 目的地
        redMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_searched);
        // 绿色标记 - 公园
        greenMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.img_marker_location_park);
    }

    private void searchDestination(String keyword) {
        Log.d(TAG, "搜索关键词: " + keyword);
        progressBar.setVisibility(View.VISIBLE);
        searchResults.clear();
        searchResultAdapter.notifyDataSetChanged();

        PoiCitySearchOption citySearchOption = new PoiCitySearchOption()
                .city(currentCity)
                .keyword(keyword)
                .pageNum(0)
                .pageCapacity(20);

        Log.d(TAG, "搜索城市: " + currentCity);
        mPoiSearch.searchInCity(citySearchOption);
    }

    private void selectDestination(PoiInfo poiInfo) {
        if (poiInfo == null || poiInfo.location == null) {
            Toast.makeText(this, "无效的地点信息", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "selectDestination called for: " + poiInfo.name);

        // 隐藏搜索结果列表
        cardSearchResults.setVisibility(View.GONE);
        clearMarkers();

        // 清空之前的公园列表
        nearbyParks.clear();

        // 添加目的地标记（红色）
        LatLng destination = poiInfo.location;
        addDestinationMarker(destination, poiInfo.name);

        // 更新搜索框显示
        etSearch.setText(poiInfo.name);
        etSearch.clearFocus();

        // 隐藏键盘
        hideKeyboard();

        // 搜索附近的公园
        searchNearbyParks(destination);

        Log.d(TAG, "卡片已设置为GONE");
    }

    private void searchNearbyParks(LatLng center) {
        Log.d(TAG, "搜索附近公园，中心点: " + center.latitude + ", " + center.longitude);
        progressBar.setVisibility(View.VISIBLE);

        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption()
                .location(center)
                .keyword("公园")
                .radius(5000)  // 5公里半径
                .pageNum(0)
                .pageCapacity(10);  // 获取10个结果

        PoiSearch parkPoiSearch = PoiSearch.newInstance();
        parkPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                progressBar.setVisibility(View.GONE);

                if (poiResult == null) {
                    Log.d(TAG, "公园搜索结果为空");
                    Toast.makeText(MapActivity.this, "未找到附近公园", Toast.LENGTH_SHORT).show();
                    moveToLocation(center, 16);
                    parkPoiSearch.destroy();
                    return;
                }

                Log.d(TAG, "公园搜索错误码: " + poiResult.error);
                Log.d(TAG, "公园搜索结果数量: " + poiResult.getAllPoi().size());

                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    nearbyParks.clear();
                    nearbyParks.addAll(poiResult.getAllPoi());
                    int count = Math.min(nearbyParks.size(), 10);

                    List<LatLng> allLatLngs = new ArrayList<>();
                    allLatLngs.add(center);  // 添加目的地位置

                    for (int i = 0; i < count; i++) {
                        PoiInfo park = nearbyParks.get(i);
                        addParkMarker(park.location, park.name, i + 1);
                        allLatLngs.add(park.location);
                    }

                    // 调整地图视图以显示所有标记
                    adjustMapView(allLatLngs);
                    Toast.makeText(MapActivity.this,
                            "找到" + count + "个附近公园", Toast.LENGTH_SHORT).show();
                } else {
                    // 即使没有找到公园，也调整到目的地
                    List<LatLng> onlyDestination = new ArrayList<>();
                    onlyDestination.add(center);
                    adjustMapView(onlyDestination);
                    Toast.makeText(MapActivity.this, "未找到附近公园", Toast.LENGTH_SHORT).show();
                }

                parkPoiSearch.destroy();
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

    private void adjustMapView(List<LatLng> latLngs) {
        Log.d(TAG, "adjustMapView called with " + (latLngs != null ? latLngs.size() : 0) + " points");

        if (latLngs == null || latLngs.isEmpty()) {
            Log.d(TAG, "adjustMapView: latLngs is null or empty");
            return;
        }

        if (latLngs.size() == 1) {
            Log.d(TAG, "adjustMapView: Only 1 point, moving to it");
            moveToLocation(latLngs.get(0), 16);
            return;
        }

        Log.d(TAG, "adjustMapView: Calculating view for " + latLngs.size() + " points");

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

        double centerLat = (minLat + maxLat) / 2;
        double centerLng = (minLng + maxLng) / 2;
        LatLng center = new LatLng(centerLat, centerLng);

        double latDiff = maxLat - minLat;
        double lngDiff = maxLng - minLng;
        float zoomLevel = calculateZoomLevel(latDiff, lngDiff);

        Log.d(TAG, "adjustMapView: Center = " + centerLat + ", " + centerLng + ", zoom = " + zoomLevel);

        moveToLocation(center, zoomLevel);
    }

    private float calculateZoomLevel(double latDiff, double lngDiff) {
        double maxDiff = Math.max(latDiff, lngDiff * 1.5);
        if (maxDiff < 0.001) return 18;
        else if (maxDiff < 0.005) return 17;
        else if (maxDiff < 0.01) return 16;
        else if (maxDiff < 0.02) return 15;
        else if (maxDiff < 0.05) return 14;
        else if (maxDiff < 0.1) return 13;
        else if (maxDiff < 0.2) return 12;
        else if (maxDiff < 0.5) return 11;
        else if (maxDiff < 1.0) return 10;
        else if (maxDiff < 2.0) return 9;
        else return 8;
    }

    private void addDestinationMarker(LatLng location, String title) {
        OverlayOptions options = new MarkerOptions()
                .position(location)
                .icon(redMarkerIcon)
                .title(title)
                .zIndex(10);
        destinationMarker = (Marker) mBaiduMap.addOverlay(options);
    }

    private void addParkMarker(LatLng location, String title, int number) {
        if (location == null) return;
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

    private void clearMarkers() {
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
        for (Marker marker : parkMarkers) {
            if (marker != null) marker.remove();
        }
        parkMarkers.clear();
    }

    private void moveToLocation(LatLng latLng, float zoomLevel) {
        if (latLng == null) return;
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        mBaiduMap.animateMapStatus(update);
    }

    public void initLocation() {
        try {
            mLocationClient = new LocationClient(getApplicationContext());
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);
            option.setCoorType("bd09ll");
            option.setScanSpan(5000);
            option.setIsNeedAddress(true);
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            mLocationClient.setLocOption(option);

            mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (!TextUtils.isEmpty(location.getCity())) {
                            currentCity = location.getCity().replace("市", "");
                        }

                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(location.getRadius())
                                .direction(location.getDirection())
                                .latitude(location.getLatitude())
                                .longitude(location.getLongitude())
                                .build();
                        mBaiduMap.setMyLocationData(locData);

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

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPoiName);
                tvAddress = itemView.findViewById(R.id.tvPoiAddress);

                // 设置点击监听器
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PoiInfo poiInfo = searchResults.get(position);
                        Log.d(TAG, "点击搜索结果: " + poiInfo.name);
                        selectDestination(poiInfo);
                    }
                });
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
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOC_PERMISSION) {
            if (PermissionManager.checkPermissionGranted(grantResults)) {
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
        if (mPoiSearch != null) mPoiSearch.destroy();
        // 释放地图资源
        if (mBaiduMap != null) mBaiduMap.setMyLocationEnabled(false);
        // 释放标记图标
        if (redMarkerIcon != null) redMarkerIcon.recycle();
        if (greenMarkerIcon != null) greenMarkerIcon.recycle();
        // 销毁地图视图
        if (mMapView != null) mMapView.onDestroy();
        super.onDestroy();
    }
}