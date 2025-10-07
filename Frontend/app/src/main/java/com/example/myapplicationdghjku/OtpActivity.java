package com.example.myapplicationdghjku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private TextInputEditText etDepositOtp;
    private Button btnConfirmOtp;
    private TextView resultTextView;
    private ApiService apiService;
    private String username; // Người thanh toán
    private String studentId; // Người nhận thanh toán
    private boolean isSelf; // Trạng thái thanh toán cho bản thân hay người khác
    private String baseUrl = "http://10.0.2.2:3000/"; // Thay bằng IP nếu test trên thiết bị thực

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        // Khởi tạo các thành phần giao diện
        etDepositOtp = findViewById(R.id.et_deposit_otp);
        btnConfirmOtp = findViewById(R.id.btn_confirm_otp);
        resultTextView = findViewById(R.id.resultTextView);
        apiService = RetrofitClient.getClient(baseUrl).create(ApiService.class);

        // Lấy username, studentId, và isSelf từ Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        studentId = intent.getStringExtra("studentId");
        isSelf = intent.getBooleanExtra("isSelf", true); // Mặc định true nếu không có

        if (username == null) {
            username = "testuser2"; // Giá trị mặc định nếu không nhận được
            Toast.makeText(this, "Lỗi: Không tìm thấy username, dùng mặc định testuser2", Toast.LENGTH_SHORT).show();
        }
        if (studentId == null) {
            studentId = username; // Mặc định thanh toán cho chính mình
        }

        // Xử lý sự kiện khi nhấn nút Xác Nhận
        btnConfirmOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etDepositOtp.getText().toString().trim();
                if (otp.isEmpty() || otp.length() != 6) {
                    resultTextView.setText("Vui lòng nhập OTP đúng 6 số!");
                    return;
                }

                // Gửi request xác thực OTP
                verifyOtp(otp);
            }
        });
    }

    private void verifyOtp(String otp) {
        VerifyOtpBody request = new VerifyOtpBody(username, otp, studentId, isSelf);
        Call<AuthResponse> call = apiService.verifyOtpAndPay(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        resultTextView.setText(authResponse.getMessage());
                        Toast.makeText(OtpActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        Intent successIntent = new Intent(OtpActivity.this, SuccessActivity.class);
                        successIntent.putExtra("username", username);
                        successIntent.putExtra("studentId", studentId);
                        startActivity(successIntent);
                        setResult(RESULT_OK); // Gửi kết quả về MainScreenActivity
                        finish(); // Đóng OtpActivity
                    } else {
                        resultTextView.setText("Lỗi: " + authResponse.getMessage());
                    }
                } else {
                    resultTextView.setText("Lỗi kết nối server!");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                resultTextView.setText("Lỗi: " + t.getMessage());
                Toast.makeText(OtpActivity.this, "Kết nối thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}