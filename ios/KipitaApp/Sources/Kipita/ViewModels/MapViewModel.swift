import SwiftUI
import CoreLocation
import MapKit

enum MapFilter: String, CaseIterable {
    case btc = "₿ BTC"
    case food = "🍜 Food"
    case cafe = "☕ Cafe"
    case shops = "🛍 Shops"
}

@Observable
@MainActor
final class MapViewModel {
    var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 13.75, longitude: 100.52),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    var activeFilters: Set<MapFilter> = [.btc]
    var btcSource: BTCSource = .btcmap
    var merchants: [BTCMerchantModel] = []
    var foodPlaces: [Place] = []
    var cafePlaces: [Place] = []
    var shopPlaces: [Place] = []
    var isLoading = false
    var searchText = ""

    private let btcService = BTCMapService()
    private let placesService = GooglePlacesService()
    private let locationManager = LocationManager.shared

    var filteredMerchants: [BTCMerchantModel] {
        guard activeFilters.contains(.btc) else { return [] }
        return merchants.filter { m in
            switch btcSource {
            case .btcmap:  m.source == .btcmap
            case .cashapp: m.source == .cashapp
            case .both:    true
            }
        }
    }

    func load(lat: Double, lng: Double) async {
        isLoading = true
        region.center = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        async let btc = btcService.fetchMerchants(lat: lat, lng: lng)
        async let food = placesService.searchNearby(lat: lat, lng: lng, category: .food)
        async let cafe = placesService.searchNearby(lat: lat, lng: lng, category: .cafe)
        async let shops = placesService.searchNearby(lat: lat, lng: lng, category: .shop)
        merchants = (try? await btc) ?? []
        foodPlaces = (try? await food) ?? []
        cafePlaces = (try? await cafe) ?? []
        shopPlaces = (try? await shops) ?? []
        isLoading = false
    }

    func searchAddress(_ query: String) async {
        let geocoder = CLGeocoder()
        guard let placemark = try? await geocoder.geocodeAddressString(query).first,
              let loc = placemark.location else { return }
        await load(lat: loc.coordinate.latitude, lng: loc.coordinate.longitude)
    }

    func requestUserLocation() async {
        guard let loc = await locationManager.requestOnce() else { return }
        await load(lat: loc.latitude, lng: loc.longitude)
    }
}
