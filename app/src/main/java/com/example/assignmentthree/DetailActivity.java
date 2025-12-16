package com.example.assignmentthree;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.assignmentthree.api.APIManager;
import com.example.assignmentthree.models.Park;
import com.baidu.mapapi.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    private ImageView ivStreetView;
    private TextView tvParkName, tvParkAddress, tvOpeningHours, tvWeather, tvRatingText;
    private RatingBar ratingBar;
    private RecyclerView rvReviews;
    private Park selectedPark; // 全局Park对象

    private static final String BAIDU_MAP_AK = "T7l0oKZq05Rp6Fm844RwNG6W8PIuZYH0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 步骤1：正确用Parcelable获取Park对象（唯一一次获取）
        selectedPark = getIntent().getParcelableExtra("PARK_DATA");

        // 步骤2：校验对象是否为空
        if (selectedPark == null) {
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
            finish(); // 关闭页面
            return;
        }

        // 初始化UI + 加载数据（顺序不能乱）
        initViews();
        loadParkData(); // 现在用全局的selectedPark，不再重新获取
        loadStreetView();
        loadWeather();
        initEmptyReviews();
    }

    private void initViews() {
        ivStreetView = findViewById(R.id.iv_street_view);
        tvParkName = findViewById(R.id.tv_park_name);
        tvParkAddress = findViewById(R.id.tv_park_address);
        tvOpeningHours = findViewById(R.id.tv_opening_hours);
        tvWeather = findViewById(R.id.tv_weather);
        ratingBar = findViewById(R.id.rating_bar);
        tvRatingText = findViewById(R.id.tv_rating_text);
        rvReviews = findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadParkData() {
        // 直接使用全局的selectedPark，不再重新从Intent获取！
        // 移除错误的getSerializableExtra代码

        // 绑定数据到UI
        tvParkName.setText(selectedPark.getName());
        // 处理地址为空的情况
        tvParkAddress.setText(selectedPark.getAddress() != null ? selectedPark.getAddress() : "暂无地址");

        // 处理开放时间
        if (selectedPark.getOpeningHours() != null) {
            tvOpeningHours.setText("Hours: " + selectedPark.getOpeningHours());
        } else {
            tvOpeningHours.setText("Hours: 暂无信息");
        }

        // 补充评分显示（原代码缺失，可选）
        ratingBar.setRating(selectedPark.getRating());
        tvRatingText.setText(String.format("%.1f", selectedPark.getRating()) + " (" + selectedPark.getReviewCount() + " reviews)");
    }

    private void loadStreetView() {
        if (selectedPark == null || selectedPark.getLatLng() == null) return;

        LatLng latLng = selectedPark.getLatLng();
        String url = String.format(
                "https://api.map.baidu.com/panorama/v2?ak=%s&width=600&height=400&fov=120&location=%f,%f",
                BAIDU_MAP_AK, latLng.longitude, latLng.latitude
        );

        ImageRequest request = new ImageRequest(
                url,
                response -> ivStreetView.setImageBitmap(response),
                0, 0, ImageView.ScaleType.CENTER_CROP, null,
                error -> ivStreetView.setBackgroundColor(0xFFE0E0E0)
        );

        APIManager.getInstance(this).addToRequestQueue(request);
    }

    private void loadWeather() {
        if (selectedPark == null || selectedPark.getLatLng() == null) return;

        // 百度地图天气服务需要行政区划代码
        // 这里假设Park对象有getDistrictId()方法获取行政区划代码
        String districtId = "110000"; // 默认为北京，实际应从Park对象获取
        String url = String.format(
                "https://api.map.baidu.com/weather/v1/?district_id=%s&data_type=now&ak=%s",
                districtId, BAIDU_MAP_AK
        );

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> parseWeatherResponse(response),
                error -> tvWeather.setText("Current Weather: Data unavailable")
        );

        APIManager.getInstance(this).addToRequestQueue(request);
    }

    private void parseWeatherResponse(JSONObject response) {
        try {
            if (response.getInt("status") == 0) {
                JSONObject now = response.getJSONObject("result").getJSONObject("now");
                String weatherText = String.format(
                        "Current Weather: Temperature %s°C, %s",
                        now.getString("temp"), now.getString("text")
                );
                tvWeather.setText(weatherText);
            }
        } catch (JSONException e) {
            tvWeather.setText("Current Weather: Data parsing failed");
            e.printStackTrace(); // 打印异常，方便调试
        }
    }

    private void initEmptyReviews() {
        // 只初始化适配器，不添加模拟数据
        rvReviews.setAdapter(new ReviewAdapter());
        rvReviews.setVisibility(android.view.View.GONE);
        findViewById(R.id.tv_reviews_title).setVisibility(android.view.View.GONE);
    }

    // 简化适配器 - 无数据状态
    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // 使用简单布局
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(20, 20, 20, 20);
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText("No reviews available");
        }

        @Override
        public int getItemCount() {
            return 0; // 无数据
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(TextView v) {
                super(v);
                textView = v;
            }
        }
    }
}