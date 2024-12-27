package com.example.potatoscan;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PredictionService {
    @Multipart
    @POST("/predict")
    Call<PredictionResponse> predict(@Part MultipartBody.Part file);
}
