package com.example.potatoscan;

public class PredictionResponse {
    private String _className;
    private String _confidence;

    public PredictionResponse(String className, String confidence) {
        _className = className;
        _confidence = confidence;
    }

    public PredictionResponse() {
        this("", "");
    }

    public String getClassName() {
        return _className;
    }

    public void setClassName(String className) {
        _className = className;
    }

    public String getConfidence() {
        return _confidence;
    }

    public void setConfidence(String confidence) {
        _confidence = confidence;
    }
}
