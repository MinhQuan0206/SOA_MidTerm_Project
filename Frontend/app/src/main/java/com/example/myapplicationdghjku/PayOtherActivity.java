package com.example.myapplicationdghjku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayOtherActivity extends AppCompatActivity {

    private TextInputEditText etStudentId, etStudentName;
    private Button btnSearch, btnPay;
    private RecyclerView rvCourses;
    private TextView tvTotalFee;
    private ApiService apiService;
    private double totalFee = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_other); // XML bạn tạo theo hướng dẫn trước

        etStudentId = findViewById(R.id.et_student_id);
        etStudentName = findViewById(R.id.et_student_name);
        btnSearch = findViewById(R.id.btn_search);
        rvCourses = findViewById(R.id.rv_courses);
        tvTotalFee = findViewById(R.id.tv_total_fee);
        btnPay = findViewById(R.id.btn_pay);

        apiService = RetrofitClient.getClient("http://10.0.2.2:3000/").create(ApiService.class);

        btnSearch.setOnClickListener(v -> {
            String studentId = etStudentId.getText().toString().trim();
            if (studentId.isEmpty()) {
                Toast.makeText(this, "Nhập mã sinh viên", Toast.LENGTH_SHORT).show();
                return;
            }
            loadStudent(studentId);
            loadCourses(studentId);
        });

        btnPay.setOnClickListener(v -> {
            if (totalFee > 0) {
                startActivity(new Intent(this, OtpActivity.class));
            } else {
                Toast.makeText(this, "Không có học phí cần thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudent(String studentId) {
        apiService.searchStudent(studentId).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etStudentName.setText(response.body().getName());
                } else {
                    Toast.makeText(PayOtherActivity.this, "Không tìm thấy sinh viên: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Toast.makeText(PayOtherActivity.this, "Lỗi tìm sinh viên: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses(String studentId) {
        apiService.getCourses(studentId).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();
                    CourseAdapter adapter = new CourseAdapter(courses); // Giả sử bạn đã có CourseAdapter
                    rvCourses.setLayoutManager(new LinearLayoutManager(PayOtherActivity.this));
                    rvCourses.setAdapter(adapter);

                    totalFee = 0;
                    for (Course c : courses) {
                        totalFee += c.getFee();
                    }
                    tvTotalFee.setText("Tổng học phí: " + totalFee + " VNĐ");
                } else {
                    Toast.makeText(PayOtherActivity.this, "Lỗi load môn học: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(PayOtherActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}