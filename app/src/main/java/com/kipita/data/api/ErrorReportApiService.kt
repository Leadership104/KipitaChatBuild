package com.kipita.data.api

import retrofit2.http.Body
import retrofit2.http.POST

data class ErrorReportRequest(
    val email: String,
    val tag: String,
    val message: String,
    val stackTrace: String,
    val createdAtEpochMillis: Long
)

interface ErrorReportApiService {
    @POST("v1/error/report")
    suspend fun sendErrorReport(@Body request: ErrorReportRequest)
}
