package com.example.a20251215.holiday

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object KasiRetrofit {

    private val client = OkHttpClient.Builder().build()

    val api: KasiSpcdeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/") // 안 되면 http로 바꿔도 됨
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(KasiSpcdeApi::class.java)
    }
}
