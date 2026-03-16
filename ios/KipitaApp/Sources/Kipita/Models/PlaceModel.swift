import Foundation

struct Place: Identifiable, Hashable {
    let id: String
    let name: String
    let address: String
    let lat: Double
    let lng: Double
    let category: PlaceCategory
    let rating: Double?
    let reviewCount: Int?
    let phoneNumber: String?
    let distanceKm: Double?
    let priceLevel: Int?
}

enum PlaceCategory: String, CaseIterable {
    case food = "FOOD"
    case cafe = "CAFE"
    case shop = "SHOPS"
    case hospital = "URGENT_CARE"
    case pharmacy = "PHARMACIES"
    case police = "SAFETY"
    case fitness = "FITNESS"

    var googleTypes: [String] {
        switch self {
        case .food:     ["restaurant", "meal_delivery", "meal_takeaway"]
        case .cafe:     ["cafe", "bakery", "coffee_shop"]
        case .shop:     ["shopping_mall", "store", "clothing_store"]
        case .hospital: ["hospital", "doctor", "emergency_room_hospital"]
        case .pharmacy: ["pharmacy", "drugstore"]
        case .police:   ["police", "fire_station"]
        case .fitness:  ["gym", "spa", "yoga_studio"]
        }
    }

    var displayName: String { rawValue.capitalized }

    var icon: String {
        switch self {
        case .food:     "🍜"
        case .cafe:     "☕"
        case .shop:     "🛍"
        case .hospital: "🏥"
        case .pharmacy: "💊"
        case .police:   "🚔"
        case .fitness:  "🏋️"
        }
    }
}

@Model
final class SavedLocationModel {
    @Attribute(.unique) var id: String
    var name: String
    var address: String
    var lat: Double
    var lng: Double
    var category: String
    var savedAt: Date

    init(id: String = UUID().uuidString, name: String, address: String,
         lat: Double, lng: Double, category: String, savedAt: Date = .now) {
        self.id = id
        self.name = name
        self.address = address
        self.lat = lat
        self.lng = lng
        self.category = category
        self.savedAt = savedAt
    }
}
