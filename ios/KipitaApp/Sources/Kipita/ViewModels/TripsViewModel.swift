import SwiftUI
import SwiftData

@Observable
@MainActor
final class TripsViewModel {
    var upcomingTrips: [TripModel] = []
    var cancelledTrips: [TripModel] = []
    var isLoading = false
    var error: String?

    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func load() {
        let descriptor = FetchDescriptor<TripModel>(
            predicate: #Predicate { $0.status != .cancelled },
            sortBy: [SortDescriptor(\.startDate)]
        )
        upcomingTrips = (try? modelContext.fetch(descriptor)) ?? []

        let cancelledDescriptor = FetchDescriptor<TripModel>(
            predicate: #Predicate { $0.status == .cancelled },
            sortBy: [SortDescriptor(\.cancelledAt, order: .reverse)]
        )
        cancelledTrips = (try? modelContext.fetch(cancelledDescriptor)) ?? []

        seedSampleTripIfNeeded()
    }

    func createTrip(destination: String, country: String,
                    startDate: Date, endDate: Date, notes: String) {
        deleteSampleTripsIfNeeded()
        let trip = TripModel(
            destination: destination,
            country: country,
            startDate: startDate,
            endDate: endDate,
            notes: notes
        )
        modelContext.insert(trip)
        try? modelContext.save()
        load()
    }

    func cancelTrip(_ trip: TripModel, reason: String) {
        trip.status = .cancelled
        trip.cancelledAt = .now
        trip.cancellationReason = reason
        try? modelContext.save()
        load()
    }

    func recreateTrip(_ trip: TripModel) {
        let newTrip = TripModel(
            destination: trip.destination,
            country: trip.country,
            startDate: Date.now.addingTimeInterval(14 * 86400),
            endDate: Date.now.addingTimeInterval(21 * 86400),
            notes: trip.notes
        )
        modelContext.insert(newTrip)
        try? modelContext.save()
        load()
    }

    func markComplete(_ trip: TripModel) {
        trip.status = .past
        try? modelContext.save()
        load()
    }

    private func seedSampleTripIfNeeded() {
        let all = (try? modelContext.fetch(FetchDescriptor<TripModel>())) ?? []
        guard all.isEmpty else { return }
        let sample = TripModel(
            id: "sample-tokyo",
            destination: "Tokyo",
            country: "Japan",
            startDate: Date.now.addingTimeInterval(30 * 86400),
            endDate: Date.now.addingTimeInterval(37 * 86400),
            notes: "Sample trip — replace with your own!",
            isSample: true
        )
        modelContext.insert(sample)
        try? modelContext.save()
        load()
    }

    private func deleteSampleTripsIfNeeded() {
        let descriptor = FetchDescriptor<TripModel>(
            predicate: #Predicate { $0.isSample == true }
        )
        let samples = (try? modelContext.fetch(descriptor)) ?? []
        samples.forEach { modelContext.delete($0) }
    }
}
