import Foundation
import SwiftData

// BTCMap API: https://api.btcmap.org/v2/elements
final class BTCMapService {
    private let baseURL = "https://api.btcmap.org/v2/elements"
    private let radiusKm = 50.0

    func fetchMerchants(lat: Double, lng: Double) async throws -> [BTCMerchantModel] {
        let url = URL(string: baseURL)!
        var request = URLRequest(url: url)
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, _) = try await URLSession.shared.data(for: request)
        let elements = try JSONDecoder().decode([BTCMapElementDTO].self, from: data)

        return elements.compactMap { el -> BTCMerchantModel? in
            guard let osmLat = el.osm_json.lat, let osmLng = el.osm_json.lon else { return nil }
            guard haversine(lat, lng, osmLat, osmLng) <= radiusKm else { return nil }
            return BTCMerchantModel(
                id: "\(el.id)",
                name: el.osm_json.tags?["name"] ?? "BTC Merchant",
                lat: osmLat,
                lng: osmLng,
                source: .btcmap,
                address: el.osm_json.tags?["addr:street"],
                phone: el.osm_json.tags?["phone"],
                website: el.osm_json.tags?["website"]
            )
        }
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
struct BTCMapElementDTO: Decodable {
    let id: Int
    let osm_json: BTCMapOsmJson
}

struct BTCMapOsmJson: Decodable {
    let lat: Double?
    let lon: Double?
    let tags: [String: String]?
}
