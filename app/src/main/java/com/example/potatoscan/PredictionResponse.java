package com.example.potatoscan;

import androidx.annotation.NonNull;

public class PredictionResponse {
    private String className;
    private Double confidence;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    @NonNull
    public String toString() {
        return "Class: " + className + ", Confidence: " + confidence;
    }
}
