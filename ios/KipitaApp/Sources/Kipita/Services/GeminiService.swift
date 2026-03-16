import Foundation

// Gemini 2.0 Flash-Lite via REST API
// Docs: https://ai.google.dev/api/generate-content
final class GeminiService {
    private let apiKey: String = {
        // Load from Config.xcconfig → Info.plist key GEMINI_API_KEY
        Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""
    }()
    private let model = "gemini-2.0-flash-lite"
    private let baseURL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private let systemPrompt = """
    You are Kipita, a concise travel AI assistant for digital nomads and adventurers.
    Focus on safety, cost, connectivity, and local experiences.
    Keep responses mobile-friendly: bullet points, short paragraphs, under 300 words.
    """

    func chat(_ message: String, history: [(String, String)] = []) async throws -> String {
        let url = URL(string: "\(baseURL)\(model):generateContent?key=\(apiKey)")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        var contents: [[String: Any]] = []
        for (role, text) in history {
            contents.append(["role": role, "parts": [["text": text]]])
        }
        contents.append(["role": "user", "parts": [["text": message]]])

        let body: [String: Any] = [
            "system_instruction": ["parts": [["text": systemPrompt]]],
            "contents": contents,
            "generationConfig": [
                "maxOutputTokens": 512,
                "temperature": 0.7
            ]
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        let candidates = json?["candidates"] as? [[String: Any]]
        let content = candidates?.first?["content"] as? [String: Any]
        let parts = content?["parts"] as? [[String: Any]]
        return parts?.first?["text"] as? String ?? "No response"
    }

    func analyzeSafetyContext(_ contextBlock: String, country: String) async throws -> String {
        let prompt = """
        Based on the following travel advisory data for \(country), provide a 3-sentence safety
        briefing for a traveler. Be factual and concise.

        \(contextBlock)
        """
        return try await chat(prompt)
    }
}
