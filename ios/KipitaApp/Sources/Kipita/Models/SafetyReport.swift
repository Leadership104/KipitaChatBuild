import Foundation

struct RealTimeSafetyReport {
    let country: String
    let safetyLevel: Int          // 1 (low risk) – 4 (critical)
    let safetyLevelLabel: String
    let aiInsight: String
    let weatherLine: String
    let advisorySections: [DwaatSection]
    let restrictions: [String]
    let generatedAt: Date
}

struct DwaatSection: Identifiable {
    let id: String
    let title: String
    let body: String
    let severity: String?
}
