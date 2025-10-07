package com.example.myapplicationdghjku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Button btnBackHome = findViewById(R.id.btn_back_home);
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK); // Gửi kết quả về
                finish(); // Quay về activity trước đó (OtpActivity hoặc MainScreenActivity)
            }
        });
    }
}