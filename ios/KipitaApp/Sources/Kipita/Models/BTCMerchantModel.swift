import Foundation
import SwiftData

enum BTCSource: String, Codable {
    case btcmap = "BTCMAP"
    case cashapp = "CASHAPP"
    case both = "BOTH"
}

@Model
final class BTCMerchantModel {
    @Attribute(.unique) var id: String
    var name: String
    var lat: Double
    var lng: Double
    var source: BTCSource
    var address: String?
    var phone: String?
    var website: String?
    var lastUpdated: Date

    init(id: String, name: String, lat: Double, lng: Double,
         source: BTCSource, address: String? = nil,
         phone: String? = nil, website: String? = nil,
         lastUpdated: Date = .now) {
        self.id = id
        self.name = name
        self.lat = lat
        self.lng = lng
        self.source = source
        self.address = address
        self.phone = phone
        self.website = website
        self.lastUpdated = lastUpdated
    }
}
