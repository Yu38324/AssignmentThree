package com.example.assignmentthree;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView btnStart;  // 改为 TextView 类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 确保使用正确的布局

        // 正确获取按钮（应该是TextView）
        btnStart = findViewById(R.id.btn_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转至百度地图核心页
                startActivity(new Intent(MainActivity.this, MapActivity.class));
                finish();
            }
        });
    }
}