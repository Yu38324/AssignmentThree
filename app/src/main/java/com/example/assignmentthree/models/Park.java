package com.example.assignmentthree.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.baidu.mapapi.model.LatLng;

// 替换Serializable为Parcelable
public class Park implements Parcelable {
    private String name;
    private String address;
    private LatLng latLng;
    private String openingHours;
    private float rating;
    private int reviewCount;
    private String districtId;

    // 空构造（保留，兼容原有逻辑）
    public Park() {}

    // 带参构造（可选，方便创建对象）
    public Park(String name, String address, LatLng latLng, String openingHours, float rating, int reviewCount, String districtId) {
        this.name = name;
        this.address = address;
        this.latLng = latLng;
        this.openingHours = openingHours;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.districtId = districtId;
    }

    // Parcelable核心：从Parcel读取数据
    protected Park(Parcel in) {
        name = in.readString();
        address = in.readString();
        // LatLng本身实现了Parcelable，直接读取
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        openingHours = in.readString();
        rating = in.readFloat();
        reviewCount = in.readInt();
        districtId = in.readString();
    }

    // Parcelable固定写法：创建器
    public static final Creator<Park> CREATOR = new Creator<Park>() {
        @Override
        public Park createFromParcel(Parcel in) {
            return new Park(in);
        }

        @Override
        public Park[] newArray(int size) {
            return new Park[size];
        }
    };

    @Override
    public int describeContents() {
        return 0; // 固定返回0（无文件描述符）
    }

    // Parcelable核心：写入数据到Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        // 写入LatLng对象
        dest.writeParcelable(latLng, flags);
        dest.writeString(openingHours);
        dest.writeFloat(rating);
        dest.writeInt(reviewCount);
        dest.writeString(districtId);
    }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    // 原有Getter & Setter（保留不变）
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LatLng getLatLng() { return latLng; }
    public void setLatLng(LatLng latLng) { this.latLng = latLng; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}