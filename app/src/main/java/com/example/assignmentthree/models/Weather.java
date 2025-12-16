package com.example.assignmentthree.models;

import java.io.Serializable;

public class Weather implements Serializable {
    private int tempC; // 摄氏度
    private String condition; // 天气状况（如下雨、晴天）
    private String windDir; // 风向
    private int windKph; // 风速（公里/小时）
    private int humidity; // 湿度

    // Getter & Setter
    public int getTempC() { return tempC; }
    public void setTempC(int tempC) { this.tempC = tempC; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }
    public int getWindKph() { return windKph; }
    public void setWindKph(int windKph) { this.windKph = windKph; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
}