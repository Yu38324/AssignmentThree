package com.example.assignmentthree;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 地图初始化代码将在后续添加
        // 现在只确保Activity可以正常运行
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 地图恢复
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 地图暂停
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 地图销毁
    }
}