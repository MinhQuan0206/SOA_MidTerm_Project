package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class LogoutBody {
    @SerializedName("username")
    private String username;
    @SerializedName("sessionId")
    private String sessionId;

    public LogoutBody(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }
}