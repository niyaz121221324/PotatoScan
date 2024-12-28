package com.example.potatoscan;

import androidx.annotation.NonNull;

public class PredictionResponse {
    private String predictedClass;
    private Double confidence;

    public String getClassName() {
        return predictedClass;
    }

    public void setClassName(String className) {
        this.predictedClass = className;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    @NonNull
    public String toString() {
        return "Class: " + predictedClass + ", Confidence: " + confidence;
    }
}
