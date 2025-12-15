package com.example.a20251215

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("signup.php")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>
}