package com.kipita.presentation.places

import com.google.common.truth.Truth.assertThat
import java.time.LocalTime
import org.junit.Test

class PlacesTabTimeOrganizerTest {

    @Test
    fun `morning prioritizes restaurants then transportation`() {
        val source = listOf(
            "Safety", "Shopping", "Transportation", "Restaurants", "Entertainment", "Services"
        )

        val sorted = sortSectionsForCurrentTime(source, { it }, LocalTime.of(8, 0))

        assertThat(sorted.take(3)).containsExactly(
            "Restaurants", "Transportation", "Services"
        ).inOrder()
    }

    @Test
    fun `evening prioritizes shopping then entertainment`() {
        val source = listOf(
            "Safety", "Transportation", "Restaurants", "Entertainment", "Shopping", "Services"
        )

        val sorted = sortSectionsForCurrentTime(source, { it }, LocalTime.of(17, 0))

        assertThat(sorted.take(3)).containsExactly(
            "Shopping", "Entertainment", "Restaurants"
        ).inOrder()
    }

    @Test
    fun `night prioritizes safety first`() {
        val source = listOf(
            "Transportation", "Restaurants", "Entertainment", "Shopping", "Services", "Safety"
        )

        val sorted = sortSectionsForCurrentTime(source, { it }, LocalTime.of(22, 0))

        assertThat(sorted.first()).isEqualTo("Safety")
    }
}
