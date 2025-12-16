package com.example.assignmentthree;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 接收传递过来的数据
        // String parkName = getIntent().getStringExtra("park_name");
        // 在这里处理数据显示
    }
}