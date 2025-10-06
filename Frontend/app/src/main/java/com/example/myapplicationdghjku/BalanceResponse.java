package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class BalanceResponse {

    @SerializedName("balance")
    private double balance;

    public BalanceResponse() {}

    public BalanceResponse(double balance) { this.balance = balance; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return "BalanceResponse{balance=" + balance + '}';
    }
}
