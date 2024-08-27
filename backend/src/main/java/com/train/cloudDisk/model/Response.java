package com.train.cloudDisk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response<T> {
    @JsonProperty("code")
    private String code;
    @JsonProperty("msg")
    private String message;
    @JsonProperty("data")
    private T data;

    public Response(@JsonProperty("code") String code,
                    @JsonProperty("msg") String message,
                    @JsonProperty("data") T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Getters and optionally setters
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String toJSON() {
        return "{\"code\":\"" + code + "\",\"msg\":\"" + message + "\",\"data\":\"" + data + "\"}";
    }
}