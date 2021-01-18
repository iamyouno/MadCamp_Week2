package com.example.myapplication;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public class Image {
    @SerializedName("name")
    public String name;
    public Bitmap bm;

    public transient File original;
    public transient File thumbnail;

    public Image(String name, File original, File thumbnail){
        this.name = name;
        this.original = original;
        this.thumbnail = thumbnail;
    }
}