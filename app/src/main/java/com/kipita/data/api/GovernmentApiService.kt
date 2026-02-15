package com.kipita.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GovernmentApiService {
    @GET("v1/safety/notices")
    suspend fun getSafetyNotices(@Query("region") region: String): List<TravelNoticeDto>

    @GET("v1/health/notices")
    suspend fun getHealthNotices(@Query("region") region: String): List<TravelNoticeDto>

    @GET("v1/advisories")
    suspend fun getAdvisories(@Query("region") region: String): List<TravelNoticeDto>

    @GET("v1/infrastructure")
    suspend fun getInfrastructure(@Query("region") region: String): List<TravelNoticeDto>
}
