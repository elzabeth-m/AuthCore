package com.elsa.authcore.AuthCore.dto;

public class ResponseWrapper<T> {
    private boolean success;
    private T data;
    private String message;

    public ResponseWrapper() {}

    public ResponseWrapper(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}