package com.kipita.presentation.places

import java.time.LocalTime

private enum class DayPhase {
    MORNING,
    EVENING,
    NIGHT
}

internal fun <T> sortSectionsForCurrentTime(
    sections: List<T>,
    labelOf: (T) -> String,
    now: LocalTime = LocalTime.now()
): List<T> {
    val phase = when (now.hour) {
        in 5..11 -> DayPhase.MORNING
        in 12..18 -> DayPhase.EVENING
        else -> DayPhase.NIGHT
    }

    val priority = when (phase) {
        DayPhase.MORNING -> listOf(
            "Restaurants", "Transportation", "Services", "Shopping", "Entertainment", "Safety", "Destinations"
        )

        DayPhase.EVENING -> listOf(
            "Shopping", "Entertainment", "Restaurants", "Transportation", "Services", "Safety", "Destinations"
        )

        DayPhase.NIGHT -> listOf(
            "Safety", "Entertainment", "Restaurants", "Transportation", "Services", "Shopping", "Destinations"
        )
    }

    return sections.sortedBy { section ->
        val idx = priority.indexOf(labelOf(section))
        if (idx >= 0) idx else Int.MAX_VALUE
    }
}
