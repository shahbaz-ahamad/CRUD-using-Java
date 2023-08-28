package com.example.firebase;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DataClass implements Parcelable {
    private String title;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DataClass() {
    }

    private String imageResource;

    public DataClass(String title, String imageResource,String key) {
        this.title = title;
        this.imageResource = imageResource;
        this.key=key;
    }

    protected DataClass(Parcel in) {
        key = in.readString();
        title = in.readString();
        imageResource = in.readString();
    }

    public static final Creator<DataClass> CREATOR = new Creator<DataClass>() {
        @Override
        public DataClass createFromParcel(Parcel in) {
            return new DataClass(in);
        }

        @Override
        public DataClass[] newArray(int size) {
            return new DataClass[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getImageResource() {
        return imageResource;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(title);
        parcel.writeString(imageResource);
    }
}
