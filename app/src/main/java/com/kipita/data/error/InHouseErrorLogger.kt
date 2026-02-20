package com.kipita.data.error

import com.kipita.data.api.ErrorReportApiService
import com.kipita.data.api.ErrorReportRequest
import com.kipita.data.local.ErrorLogDao
import com.kipita.data.local.ErrorLogEntity
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class InHouseErrorLogger(
    private val dao: ErrorLogDao,
    private val reportApiService: ErrorReportApiService
) {
    suspend fun log(tag: String, throwable: Throwable) {
        val entity = ErrorLogEntity(
            id = UUID.randomUUID().toString(),
            tag = tag,
            message = throwable.message ?: "Unknown error",
            stackTrace = throwable.stackTraceToString(),
            createdAtEpochMillis = Instant.now().toEpochMilli(),
            sent = false
        )
        dao.upsert(entity)
        flushUnsent()
    }

    suspend fun flushUnsent() = withContext(Dispatchers.IO) {
        val unsent = dao.getUnsent()
        if (unsent.isEmpty()) return@withContext
        val sentIds = coroutineScope {
            unsent.map { entry ->
                async {
                    runCatching {
                        reportApiService.sendErrorReport(
                            ErrorReportRequest(
                                email = "info@kipita.com",
                                tag = entry.tag,
                                message = entry.message,
                                stackTrace = entry.stackTrace,
                                createdAtEpochMillis = entry.createdAtEpochMillis
                            )
                        )
                    }.fold(onSuccess = { entry.id }, onFailure = { null })
                }
            }.awaitAll().filterNotNull()
        }
        if (sentIds.isNotEmpty()) dao.markSent(sentIds)
    }

    suspend fun allLogs(): List<ErrorLogEntity> = dao.getAll()
}
