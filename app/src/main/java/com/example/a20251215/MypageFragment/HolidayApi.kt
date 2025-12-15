package com.example.a20251215.MypageFragment

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface HolidayApi {

    // baseUrl 뒤에 getRestDeInfo 붙는 구조
    @GET("getRestDeInfo")
    suspend fun getRestDeInfoRaw(
        @QueryMap params: Map<String, String>,
        @Query("_type") type: String = "json"
    ): String
}