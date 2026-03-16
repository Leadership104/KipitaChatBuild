import SwiftUI
import CoreLocation

@Observable
@MainActor
final class AdvisoryViewModel {
    var report: RealTimeSafetyReport?
    var isLoading = false
    var error: String?
    var country = "Global"

    private let dwaatService = DwaatAPIService()
    private let geminiService = GeminiService()
    private let locationManager = LocationManager.shared

    var safetyLevel: Int { report?.safetyLevel ?? 0 }
    var safetyLevelLabel: String { report?.safetyLevelLabel ?? "Unknown" }
    var aiInsight: String { report?.aiInsight ?? "" }
    var weatherLine: String { report?.weatherLine ?? "" }
    var advisorySections: [DwaatSection] { report?.advisorySections ?? [] }

    func loadWithLocation() async {
        isLoading = true
        if let loc = await locationManager.requestOnce() {
            let geocoder = CLGeocoder()
            if let placemark = try? await geocoder.reverseGeocodeLocation(
                CLLocation(latitude: loc.latitude, longitude: loc.longitude)
            ).first {
                country = placemark.country ?? "Global"
            }
            await fetchReport(country: country, lat: loc.latitude, lng: loc.longitude)
        } else {
            await fetchReport(country: "Global", lat: nil, lng: nil)
        }
        isLoading = false
    }

    func refresh() async {
        await loadWithLocation()
    }

    private func fetchReport(country: String, lat: Double?, lng: Double?) async {
        do {
            let sections = try await dwaatService.advisorySections(country: country)
            let weather = try? await dwaatService.weatherAdvisory(country: country)
            let restrictions = (try? await dwaatService.restrictions(country: country)) ?? []

            let contextBlock = buildContextBlock(country: country, sections: sections,
                                                 weather: weather, restrictions: restrictions)
            let aiText = try await geminiService.analyzeSafetyContext(contextBlock, country: country)

            let level = computeSafetyLevel(sections: sections, restrictions: restrictions)
            report = RealTimeSafetyReport(
                country: country,
                safetyLevel: level,
                safetyLevelLabel: safetyLevelLabel(for: level),
                aiInsight: aiText,
                weatherLine: weather ?? "",
                advisorySections: sections,
                restrictions: restrictions,
                generatedAt: .now
            )
        } catch {
            self.error = error.localizedDescription
        }
    }

    private func buildContextBlock(country: String, sections: [DwaatSection],
                                   weather: String?, restrictions: [String]) -> String {
        var block = "Country: \(country)\n"
        if let w = weather { block += "Weather: \(w)\n" }
        if !restrictions.isEmpty { block += "Restrictions: \(restrictions.joined(separator: ", "))\n" }
        block += sections.map { "[\($0.title)] \($0.body)" }.joined(separator: "\n")
        return block
    }

    private func computeSafetyLevel(sections: [DwaatSection], restrictions: [String]) -> Int {
        let critical = ["do not travel", "war", "armed conflict", "terrorism"]
        let high = ["exercise increased caution", "reconsider travel", "crime", "protests"]
        let text = sections.map { $0.body.lowercased() }.joined() + restrictions.joined().lowercased()
        if critical.contains(where: { text.contains($0) }) { return 4 }
        if high.contains(where: { text.contains($0) }) { return 3 }
        if sections.count > 2 { return 2 }
        return 1
    }

    private func safetyLevelLabel(for level: Int) -> String {
        switch level {
        case 1: "Low Risk"
        case 2: "Exercise Caution"
        case 3: "High Alert"
        case 4: "Critical — Avoid"
        default: "Unknown"
        }
    }
}
