package com.example.assignmentthree.models;

public class Weather {
    private double temperature;
    private String condition;
    private double humidity;
    private double windSpeed;

    // 构造方法
    public Weather() {
    }

    public Weather(double temperature, String condition) {
        this.temperature = temperature;
        this.condition = condition;
    }

    // Getter 和 Setter 方法
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
}