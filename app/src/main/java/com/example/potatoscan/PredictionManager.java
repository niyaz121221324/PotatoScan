package com.example.potatoscan;

import androidx.annotation.NonNull;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionManager {
    private final PredictionService predictionService;

    public PredictionManager() {
        predictionService = ApiClient.getClient().create(PredictionService.class);
    }

    public void predict(MultipartBody.Part file, PredictionCallback callback) {
        Call<PredictionResponse> call = predictionService.predict(file);

        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(@NonNull Call<PredictionResponse> call, @NonNull Response<PredictionResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new Exception("Failed to make prediction."));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PredictionResponse> call, @NonNull Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public interface PredictionCallback {
        void onSuccess(PredictionResponse predictionResponse);

        void onFailure(Throwable t);
    }

}