import Foundation

// Google Places API (New) v1
// Docs: https://developers.google.com/maps/documentation/places/web-service/nearby-search
final class GooglePlacesService {
    private let apiKey: String = {
        Bundle.main.object(forInfoDictionaryKey: "GOOGLE_PLACES_API_KEY") as? String ?? ""
    }()
    private let baseURL = "https://places.googleapis.com/v1/places:searchNearby"

    func searchNearby(lat: Double, lng: Double, category: PlaceCategory,
                      radius: Double = 2000) async throws -> [Place] {
        var request = URLRequest(url: URL(string: baseURL)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(apiKey, forHTTPHeaderField: "X-Goog-Api-Key")
        request.setValue(
            "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.internationalPhoneNumber,places.priceLevel",
            forHTTPHeaderField: "X-Goog-FieldMask"
        )

        let body: [String: Any] = [
            "includedTypes": category.googleTypes,
            "maxResultCount": 20,
            "locationRestriction": [
                "circle": [
                    "center": ["latitude": lat, "longitude": lng],
                    "radius": radius
                ]
            ]
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(GooglePlacesResponse.self, from: data)

        return response.places?.map { dto -> Place in
            let distKm = haversine(lat, lng, dto.location.latitude, dto.location.longitude)
            return Place(
                id: dto.id,
                name: dto.displayName.text,
                address: dto.formattedAddress ?? "",
                lat: dto.location.latitude,
                lng: dto.location.longitude,
                category: category,
                rating: dto.rating,
                reviewCount: dto.userRatingCount,
                phoneNumber: dto.internationalPhoneNumber,
                distanceKm: distKm,
                priceLevel: dto.priceLevel
            )
        } ?? []
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

// MARK: - DTOs
struct GooglePlacesResponse: Decodable {
    let places: [GooglePlaceDTO]?
}

struct GooglePlaceDTO: Decodable {
    let id: String
    let displayName: DisplayName
    let formattedAddress: String?
    let location: LatLng
    let rating: Double?
    let userRatingCount: Int?
    let internationalPhoneNumber: String?
    let priceLevel: Int?
}

struct DisplayName: Decodable { let text: String }
struct LatLng: Decodable { let latitude: Double; let longitude: Double }
