package com.example.assignmentthree.utils;

public class Utils {

    // 格式化温度显示
    public static String formatTemperature(double celsius) {
        return String.format("%.1f°C", celsius);
    }

    // 计算距离（简单示例）
    public static String formatDistance(double distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format("%.0f米", distanceInMeters);
        } else {
            return String.format("%.1f公里", distanceInMeters / 1000);
        }
    }
}
