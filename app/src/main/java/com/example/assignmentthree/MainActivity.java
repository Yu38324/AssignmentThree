package com.example.assignmentthree;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(v -> {
            // 跳转至百度地图核心页
            startActivity(new Intent(MainActivity.this, MapActivity.class));
            finish();
        });
    }
}