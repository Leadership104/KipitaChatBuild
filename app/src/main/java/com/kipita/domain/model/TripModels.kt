package com.kipita.domain.model

import java.time.LocalDate

data class Trip(
    val id: String,
    val title: String,
    val destination: String,
    val country: String,
    val countryFlag: String,
    val coverImageUrl: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val weatherHighC: Int,
    val weatherLowC: Int,
    val weatherIcon: String,
    val isUpcoming: Boolean
) {
    val daysUntil: Int get() {
        val today = LocalDate.now()
        return if (startDate.isAfter(today)) {
            java.time.temporal.ChronoUnit.DAYS.between(today, startDate).toInt()
        } else 0
    }
    val durationDays: Int get() =
        java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
}

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val homeCity: String = "",
    val travelStyle: List<String> = emptyList(),
    val isGroup: Boolean = false,
    val groupName: String = "",
    val groupMemberCount: Int = 0,
    val tripsCount: Int = 0,
    val countriesVisited: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

data class ExploreDestination(
    val id: String,
    val rank: Int,
    val city: String,
    val country: String,
    val coverImageUrl: String,
    val costPerMonthUsd: Int,
    val wifiSpeedMbps: Int,
    val weatherSummary: String,
    val weatherIcon: String,
    val isPopular: Boolean,
    val safetyScore: Double,
    val tags: List<String>
)

data class CommunityGroup(
    val id: String,
    val name: String,
    val location: String,
    val memberCount: Int,
    val unreadCount: Int,
    val lastMessage: String,
    val avatarEmoji: String
)

data class NearbyTraveler(
    val id: String,
    val name: String,
    val currentCity: String,
    val avatarUrl: String,
    val mutualGroups: Int,
    val travelStyle: String
)

// Singleton sample data for UI while APIs are being wired
object SampleData {
    val upcomingTrips = listOf(
        Trip(
            id = "1", title = "Tokyo Adventure", destination = "Tokyo", country = "Japan",
            countryFlag = "🇯🇵", coverImageUrl = "",
            startDate = LocalDate.now().plusDays(12), endDate = LocalDate.now().plusDays(22),
            weatherHighC = 24, weatherLowC = 16, weatherIcon = "⛅", isUpcoming = true
        ),
        Trip(
            id = "2", title = "Bali Escape", destination = "Bali", country = "Indonesia",
            countryFlag = "🇮🇩", coverImageUrl = "",
            startDate = LocalDate.now().plusDays(45), endDate = LocalDate.now().plusDays(55),
            weatherHighC = 31, weatherLowC = 24, weatherIcon = "☀️", isUpcoming = true
        )
    )

    val pastTrips = listOf(
        Trip(
            id = "3", title = "Paris Weekend", destination = "Paris", country = "France",
            countryFlag = "🇫🇷", coverImageUrl = "",
            startDate = LocalDate.now().minusDays(60), endDate = LocalDate.now().minusDays(56),
            weatherHighC = 19, weatherLowC = 12, weatherIcon = "🌤", isUpcoming = false
        ),
        Trip(
            id = "4", title = "NYC Work Trip", destination = "New York", country = "USA",
            countryFlag = "🇺🇸", coverImageUrl = "",
            startDate = LocalDate.now().minusDays(90), endDate = LocalDate.now().minusDays(86),
            weatherHighC = 22, weatherLowC = 15, weatherIcon = "⛅", isUpcoming = false
        )
    )

    val destinations = listOf(
        ExploreDestination(
            id = "1", rank = 1, city = "Chiang Mai", country = "Thailand",
            coverImageUrl = "", costPerMonthUsd = 1200, wifiSpeedMbps = 52,
            weatherSummary = "Warm & Sunny · 28°C", weatherIcon = "☀️", isPopular = true,
            safetyScore = 8.2, tags = listOf("Affordable", "Digital Nomad")
        ),
        ExploreDestination(
            id = "2", rank = 2, city = "Lisbon", country = "Portugal",
            coverImageUrl = "", costPerMonthUsd = 1605, wifiSpeedMbps = 48,
            weatherSummary = "Mild & Breezy · 18°C", weatherIcon = "🌤", isPopular = true,
            safetyScore = 8.7, tags = listOf("Europe", "Food Scene")
        ),
        ExploreDestination(
            id = "3", rank = 3, city = "Medellín", country = "Colombia",
            coverImageUrl = "", costPerMonthUsd = 980, wifiSpeedMbps = 35,
            weatherSummary = "Spring Year-Round · 22°C", weatherIcon = "⛅", isPopular = false,
            safetyScore = 7.1, tags = listOf("Affordable", "Culture")
        ),
        ExploreDestination(
            id = "4", rank = 4, city = "Tallinn", country = "Estonia",
            coverImageUrl = "", costPerMonthUsd = 1450, wifiSpeedMbps = 61,
            weatherSummary = "Cool & Clear · 4°C", weatherIcon = "🌥", isPopular = false,
            safetyScore = 9.1, tags = listOf("Tech Hub", "EU")
        ),
        ExploreDestination(
            id = "5", rank = 5, city = "Bali", country = "Indonesia",
            coverImageUrl = "", costPerMonthUsd = 1100, wifiSpeedMbps = 28,
            weatherSummary = "Tropical · 30°C", weatherIcon = "🌴", isPopular = true,
            safetyScore = 7.8, tags = listOf("Beach", "Wellness")
        ),
        ExploreDestination(
            id = "6", rank = 6, city = "Bangkok", country = "Thailand",
            coverImageUrl = "", costPerMonthUsd = 1350, wifiSpeedMbps = 44,
            weatherSummary = "Hot & Humid · 33°C", weatherIcon = "☀️", isPopular = true,
            safetyScore = 7.5, tags = listOf("Street Food", "Culture")
        ),
        ExploreDestination(
            id = "7", rank = 7, city = "Barcelona", country = "Spain",
            coverImageUrl = "", costPerMonthUsd = 1800, wifiSpeedMbps = 55,
            weatherSummary = "Warm & Mediterranean · 20°C", weatherIcon = "🌤", isPopular = true,
            safetyScore = 8.0, tags = listOf("Beach", "Europe")
        ),
        ExploreDestination(
            id = "8", rank = 8, city = "Mexico City", country = "Mexico",
            coverImageUrl = "", costPerMonthUsd = 1150, wifiSpeedMbps = 38,
            weatherSummary = "Mild & Sunny · 24°C", weatherIcon = "⛅", isPopular = false,
            safetyScore = 6.8, tags = listOf("Culture", "Food")
        )
    )

    val communityGroups = listOf(
        CommunityGroup("g1", "Tokyo Nomads", "Tokyo, Japan", 48, 3, "Anyone know good coworking spots near Shibuya?", "🗼"),
        CommunityGroup("g2", "Lisbon Digital", "Lisbon, Portugal", 126, 12, "Sunset boat trip this Friday — who's in?", "🏖"),
        CommunityGroup("g3", "Crypto Travelers", "Global", 312, 0, "New BTCMap merchants in SE Asia listing", "₿"),
        CommunityGroup("g4", "Solo Female Travelers", "Global", 891, 7, "Safety tips for solo night travel in Asia", "✈️"),
        CommunityGroup("g5", "Bali Nomads", "Bali, Indonesia", 234, 5, "Best beach clubs for working with WiFi 🌴", "🏄"),
        CommunityGroup("g6", "Budget Backpackers", "SE Asia", 512, 2, "Found $8/night gem in Chiang Mai!", "🎒")
    )

    val nearbyTravelers = listOf(
        NearbyTraveler("t1", "Alex Chen", "Lisbon, Portugal", "", 2, "Budget Explorer"),
        NearbyTraveler("t2", "Sofia Martins", "Lisbon, Portugal", "", 1, "Luxury Nomad"),
        NearbyTraveler("t3", "James Park", "Porto, Portugal", "", 3, "Adventure Seeker"),
        NearbyTraveler("t4", "Maria Santos", "Barcelona, Spain", "", 1, "Culture Lover"),
        NearbyTraveler("t5", "Kenji Tanaka", "Tokyo, Japan", "", 4, "Tech Nomad")
    )
}
