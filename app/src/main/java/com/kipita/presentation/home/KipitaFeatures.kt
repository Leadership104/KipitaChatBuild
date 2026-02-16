package com.kipita.presentation.home

data class KipitaFeature(
    val title: String,
    val description: String
)

object KipitaFeatures {
    val officialComponents: List<KipitaFeature> = listOf(
        KipitaFeature(
            title = "Smart Navigation",
            description = "AI-powered route optimization and real-time traffic updates to get you there faster."
        ),
        KipitaFeature(
            title = "Trip Planning",
            description = "Intelligent itinerary creation with personalized recommendations based on your preferences."
        ),
        KipitaFeature(
            title = "Safety First",
            description = "Real-time safety alerts, emergency contacts, and secure location sharing for peace of mind."
        ),
        KipitaFeature(
            title = "Travel Community",
            description = "Connect with fellow travelers, share experiences, and discover hidden gems together."
        )
    )
}
