import Foundation
import SwiftData

enum TripStatus: String, Codable {
    case upcoming = "UPCOMING"
    case active = "ACTIVE"
    case past = "PAST"
    case cancelled = "CANCELLED"
}

@Model
final class TripModel {
    @Attribute(.unique) var id: String
    var destination: String
    var country: String
    var startDate: Date
    var endDate: Date
    var notes: String
    var status: TripStatus
    var isSample: Bool
    var createdAt: Date
    var cancelledAt: Date?
    var cancellationReason: String?

    init(
        id: String = UUID().uuidString,
        destination: String,
        country: String,
        startDate: Date,
        endDate: Date,
        notes: String = "",
        status: TripStatus = .upcoming,
        isSample: Bool = false,
        createdAt: Date = .now,
        cancelledAt: Date? = nil,
        cancellationReason: String? = nil
    ) {
        self.id = id
        self.destination = destination
        self.country = country
        self.startDate = startDate
        self.endDate = endDate
        self.notes = notes
        self.status = status
        self.isSample = isSample
        self.createdAt = createdAt
        self.cancelledAt = cancelledAt
        self.cancellationReason = cancellationReason
    }
}
