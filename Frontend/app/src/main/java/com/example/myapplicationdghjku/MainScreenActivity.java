package com.example.myapplicationdghjku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainScreenActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private MaterialToolbar toolbar;
    private TextView tvAccountBalance;
    private TextInputEditText etStudentId, etStudentName;
    private RecyclerView rvCourses;
    private TextView tvTotalFee;
    private Button btnPay;

    private ApiService apiService;
    private String username;
    private double totalFee = 0.0;
    private CourseAdapter adapter;
    private List<Course> courses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các view với kiểm tra null
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        tvAccountBalance = findViewById(R.id.tv_account_balance);
        etStudentId = findViewById(R.id.et_student_id);
        etStudentName = findViewById(R.id.et_student_name);
        rvCourses = findViewById(R.id.rv_courses);
        tvTotalFee = findViewById(R.id.tv_total_fee);
        btnPay = findViewById(R.id.btn_pay);

        if (toolbar == null || drawerLayout == null || navView == null) {
            Toast.makeText(this, "Lỗi tải giao diện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cài đặt Toolbar và Drawer
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Cài đặt NavigationView và lấy username một lần
        navView.setNavigationItemSelectedListener(this);
        TextView tvNavUsername = navView.getHeaderView(0).findViewById(R.id.tv_nav_username);
        if (tvNavUsername != null) {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            username = prefs.getString("username", "Khách");
            tvNavUsername.setText(username); // Hiển thị tên người dùng
        } else {
            Toast.makeText(this, "Lỗi tải header sidebar", Toast.LENGTH_SHORT).show();
        }

        // Khởi tạo API service (dù không dùng API ở đây)
        apiService = RetrofitClient.getClient("http://10.0.2.2:3000/").create(ApiService.class);

        // Khởi tạo RecyclerView
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(courses);
        rvCourses.setAdapter(adapter);

        // Thêm dữ liệu giả định
        addMockData();

        // Load dữ liệu khác (không cần gọi loadCourses)
        loadBalance();
        loadStudentInfo();

        // Xử lý sự kiện nút thanh toán
        btnPay.setOnClickListener(v -> {
            if (totalFee > 0) {
                startActivity(new Intent(this, OtpActivity.class));
            } else {
                Toast.makeText(this, "Không có học phí cần thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMockData() {
        // Thêm dữ liệu giả định
        courses.add(new Course("CS101", "Lập trình Java", 3, 1500000.00));
        courses.add(new Course("CS102", "Cơ sở dữ liệu", 4, 2000000.00));
        courses.add(new Course("CS103", "Mạng máy tính", 3, 1800000.00));

        // Tính tổng học phí
        totalFee = 0;
        for (Course c : courses) {
            totalFee += c.getFee();
        }
        if (tvTotalFee != null) {
            tvTotalFee.setText("Tổng học phí: " + totalFee + " VNĐ");
        }

        // Cập nhật adapter
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Mock data added, total courses: " + courses.size() + ", total fee: " + totalFee);
    }

    private void loadBalance() {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getBalance(username).enqueue(new Callback<BalanceResponse>() {
            @Override
            public void onResponse(Call<BalanceResponse> call, Response<BalanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvAccountBalance.setText("Số dư: " + response.body().getBalance() + " VNĐ");
                } else {
                    Log.e(TAG, "Load balance error: " + response.message());
                    Toast.makeText(MainScreenActivity.this, "Lỗi load số dư: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BalanceResponse> call, Throwable t) {
                Log.e(TAG, "Load balance failure: " + t.getMessage());
                Toast.makeText(MainScreenActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentInfo() {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getStudentInfo(username).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Student student = response.body();
                    if (etStudentId != null) etStudentId.setText(student.getStudentId());
                    if (etStudentName != null) etStudentName.setText(student.getName());
                } else {
                    Log.e(TAG, "Load student info error: " + response.message());
                    Toast.makeText(MainScreenActivity.this, "Lỗi load info sinh viên: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Log.e(TAG, "Load student info failure: " + t.getMessage());
                Toast.makeText(MainScreenActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        // Tạm thời không gọi API, đã dùng dữ liệu giả định
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_pay_other) {
            startActivity(new Intent(this, PayOtherActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else if (id == R.id.nav_logout) {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}