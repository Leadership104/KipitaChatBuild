package com.kipita.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OfflineMapRepository {
    private val mutex = Mutex()
    private val cachedRegions = mutableSetOf<String>()

    suspend fun cacheRegion(region: String) {
        mutex.withLock { cachedRegions.add(region.lowercase()) }
    }

    suspend fun isRegionAvailableOffline(region: String): Boolean =
        mutex.withLock { cachedRegions.contains(region.lowercase()) }
}
