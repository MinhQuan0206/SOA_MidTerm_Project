package com.example.myapplicationdghjku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private ApiService apiService;
    private String username;
    private TransactionAdapter adapter;
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Khởi tạo RecyclerView
        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(transactions); // Khởi tạo adapter
        rvHistory.setAdapter(adapter);

        // Lấy username từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        username = prefs.getString("username", "");
        if (username.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo API service
        apiService = RetrofitClient.getClient("http://10.0.2.2:3000/").create(ApiService.class);

        // Load dữ liệu
        loadHistory();
    }

    private void loadHistory() {
        Call<List<Transaction>> call = apiService.getTransactions(username);
        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactions.clear();
                    transactions.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (transactions.isEmpty()) {
                        Toast.makeText(HistoryActivity.this, "Không có giao dịch nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Lỗi tải dữ liệu: " + (response.body() != null ? response.body().toString() : response.message()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}