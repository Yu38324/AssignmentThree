package com.example.assignmentthree.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.assignmentthree.MapActivity;

public class PermissionManager {
    /**
     * 检查并申请定位权限
     */
    public static void checkAndRequestLocationPermission(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    requestCode);
        } else {
            // 权限已授予，初始化定位（调用MapActivity的initLocation方法）
            if (activity instanceof MapActivity) {
                ((MapActivity) activity).initLocation();
            }
        }
    }

    /**
     * 检查权限是否授予
     */
    public static boolean checkPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}