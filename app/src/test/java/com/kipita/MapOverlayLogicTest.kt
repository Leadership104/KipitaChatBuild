package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.presentation.map.MapUiState
import com.kipita.presentation.map.OverlayType
import org.junit.Test

class MapOverlayLogicTest {
    @Test
    fun `overlay defaults include safety and health`() {
        val state = MapUiState()
        assertThat(state.activeOverlays).contains(OverlayType.SAFETY)
        assertThat(state.activeOverlays).contains(OverlayType.HEALTH)
    }
}
