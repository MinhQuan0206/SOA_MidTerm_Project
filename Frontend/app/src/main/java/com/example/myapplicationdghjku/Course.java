package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("course_id")
    private String courseId;
    @SerializedName("name")
    private String name;
    @SerializedName("credits")
    private int credits;
    @SerializedName("fee")
    private double fee;

    // Constructor không tham số (mặc định, giữ nguyên)
    public Course() {
    }

    // Constructor với 4 tham số
    public Course(String courseId, String name, int credits, double fee) {
        this.courseId = courseId;
        this.name = name;
        this.credits = credits;
        this.fee = fee;
    }

    public String getCourseId() { return courseId; }
    public String getName() { return name; }
    public int getCredits() { return credits; }
    public double getFee() { return fee; }
}