package com.kipita.presentation.advisory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.ai.RealTimeSafetyReport
import com.kipita.ai.SafetyAiEngine
import com.kipita.data.api.DwaatAdvisorySection
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.domain.model.NoticeCategory
import com.kipita.domain.model.SeverityLevel
import com.kipita.domain.model.TravelNotice
import com.kipita.domain.usecase.TravelDataEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdvisoryViewModel @Inject constructor(
    private val travelDataEngine: TravelDataEngine,
    private val safetyAiEngine: SafetyAiEngine,
    private val errorLogger: InHouseErrorLogger
) : ViewModel() {
    private val _state = MutableStateFlow(AdvisoryUiState())
    val state: StateFlow<AdvisoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun selectTab(tab: NoticeCategory) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    /** Basic load using existing TravelDataEngine (no GPS). */
    fun load(region: String = "global") {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            runCatching { travelDataEngine.collectRegionNotices(region) }
                .onSuccess { notices ->
                    _state.value = _state.value.copy(loading = false, notices = notices)
                }
                .onFailure {
                    errorLogger.log("AdvisoryViewModel.load", it)
                    _state.value = _state.value.copy(loading = false)
                }
        }
    }

    /**
     * Full real-time load: pulls Dwaat advisory/weather/restrictions + Gemini AI
     * synthesis in parallel with the existing domain-layer notices.
     */
    fun loadWithLocation(country: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, aiInsightLoading = true)
            // Kick off both in parallel
            val noticesDeferred = async {
                runCatching { travelDataEngine.collectRegionNotices(country) }.getOrElse { emptyList() }
            }
            val safetyDeferred = async {
                runCatching { safetyAiEngine.analyze(country, lat, lng) }.getOrNull()
            }

            val notices = noticesDeferred.await()
            val report = safetyDeferred.await()

            _state.value = _state.value.copy(
                loading = false,
                aiInsightLoading = false,
                notices = notices,
                safetyReport = report
            )
        }
    }

    /** Called when user taps refresh on the AI insight card. */
    fun refreshAiInsight(country: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(aiInsightLoading = true)
            runCatching { safetyAiEngine.analyze(country, lat, lng) }
                .onSuccess { report ->
                    _state.value = _state.value.copy(aiInsightLoading = false, safetyReport = report)
                }
                .onFailure {
                    errorLogger.log("AdvisoryViewModel.refreshAiInsight", it)
                    _state.value = _state.value.copy(aiInsightLoading = false)
                }
        }
    }
}

data class AdvisoryUiState(
    val loading: Boolean = false,
    val aiInsightLoading: Boolean = false,
    val selectedTab: NoticeCategory = NoticeCategory.ADVISORY,
    val notices: List<TravelNotice> = emptyList(),
    val safetyReport: RealTimeSafetyReport? = null
) {
    val tabbedNotices: List<TravelNotice>
        get() = notices.filter { it.category == selectedTab }

    val dwaatSections: List<DwaatAdvisorySection>
        get() = safetyReport?.advisorySections ?: emptyList()

    val aiInsight: String?
        get() = safetyReport?.aiInsight?.takeIf { it.isNotBlank() }

    val weatherLine: String
        get() {
            val r = safetyReport ?: return ""
            return buildString {
                if (r.weatherTemperature.isNotBlank()) append(r.weatherTemperature)
                if (r.weatherCondition.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(r.weatherCondition)
                }
            }
        }

    /** US State Dept–style level: 1=Normal, 2=Increased Caution, 3=Reconsider, 4=Do Not Travel */
    val safetyLevel: Int
        get() = when {
            safetyReport != null -> safetyReport.overallLevel
            notices.isEmpty() -> 2
            notices.any { it.severity == SeverityLevel.CRITICAL } -> 4
            notices.any { it.severity == SeverityLevel.HIGH } -> 3
            notices.any { it.severity == SeverityLevel.MEDIUM } -> 2
            else -> 1
        }

    val safetyLevelLabel: String
        get() = when (safetyLevel) {
            1 -> "Exercise normal\nprecautions"
            3 -> "Reconsider\ntravel"
            4 -> "Do not\ntravel"
            else -> "Exercise increased\ncaution"
        }
}
