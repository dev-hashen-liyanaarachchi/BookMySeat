package com.hash.bookmyseat.model;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/bookings/confirm")
    Call<ResponseBody> confirmBooking(@Body Map<String, Object> bookingData);


    @POST("api/auth/send-otp")
    Call<ResponseBody> sendOtp(@Body Map<String, Object> otpData);

    @POST("api/auth/verify-otp")
    Call<ResponseBody> verifyOtp(@Body Map<String, Object> verifyData);

    @POST("api/auth/register")
    Call<ResponseBody> registerUser(@Body Map<String, Object> userData);

    @POST("api/auth/login")
    Call<ResponseBody> loginUser(@Body Map<String, String> loginData);

    @POST("api/auth/resend-otp")
    Call<ResponseBody> resendOtp(@Body Map<String, String> resendData);

    @POST("api/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body Map<String, String> emailData);
}