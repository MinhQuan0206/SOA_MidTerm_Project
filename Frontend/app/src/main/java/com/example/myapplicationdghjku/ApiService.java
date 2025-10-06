package com.example.myapplicationdghjku;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
public interface ApiService {

    @POST("register")
    Call<AuthResponse> register(@Body UserRequest userRequest);

    @POST("login")
    Call<AuthResponse> login(@Body UserRequest userRequest);
    @GET("student/info")
    Call<Student> getStudentInfo(@Query("username") String username);

    // Lấy danh sách môn học
    @GET("student/courses")
    Call<List<Course>> getCourses(@Query("student_id") String studentId);

    // Lấy số dư tài khoản
    @GET("payment/getBalance")
    Call<BalanceResponse> getBalance(@Query("username") String username);

    // Tìm sinh viên khác
    @GET("student/search")
    Call<Student> searchStudent(@Query("student_id") String studentId);

    // Lấy lịch sử giao dịch
    @GET("transactions")
    Call<List<Transaction>> getTransactions(@Query("username") String username);

    // Gửi yêu cầu thanh toán (tạo OTP)
    // Gửi yêu cầu thanh toán (tạo OTP)

    @POST("payment/requestPayTuition")
    Call<AuthResponse> requestPayTuition(@Body RequestPayTuitionBody body);


    @POST("payment/verifyOtpAndPay")
    Call<AuthResponse> verifyOtpAndPay(@Body VerifyOtpBody body);





}
