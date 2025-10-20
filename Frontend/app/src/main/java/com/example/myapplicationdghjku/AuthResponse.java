package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("sessionId")
    private String sessionId;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getSessionId() { return sessionId; }
}
