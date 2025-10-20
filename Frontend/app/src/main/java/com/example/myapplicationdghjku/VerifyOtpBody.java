package com.example.myapplicationdghjku;

public class VerifyOtpBody {
    private String username;
    private String otp;
    private String studentId;
    private boolean isSelf;
    private String sessionId;
    public VerifyOtpBody(String username, String otp, String studentId, boolean isSelf) {
        this.username = username;
        this.otp = otp;
        this.studentId = studentId;
        this.isSelf = isSelf;
    }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUsername() { return username; }
    public String getOtp() { return otp; }
    public String getStudentId() { return studentId; }
    public boolean isSelf() { return isSelf; }
}
