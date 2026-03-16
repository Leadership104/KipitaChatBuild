import Foundation

// Dwaat API — backend at https://api.dwaat.com/
// All endpoints use POST with an `action` field
final class DwaatAPIService {
    private let baseURL = URL(string: "https://api.dwaat.com/")!

    // MARK: - Auth
    func login(email: String, password: String) async throws -> DwaatAuthResponse {
        try await post(action: "Login", body: ["email": email, "password": password])
    }

    func register(email: String, password: String, name: String) async throws -> DwaatAuthResponse {
        try await post(action: "Register", body: ["email": email, "password": password, "name": name])
    }

    func socialLogin(provider: String, token: String) async throws -> DwaatAuthResponse {
        try await post(action: "socialLogin", body: ["provider": provider, "token": token])
    }

    func deleteAccount(token: String) async throws -> DwaatStatusResponse {
        try await post(action: "deleteAccount", body: ["token": token])
    }

    // MARK: - Advisories
    func advisorySections(country: String) async throws -> [DwaatSection] {
        let resp: DwaatResponse<[DwaatSectionDTO]> = try await post(
            action: "advisorySections", body: ["country": country]
        )
        return resp.data?.map { DwaatSection(id: $0.id, title: $0.title, body: $0.body, severity: $0.severity) } ?? []
    }

    func weatherAdvisory(country: String) async throws -> String? {
        let resp: DwaatResponse<DwaatWeatherDTO> = try await post(
            action: "getWeatherAdvisory", body: ["country": country]
        )
        return resp.data?.summary
    }

    func restrictions(country: String) async throws -> [String] {
        let resp: DwaatResponse<[String]> = try await post(
            action: "restrictions", body: ["country": country]
        )
        return resp.data ?? []
    }

    // MARK: - Preferences
    func getPreferences(token: String) async throws -> DwaatPreferences {
        try await post(action: "getPreferences", body: ["token": token])
    }

    func savePreferences(_ prefs: DwaatPreferences, token: String) async throws -> DwaatStatusResponse {
        try await post(action: "savePreferences", body: ["token": token, "preferences": prefs])
    }

    // MARK: - Airport Search
    func searchAirport(query: String) async throws -> [DwaatAirport] {
        let resp: DwaatResponse<[DwaatAirport]> = try await post(
            action: "airportSearch", body: ["query": query]
        )
        return resp.data ?? []
    }

    // MARK: - Bookmarks
    func listBookmarks(token: String) async throws -> [DwaatBookmark] {
        let resp: DwaatResponse<[DwaatBookmark]> = try await post(
            action: "listBookmarks", body: ["token": token]
        )
        return resp.data ?? []
    }

    func addBookmark(placeId: String, token: String) async throws -> DwaatStatusResponse {
        try await post(action: "addBookmark", body: ["place_id": placeId, "token": token])
    }

    // MARK: - Core POST helper
    private func post<T: Decodable>(action: String, body: [String: Any]) async throws -> T {
        var request = URLRequest(url: baseURL)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        var payload = body
        payload["action"] = action
        request.httpBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode(T.self, from: data)
    }
}

// MARK: - Response DTOs
struct DwaatResponse<T: Decodable>: Decodable {
    let status: String
    let message: String?
    let data: T?
}

struct DwaatAuthResponse: Decodable {
    let status: String
    let token: String?
    let userId: String?
}

struct DwaatStatusResponse: Decodable {
    let status: String
    let message: String?
}

struct DwaatSectionDTO: Decodable {
    let id: String
    let title: String
    let body: String
    let severity: String?
}

struct DwaatWeatherDTO: Decodable {
    let summary: String?
    let temperature: String?
    let conditions: String?
}

struct DwaatPreferences: Codable {
    var currency: String?
    var language: String?
    var notificationsEnabled: Bool?
}

struct DwaatAirport: Decodable, Identifiable {
    let id: String
    let name: String
    let iata: String
    let country: String
    let lat: Double
    let lng: Double
}

struct DwaatBookmark: Decodable, Identifiable {
    let id: String
    let placeId: String
    let name: String?
    let createdAt: String?
}
