import SwiftUI

struct ChatMessage: Identifiable {
    let id = UUID()
    let role: Role
    let text: String
    let timestamp: Date

    enum Role { case user, assistant }
}

@Observable
@MainActor
final class AIViewModel {
    var messages: [ChatMessage] = []
    var inputText = ""
    var isTyping = false
    var lastPlanDestination: String?
    var lastPlanDays: Int?

    private let geminiService = GeminiService()

    func sendMessage(_ text: String) async {
        guard !text.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        messages.append(ChatMessage(role: .user, text: text, timestamp: .now))
        inputText = ""
        isTyping = true
        do {
            let history = messages.dropLast().map { ($0.role == .user ? "user" : "model", $0.text) }
            let reply = try await geminiService.chat(text, history: history)
            messages.append(ChatMessage(role: .assistant, text: reply, timestamp: .now))
            extractPlanInfo(from: text, reply: reply)
        } catch {
            messages.append(ChatMessage(role: .assistant,
                                        text: "Sorry, I couldn't process that. Please try again.",
                                        timestamp: .now))
        }
        isTyping = false
    }

    func analyzeSafety(country: String = "global", lat: Double? = nil, lng: Double? = nil) async {
        let prompt = "Give me a concise travel safety briefing for \(country). Include crime, health, local laws, and any active advisories."
        await sendMessage(prompt)
    }

    func quickAction(_ prompt: String) async {
        await sendMessage(prompt)
    }

    func clearLastPlan() {
        lastPlanDestination = nil
        lastPlanDays = nil
    }

    private func extractPlanInfo(from input: String, reply: String) {
        // Simple heuristic: detect destination + days from AI reply
        let lower = reply.lowercased()
        if lower.contains("day") || lower.contains("itinerary") || lower.contains("trip to") {
            let words = input.components(separatedBy: " ")
            if let dayIdx = words.firstIndex(where: { $0.contains("day") }),
               dayIdx > 0, let days = Int(words[dayIdx - 1]) {
                lastPlanDays = days
            }
            // Destination heuristic: look for "trip to X" or "visit X"
            for keyword in ["to ", "visit ", "in ", "exploring "] {
                if let range = lower.range(of: keyword) {
                    let after = String(lower[range.upperBound...]).components(separatedBy: " ").first ?? ""
                    if after.count > 2 { lastPlanDestination = after.capitalized; break }
                }
            }
        }
    }
}
