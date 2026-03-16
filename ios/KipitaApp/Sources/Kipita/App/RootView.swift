import SwiftUI

struct RootView: View {
    @Environment(AppState.self) private var appState

    var body: some View {
        @Bindable var state = appState

        TabView(selection: $state.selectedTab) {
            ForEach(AppState.Tab.allCases, id: \.self) { tab in
                tabContent(tab)
                    .tabItem {
                        Label(tab.label, systemImage: tab.icon)
                    }
                    .tag(tab)
            }
        }
        .tint(Color("AccentColor"))
        .sheet(isPresented: $state.showAuth) {
            AuthView()
        }
        .sheet(isPresented: $state.showWebView) {
            InAppWebView(url: state.webViewUrl, title: state.webViewTitle)
        }
    }

    @ViewBuilder
    private func tabContent(_ tab: AppState.Tab) -> some View {
        switch tab {
        case .home:      HomeView()
        case .trips:     TripsView()
        case .explore:   ExploreView()
        case .map:       MapView()
        case .advisory:  AdvisoryView()
        case .ai:        AIAssistantView()
        }
    }
}
