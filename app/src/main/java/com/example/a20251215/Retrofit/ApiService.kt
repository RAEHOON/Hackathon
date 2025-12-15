package com.example.a20251215.Retrofit

import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Sign.SignupResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("signup.php")
    fun signup(
        @Field("username") username: String,
        @Field("nickname") nickname: String,
        @Field("loginid") loginid: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<SignupResponse>

    @FormUrlEncoded
    @POST("send_code.php")
    fun sendEmailCode(
        @Field("email") email: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("check_code.php")
    fun checkEmailCode(
        @Field("email") email: String,
        @Field("code") code: String
    ): Call<ApiResponse>



}