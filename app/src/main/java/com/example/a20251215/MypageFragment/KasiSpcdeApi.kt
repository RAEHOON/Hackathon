package com.example.a20251215.holiday

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KasiSpcdeApi {

    // 공휴일 정보 조회
    @GET("B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
    suspend fun getRestDeInfo(
        @Query(value = "ServiceKey", encoded = true) serviceKey: String,
        @Query("solYear") solYear: String,
        @Query("solMonth") solMonth: String,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1
    ): Response<String>
}
