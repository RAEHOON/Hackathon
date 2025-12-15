package com.example.a20251215

import retrofit2.Call
import retrofit2.http.Body
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


}