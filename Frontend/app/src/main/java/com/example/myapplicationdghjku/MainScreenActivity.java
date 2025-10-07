package com.example.myapplicationdghjku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MainScreenActivity - hiển thị thông tin sinh viên, bảng môn học (TableLayout),
 * thanh tìm kiếm theo mã sinh viên, và danh sách transactions bằng RecyclerView.
 */
public class MainScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainScreenActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private MaterialToolbar toolbar;

    private TextView tvAccountBalance;
    private TextInputEditText etStudentId, etStudentName, etSearchStudent;
    private TableLayout tlCourses, tlTransactions;
    private TextView tvTotalFee;
    private Button btnPay, btnSearch;

    private ApiService apiService;
    private String username;
    private double totalFee = 0.0;
    private boolean isSelf = true;
    private final List<Course> courses = new ArrayList<>();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    private ActivityResultLauncher<Intent> successLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(MainScreenActivity.this, "Thanh toán thành công, reload dữ liệu", Toast.LENGTH_SHORT).show();
                        String currentStudentId = etStudentId.getText().toString();
                        if (currentStudentId != null && !currentStudentId.isEmpty()) {
                            loadCourses(currentStudentId);
                            loadTransactions();
                        }
                        loadBalance(); // Thêm reload số dư
                        loadStudentInfo(); // Thêm reload thông tin sinh viên nếu cần
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ view
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        tvAccountBalance = findViewById(R.id.tv_account_balance);
        etStudentId = findViewById(R.id.et_student_id);
        etStudentName = findViewById(R.id.et_student_name);
        etSearchStudent = findViewById(R.id.et_search_student);
        tlCourses = findViewById(R.id.tl_courses);
        tlTransactions = findViewById(R.id.tableTransactions);
        tvTotalFee = findViewById(R.id.tv_total_fee);
        btnPay = findViewById(R.id.btn_pay);
        btnSearch = findViewById(R.id.btn_search);

        // Setup toolbar + drawer
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);

        // Lấy username từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        username = prefs.getString("username", null);
        if (username == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Khởi tạo Retrofit client
        apiService = RetrofitClient.getClient("http://10.0.2.2:3000/").create(ApiService.class);

        // Nút thanh toán
        btnPay.setOnClickListener(v -> {
            if (totalFee <= 0) {
                Toast.makeText(this, "Không có học phí cần thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentStudentId = etStudentId.getText().toString();
            boolean isSelfPayment = currentStudentId.equals(username);
            RequestPayTuitionBody body = new RequestPayTuitionBody(username, currentStudentId, isSelfPayment);
            apiService.requestPayTuition(body).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse resp = response.body();
                        if (resp.isSuccess()) {
                            Toast.makeText(MainScreenActivity.this, "OTP đã gửi vào email", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainScreenActivity.this, OtpActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("studentId", currentStudentId);
                            intent.putExtra("isSelf", isSelfPayment);
                            successLauncher.launch(intent); // Sử dụng launcher thay vì startActivity
                        } else {
                            Toast.makeText(MainScreenActivity.this, resp.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(MainScreenActivity.this, "Lỗi gửi OTP", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Nút tìm kiếm sinh viên
        btnSearch.setOnClickListener(v -> {
            String studentId = etSearchStudent.getText().toString().trim();
            if (!studentId.isEmpty()) {
                loadStudentInfoById(studentId);
            } else {
                isSelf = true;
                Toast.makeText(this, "Nhập mã sinh viên để tìm", Toast.LENGTH_SHORT).show();
            }
        });

        // Load dữ liệu ban đầu
        loadBalance();
        loadStudentInfo();
        loadTransactions();
    }

    // ----- TableLayout: thêm một hàng môn học -----
    private void addCourseRow(String code, String name, int credits, double fee) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView tvCode = new TextView(this);
        tvCode.setText(code);
        tvCode.setGravity(Gravity.CENTER);
        tvCode.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        tvCode.setLayoutParams(lp);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        TableRow.LayoutParams lpName = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
        tvName.setLayoutParams(lpName);

        TextView tvCredits = new TextView(this);
        tvCredits.setText(String.valueOf(credits));
        tvCredits.setGravity(Gravity.CENTER);
        tvCredits.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        tvCredits.setLayoutParams(lp);

        TextView tvFee = new TextView(this);
        tvFee.setText(moneyFormat.format(fee) + " VNĐ");
        tvFee.setGravity(Gravity.CENTER);
        tvFee.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        tvFee.setLayoutParams(lp);

        row.addView(tvCode);
        row.addView(tvName);
        row.addView(tvCredits);
        row.addView(tvFee);

        int lastIndex = tlCourses.getChildCount() - 1;
        if (lastIndex >= 1) {
            tlCourses.addView(row, lastIndex);
        } else {
            tlCourses.addView(row);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // ----- Load danh sách môn học -----
    private void loadCourses(String studentId) {
        if (studentId == null || studentId.isEmpty()) return;

        apiService.getCourses(studentId).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courses.clear();
                    courses.addAll(response.body());

                    // Xóa các hàng cũ (giữ header + footer)
                    int childCount = tlCourses.getChildCount();
                    if (childCount > 2) {
                        for (int i = childCount - 2; i >= 1; i--) {
                            tlCourses.removeViewAt(i);
                        }
                    }

                    totalFee = 0;
                    if (courses.isEmpty()) {
                        tvTotalFee.setText(moneyFormat.format(0) + " VNĐ");
                    } else {
                        for (Course c : courses) {
                            addCourseRow(c.getCourseId(), c.getName(), c.getCredits(), c.getFee());
                            totalFee += c.getFee();
                        }
                        tvTotalFee.setText(moneyFormat.format(totalFee) + " VNĐ");
                    }
                    Log.d(TAG, "Courses response: " + response.body().toString());
                    Log.d(TAG, "Total fee updated to: " + totalFee);
                } else {
                    Log.e(TAG, "Failed to load courses: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e(TAG, "Load courses error: " + t.getMessage(), t);
            }
        });
    }

    // ----- Load số dư -----
    private void loadBalance() {
        if (username == null || username.isEmpty()) return;

        apiService.getBalance(username).enqueue(new Callback<BalanceResponse>() {
            @Override
            public void onResponse(Call<BalanceResponse> call, Response<BalanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double balance = response.body().getBalance();
                    tvAccountBalance.setText("Số dư: " + moneyFormat.format(balance) + " VNĐ");
                } else {
                    tvAccountBalance.setText("Số dư: 0 VNĐ");
                    Toast.makeText(MainScreenActivity.this, "Không thể tải số dư", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BalanceResponse> call, Throwable t) {
                tvAccountBalance.setText("Số dư: 0 VNĐ");
                Toast.makeText(MainScreenActivity.this, "Lỗi kết nối khi tải số dư", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Load balance error: " + t.getMessage(), t);
            }
        });
    }

    // ----- Load thông tin sinh viên -----
    private void loadStudentInfo() {
        if (username == null || username.isEmpty()) return;

        apiService.getStudentInfo(username).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Student student = response.body();
                    etStudentId.setText(student.getStudentId());
                    etStudentName.setText(student.getName());
                    isSelf = true; // Đặt isSelf = true khi load thông tin bản thân
                    loadCourses(student.getStudentId());
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Log.e(TAG, "Load student info error: " + t.getMessage(), t);
            }
        });
    }

    // ----- Tìm sinh viên theo mã -----
    private void loadStudentInfoById(String studentId) {
        if (studentId == null || studentId.isEmpty()) return;

        apiService.searchStudent(studentId).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Student student = response.body();
                    etStudentId.setText(student.getStudentId());
                    etStudentName.setText(student.getName());
                    isSelf = student.getStudentId().equals(username); // Cập nhật isSelf dựa trên studentId
                    loadCourses(student.getStudentId());
                } else {
                    Toast.makeText(MainScreenActivity.this, "Không tìm thấy sinh viên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Toast.makeText(MainScreenActivity.this, "Lỗi khi tìm sinh viên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ----- Load lịch sử giao dịch vào TableLayout -----
    private void loadTransactions() {
        if (username == null || username.isEmpty()) return;

        apiService.getTransactions(username).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tlTransactions.removeViews(1, tlTransactions.getChildCount() - 1); // Xóa rows cũ, giữ header
                    for (Transaction t : response.body()) {
                        addTransactionRow(t);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Log.e(TAG, "Load transactions error: " + t.getMessage(), t);
            }
        });
    }

    // ----- Thêm row cho lịch sử giao dịch -----
    private void addTransactionRow(Transaction t) {
        TableRow row = new TableRow(this);

        TableRow.LayoutParams lp1 = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TableRow.LayoutParams lp2 = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TableRow.LayoutParams lp3 = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TableRow.LayoutParams lp4 = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TableRow.LayoutParams lp5 = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        TextView tvSender = new TextView(this);
        tvSender.setText(t.getSender() != null ? t.getSender() : "-");
        tvSender.setGravity(Gravity.CENTER);
        tvSender.setLayoutParams(lp1);

        TextView tvReceiver = new TextView(this);
        tvReceiver.setText(t.getReceiverStudentId() != null ? t.getReceiverStudentId() : "-");
        tvReceiver.setGravity(Gravity.CENTER);
        tvReceiver.setLayoutParams(lp2);

        TextView tvAmount = new TextView(this);
        tvAmount.setText(moneyFormat.format(t.getAmount()) + " VNĐ");
        tvAmount.setGravity(Gravity.CENTER);
        tvAmount.setLayoutParams(lp3);

        TextView tvDatetime = new TextView(this);
        tvDatetime.setText(t.getDatetime() != null ? t.getDatetime() : "-");
        tvDatetime.setGravity(Gravity.CENTER);
        tvDatetime.setLayoutParams(lp4);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(t.getStatus() != null ? t.getStatus() : "-");
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setLayoutParams(lp5);

        row.addView(tvSender);
        row.addView(tvReceiver);
        row.addView(tvAmount);
        row.addView(tvDatetime);
        row.addView(tvStatus);

        tlTransactions.addView(row);
    }

    // ----- Navigation Drawer -----
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
        if (drawerLayout != null) drawerLayout.closeDrawers();
        return true;
    }
}