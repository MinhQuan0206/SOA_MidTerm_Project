package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("date")
    private String date;
    @SerializedName("amount")
    private double amount;
    @SerializedName("status")
    private String status;

    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
}