package com.example.myapplicationdghjku;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("username")
    private String sender;             // Người chuyển khoản

    @SerializedName("studentId")
    private String receiverStudentId;  // Mã SV được thanh toán

    @SerializedName("amount")
    private double amount;             // Số tiền

    @SerializedName("date")
    private String datetime;           // Ngày giờ

    @SerializedName("status")
    private String status;             // Trạng thái

    // Constructor đầy đủ
    public Transaction(String sender, String receiverStudentId, double amount, String datetime, String status) {
        this.sender = sender;
        this.receiverStudentId = receiverStudentId;
        this.amount = amount;
        this.datetime = datetime;
        this.status = status;
    }

    // Getter & Setter
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiverStudentId() {
        return receiverStudentId;
    }

    public void setReceiverStudentId(String receiverStudentId) {
        this.receiverStudentId = receiverStudentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
