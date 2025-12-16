package com.example.assignmentthree.utils;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {
    private static final String TAG = "Utils";

    /**
     * JSON解析工具（示例）
     */
    public static String parseJsonString(JSONObject jsonObject, String key) {
        try {
            if (jsonObject != null && jsonObject.has(key)) {
                return jsonObject.getString(key);
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON解析失败：" + e.getMessage());
        }
        return "";
    }

    /**
     * 坐标转换工具（如需转换坐标系可扩展）
     */
    public static double[] convertCoord(double lat, double lon) {
        // 预留：百度BD09、高德GCJ02、WGS84坐标转换逻辑
        return new double[]{lat, lon};
    }
}