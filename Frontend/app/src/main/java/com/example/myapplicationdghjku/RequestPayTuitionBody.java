package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class RequestPayTuitionBody {

    @SerializedName("username")
    private String username;

    @SerializedName("studentId")
    private String studentId;

    @SerializedName("isSelf")
    private boolean isSelf;
    @SerializedName("sessionId")
    private String sessionId;
    public RequestPayTuitionBody(String username, String studentId, boolean isSelf) {
        this.username = username;
        this.studentId = studentId;
        this.isSelf = isSelf;
    }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUsername() { return username; }
    public String getStudentId() { return studentId; }
    public boolean isSelf() { return isSelf; }

    public void setUsername(String username) { this.username = username; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setSelf(boolean self) { isSelf = self; }
}
