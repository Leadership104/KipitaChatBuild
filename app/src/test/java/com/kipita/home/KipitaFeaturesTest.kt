package com.kipita.home

import com.google.common.truth.Truth.assertThat
import com.kipita.presentation.home.KipitaFeatures
import org.junit.Test

class KipitaFeaturesTest {
    @Test
    fun `official components are present`() {
        val titles = KipitaFeatures.officialComponents.map { it.title }
        assertThat(titles).contains("Smart Navigation")
        assertThat(titles).contains("Trip Planning")
        assertThat(titles).contains("Safety First")
        assertThat(titles).contains("Travel Community")
    }
}
