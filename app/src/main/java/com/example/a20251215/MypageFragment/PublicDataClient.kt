package com.example.a20251215.MypageFragment

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object PublicDataClient {

    // 공공데이터(특일정보) 서비스 기본 주소
    private const val BASE_URL =
        "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/"

    val api: HolidayApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(HolidayApi::class.java)
    }
}