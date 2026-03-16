import SwiftUI
import CoreLocation
import SwiftData

@Observable
@MainActor
final class ExploreViewModel {
    var searchQuery = ""
    var destinations: [Destination] = Destination.defaults
    var savedPlaceIds: Set<String> = []
    var userLocation: CLLocationCoordinate2D?
    var isLoadingLocation = false

    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        loadSavedIds()
    }

    var filteredDestinations: [Destination] {
        let base = searchQuery.isEmpty ? destinations : destinations.filter {
            $0.name.localizedCaseInsensitiveContains(searchQuery) ||
            $0.country.localizedCaseInsensitiveContains(searchQuery)
        }
        guard let loc = userLocation else { return base }
        return base.sorted { a, b in
            haversine(a.lat, a.lng, loc.latitude, loc.longitude) <
            haversine(b.lat, b.lng, loc.latitude, loc.longitude)
        }
    }

    func toggleSaved(_ place: Place) {
        if savedPlaceIds.contains(place.id) {
            savedPlaceIds.remove(place.id)
            let desc = FetchDescriptor<SavedLocationModel>(
                predicate: #Predicate { $0.id == place.id }
            )
            (try? modelContext.fetch(desc))?.forEach { modelContext.delete($0) }
        } else {
            savedPlaceIds.insert(place.id)
            let saved = SavedLocationModel(
                id: place.id, name: place.name, address: place.address,
                lat: place.lat, lng: place.lng, category: place.category.rawValue
            )
            modelContext.insert(saved)
        }
        try? modelContext.save()
    }

    private func loadSavedIds() {
        let all = (try? modelContext.fetch(FetchDescriptor<SavedLocationModel>())) ?? []
        savedPlaceIds = Set(all.map { $0.id })
    }

    private func haversine(_ lat1: Double, _ lng1: Double,
                            _ lat2: Double, _ lng2: Double) -> Double {
        let R = 6371.0
        let dLat = (lat2 - lat1) * .pi / 180
        let dLng = (lng2 - lng1) * .pi / 180
        let a = sin(dLat/2)*sin(dLat/2) +
                cos(lat1 * .pi/180)*cos(lat2 * .pi/180)*sin(dLng/2)*sin(dLng/2)
        return R * 2 * atan2(sqrt(a), sqrt(1-a))
    }
}

struct Destination: Identifiable {
    let id: String
    let name: String
    let country: String
    let lat: Double
    let lng: Double
    let description: String
    let costPerDay: Int       // USD
    let safetyScore: Int      // 1–5
    let wifiScore: Int        // 1–5
    let walkabilityScore: Int // 1–5
    let imageUrl: String?

    static let defaults: [Destination] = [
        Destination(id: "bkk", name: "Bangkok", country: "Thailand", lat: 13.75, lng: 100.52,
                    description: "Vibrant street food, temples, and buzzing nightlife.", costPerDay: 55, safetyScore: 3, wifiScore: 4, walkabilityScore: 3, imageUrl: nil),
        Destination(id: "mde", name: "Medellín", country: "Colombia", lat: 6.24, lng: -75.58,
                    description: "Eternal spring climate, tech scene, digital nomad hubs.", costPerDay: 45, safetyScore: 3, wifiScore: 4, walkabilityScore: 4, imageUrl: nil),
        Destination(id: "lis", name: "Lisbon", country: "Portugal", lat: 38.71, lng: -9.14,
                    description: "Cobblestone charm, trams, fado, and Atlantic views.", costPerDay: 80, safetyScore: 5, wifiScore: 4, walkabilityScore: 5, imageUrl: nil),
        Destination(id: "chn", name: "Chiang Mai", country: "Thailand", lat: 18.79, lng: 98.98,
                    description: "Lush mountains, temples, low cost, large nomad community.", costPerDay: 40, safetyScore: 4, wifiScore: 4, walkabilityScore: 3, imageUrl: nil),
        Destination(id: "tyo", name: "Tokyo", country: "Japan", lat: 35.68, lng: 139.69,
                    description: "Hyper-efficient transit, incredible food, safe streets.", costPerDay: 110, safetyScore: 5, wifiScore: 5, walkabilityScore: 5, imageUrl: nil),
        Destination(id: "tlv", name: "Tel Aviv", country: "Israel", lat: 32.06, lng: 34.78,
                    description: "Startup nation, beaches, vibrant tech & cultural scene.", costPerDay: 100, safetyScore: 3, wifiScore: 5, walkabilityScore: 4, imageUrl: nil),
        Destination(id: "prg", name: "Prague", country: "Czech Republic", lat: 50.08, lng: 14.44,
                    description: "Medieval architecture, beer culture, affordable Europe.", costPerDay: 70, safetyScore: 4, wifiScore: 4, walkabilityScore: 5, imageUrl: nil),
        Destination(id: "cpt", name: "Cape Town", country: "South Africa", lat: -33.93, lng: 18.42,
                    description: "Table Mountain, beaches, wine routes, creative scene.", costPerDay: 65, safetyScore: 2, wifiScore: 3, walkabilityScore: 3, imageUrl: nil),
    ]
}
