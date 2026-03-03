package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.api.BtcMapElementDto
import com.kipita.data.api.BtcMapOsmJson
import com.kipita.data.api.BtcMerchantApiService
import com.kipita.data.api.CashAppMerchantDto
import com.kipita.data.local.MerchantDao
import com.kipita.data.local.MerchantEntity
import com.kipita.data.repository.MerchantRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MerchantRepositoryTest {
    @Test
    fun `refresh merges btcmap and cashapp`() = runTest {
        val api = mockk<BtcMerchantApiService>()
        val dao = mockk<MerchantDao>()
        val repo = MerchantRepository(api, dao)

        coEvery { api.getBtcMapMerchants() } returns listOf(
            BtcMapElementDto(
                id = "1",
                osmJson = BtcMapOsmJson(
                    lat = 1.0,
                    lon = 2.0,
                    tags = mapOf(
                        "name" to "Cafe",
                        "payment:lightning" to "yes",
                        "payment:bitcoin" to "yes"
                    )
                )
            )
        )
        coEvery { api.getCashAppMerchants(any()) } returns listOf(
            CashAppMerchantDto("2", "Store", 2.0, 3.0, true, 200)
        )
        coEvery { dao.upsertAll(any()) } returns Unit

        val merchants = repo.refresh("token")

        assertThat(merchants).hasSize(2)
        assertThat(merchants.any { it.acceptsLightning }).isTrue()
        assertThat(merchants.any { it.acceptsCashApp }).isTrue()
    }

    @Test
    fun `refresh falls back to cache`() = runTest {
        val api = mockk<BtcMerchantApiService>()
        val dao = mockk<MerchantDao>()
        val repo = MerchantRepository(api, dao)

        coEvery { api.getBtcMapMerchants() } throws RuntimeException("offline")
        coEvery { dao.getAll() } returns listOf(
            MerchantEntity("c", "Cached", 1.0, 1.0, true, false, false, "btcmap", 1L, "k=v")
        )

        val merchants = repo.refresh(null)

        assertThat(merchants).hasSize(1)
        assertThat(merchants.first().name).isEqualTo("Cached")
    }
}
