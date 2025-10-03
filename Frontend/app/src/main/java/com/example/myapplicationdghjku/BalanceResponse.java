package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class BalanceResponse {
    @SerializedName("balance")
    private double balance;

    public double getBalance() { return balance; }
}