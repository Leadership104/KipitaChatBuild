package com.kipita.domain.usecase

import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.TripChatRepository
import com.kipita.domain.model.LlmPrompt
import com.kipita.domain.model.LlmProvider

data class MapAction(
    val centerLat: Double,
    val centerLon: Double,
    val zoom: Float,
    val highlightMerchantIds: List<String>
)

data class OrchestratedAssistantResponse(
    val naturalLanguage: String,
    val modelUsed: LlmProvider,
    val citations: List<String>,
    val mapAction: MapAction?
)

class AiOrchestrator(
    private val travelDataEngine: TravelDataEngine,
    private val llmRouter: LlmRouter,
    private val merchantRepository: MerchantRepository,
    private val nomadRepository: NomadRepository,
    private val tripChatRepository: TripChatRepository
) {
    suspend fun handleIntent(query: String, region: String): OrchestratedAssistantResponse {
        val notices = travelDataEngine.collectRegionNotices(region)
        val merchants = merchantRepository.getCachedMerchants()
        val nomad = nomadRepository.refresh().take(3)
        val intentWantsMerchants = query.contains("merchant", ignoreCase = true) || query.contains("bitcoin", ignoreCase = true)

        val prompt = buildString {
            appendLine("User query: $query")
            appendLine("Region: $region")
            appendLine("Travel notices count: ${notices.size}")
            appendLine("Merchants count: ${merchants.size}")
            appendLine("Nomad snapshots: ${nomad.joinToString { "${it.city} safety=${it.safetyScore} internet=${it.internetMbps}" }}")
            appendLine("Return concise safe-travel guidance with confidence and itinerary hints.")
        }

        val candidates = llmRouter.askAll(prompt)
        val best = candidates.maxByOrNull { it.confidence } ?: llmRouter.ask(LlmPrompt(LlmProvider.OPENAI, prompt))

        val action = if (intentWantsMerchants && merchants.isNotEmpty()) {
            val anchor = merchants.first()
            MapAction(anchor.latitude, anchor.longitude, 13f, merchants.take(8).map { it.id })
        } else null

        return OrchestratedAssistantResponse(
            naturalLanguage = best.content,
            modelUsed = best.provider,
            citations = notices.take(5).map { "${it.sourceName} @ ${it.lastUpdated}" } + "Model: ${best.model}",
            mapAction = action
        )
    }

    suspend fun assistTripChat(tripId: String, participantIds: List<String>, userPrompt: String): String {
        tripChatRepository.enforceParticipantLimit(participantIds)
        val contextMessages = tripChatRepository.getMessages(tripId).takeLast(10)
        val prompt = buildString {
            appendLine("You are the single trip planning AI for a group trip chat.")
            appendLine("Participants: ${participantIds.joinToString()}")
            appendLine("Recent chat: ${contextMessages.joinToString { "${it.senderName}: ${it.content}" }}")
            appendLine("User request: $userPrompt")
            appendLine("Return itinerary bullets with timing and contingency tips.")
        }
        val response = llmRouter.ask(LlmPrompt(LlmProvider.OPENAI, prompt, structured = true))
        tripChatRepository.sendMessage(tripId, "ai", "Kipita AI Planner", response.content, isAi = true)
        return response.content
    }
}
