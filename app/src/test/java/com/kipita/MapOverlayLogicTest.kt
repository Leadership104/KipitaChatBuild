package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.presentation.map.BtcSourceFilter
import com.kipita.presentation.map.MapUiState
import com.kipita.presentation.map.MarkerType
import com.kipita.presentation.map.OverlayType
import org.junit.Test

class MapOverlayLogicTest {
    @Test
    fun `overlay defaults include merchant safety health and nomad`() {
        val state = MapUiState()
        assertThat(state.activeOverlays).contains(OverlayType.BTC_MERCHANTS)
        assertThat(state.activeOverlays).contains(OverlayType.SAFETY)
        assertThat(state.activeOverlays).contains(OverlayType.HEALTH)
        assertThat(state.activeOverlays).contains(OverlayType.NOMAD)
    }

    @Test
    fun `map defaults include btc source and marker type`() {
        val state = MapUiState()
        assertThat(state.btcSourceFilter).isEqualTo(BtcSourceFilter.BOTH)
        assertThat(state.selectedMarkerType).isEqualTo(MarkerType.FOOD)
    }
}
