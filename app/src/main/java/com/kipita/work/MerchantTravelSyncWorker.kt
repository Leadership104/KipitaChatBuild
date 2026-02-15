package com.kipita.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.OfflineMapRepository
import com.kipita.domain.usecase.TravelDataEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MerchantTravelSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val merchantRepository: MerchantRepository,
    private val nomadRepository: NomadRepository,
    private val offlineMapRepository: OfflineMapRepository,
    private val travelDataEngine: TravelDataEngine
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return runCatching {
            merchantRepository.refresh(cashAppToken = null)
            nomadRepository.refresh()
            offlineMapRepository.cacheRegion("global")
            travelDataEngine.collectRegionNotices("global")
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
