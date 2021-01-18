package com.example.myapplication;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ImageService {
    @GET("api/images")
    Call<List<Image>> getAllImageName();

    @GET("api/images/{filename}")
    Call<ResponseBody> getImage(@Path("filename") String filename);

    @Multipart
    @POST("api/images")
    Call<ResponseBody> createImage(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );
}