package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class Student {
    @SerializedName("student_id")
    private String studentId;
    @SerializedName("name")
    private String name;

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
}