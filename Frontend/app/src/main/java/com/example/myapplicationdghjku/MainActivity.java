package com.example.myapplicationdghjku;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private static final String BASE_URL = "http://10.0.2.2:3000/"; // Phù hợp với emulator
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = RetrofitClient.getClient(BASE_URL).create(ApiService.class);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập tên và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRequest req = new UserRequest(username, password);
            apiService.login(req).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AuthResponse authResponse = response.body();
                        if (authResponse.isSuccess()) {
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                            String sessionId = authResponse.getSessionId();
                            if (sessionId != null) {
                                prefs.edit().putString("username", username).apply();
                                prefs.edit().putString("sessionId", sessionId).apply();
                                Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("sessionId", sessionId);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Lỗi: Không nhận được sessionId", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại: " + authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Đăng nhập thất bại (response error)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}