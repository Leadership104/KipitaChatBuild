import SwiftUI

@Observable
final class AppState {
    var selectedTab: Tab = .home
    var currentUser: UserModel?
    var showAuth = false
    var showWebView = false
    var webViewUrl: String = ""
    var webViewTitle: String = ""
    var avatarUrl: String = ""

    enum Tab: Int, CaseIterable {
        case home, trips, explore, map, advisory, ai

        var label: String {
            switch self {
            case .home: "Home"
            case .trips: "Trips"
            case .explore: "Explore"
            case .map: "Map"
            case .advisory: "Safety"
            case .ai: "AI"
            }
        }

        var icon: String {
            switch self {
            case .home: "house.fill"
            case .trips: "airplane"
            case .explore: "safari.fill"
            case .map: "map.fill"
            case .advisory: "shield.fill"
            case .ai: "sparkles"
            }
        }
    }
}
