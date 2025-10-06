package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Student {

    @SerializedName("username")
    private String username;

    @SerializedName("studentId")
    private String studentId;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    @SerializedName("courses")
    private List<Course> courses;

    public Student() {}

    public Student(String username, String studentId, String name, double balance, List<Course> courses) {
        this.username = username;
        this.studentId = studentId;
        this.name = name;
        this.balance = balance;
        this.courses = courses;
    }

    public String getUsername() { return username; }
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public List<Course> getCourses() { return courses; }

    public void setUsername(String username) { this.username = username; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setName(String name) { this.name = name; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    @Override
    public String toString() {
        return "Student{" +
                "username='" + username + '\'' +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", courses=" + courses +
                '}';
    }
}
