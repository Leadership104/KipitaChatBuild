package com.kipita.data.repository

import com.kipita.BuildConfig
import com.kipita.data.api.AffiliatesRequest
import com.kipita.data.api.DwaatApiService
import com.kipita.data.api.PerkItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerksRepository @Inject constructor(
    private val dwaatApiService: DwaatApiService
) {
    /** Static travel booking links from old Kipita planner. */
    val staticPlannerDeals = listOf(
        PerkItem(id = -1, name = "Flights & Hotels", description = "Search and book flights and hotels at the best prices", link = "https://savetraveldeals.com/book-hotels-app/", image = "✈️"),
        PerkItem(id = -2, name = "Car Rental", description = "Rent a car anywhere in the world with ease", link = "https://savetraveldeals.com/car-rental-app/", image = "🚗"),
        PerkItem(id = -3, name = "Airport Transfers", description = "Book reliable airport transfer services", link = "https://savetraveldeals.com/airport-transfers/", image = "🚐"),
        PerkItem(id = -4, name = "Bike Rental", description = "Explore cities on two wheels with bike rentals", link = "https://savetraveldeals.com/bike-rental-app/", image = "🚲")
    )

    /** Dynamic affiliate perks loaded from the Dwaat backend. */
    suspend fun getDynamicPerks(): Result<List<PerkItem>> = runCatching {
        val primary = runCatching {
            dwaatApiService.getAffiliates(AffiliatesRequest()).data ?: emptyList()
        }.getOrNull().orEmpty()
        if (primary.isNotEmpty()) return@runCatching primary

        // Fallback probes for host/path variants when the primary Dwaat route has TLS/path issues.
        val candidates = listOf(
            BuildConfig.DWAAT_BASE_URL.trimEnd('/') + "/affiliates.php",
            BuildConfig.DWAAT_FALLBACK_BASE_URL.trimEnd('/') + "/affiliates.php"
        ).distinct()

        candidates.forEach { url ->
            val items = runCatching {
                dwaatApiService.getAffiliatesAt(url, AffiliatesRequest()).data ?: emptyList()
            }.getOrNull().orEmpty()
            if (items.isNotEmpty()) return@runCatching items
        }

        emptyList()
    }
}
