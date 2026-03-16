import SwiftUI

struct HomeView: View {
    @Environment(AppState.self) private var appState
    @State private var showSosSheet = false
    @State private var showPackingList = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    greetingCard
                    transportSection
                    safetySection
                    quickActionsSection
                }
                .padding()
            }
            .navigationTitle("Kipita")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        // open profile
                    } label: {
                        Image(systemName: "person.circle.fill")
                            .font(.title3)
                    }
                }
            }
        }
        .sheet(isPresented: $showSosSheet) { SosSheet() }
        .sheet(isPresented: $showPackingList) {
            PackingListSheet(onOpenWebView: { url, title in
                appState.webViewUrl = url
                appState.webViewTitle = title
                appState.showWebView = true
            })
        }
    }

    private var greetingCard: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Good \(greeting)!")
                .font(.title2).bold()
            Text("Where's the next adventure?")
                .foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 16))
    }

    private var transportSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Transport & Booking", systemImage: "airplane.circle.fill")
                .font(.headline)

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                transportCard("✈️ Flights", url: "https://www.google.com/flights")
                transportCard("🏨 Hotels", url: "https://www.booking.com")
                transportCard("🚌 Buses", url: "https://www.busbud.com")
                transportCard("🚂 Trains", url: "https://www.seat61.com")
            }
        }
    }

    private func transportCard(_ title: String, url: String) -> some View {
        Button {
            appState.webViewUrl = url
            appState.webViewTitle = title
            appState.showWebView = true
        } label: {
            Text(title)
                .font(.subheadline).bold()
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }

    private var safetySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Safety & Help", systemImage: "shield.fill")
                .font(.headline)

            Button {
                showSosSheet = true
            } label: {
                Label("SOS Emergency", systemImage: "exclamationmark.triangle.fill")
                    .font(.headline)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red, in: RoundedRectangle(cornerRadius: 14))
            }
        }
    }

    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label("Quick Actions", systemImage: "bolt.circle.fill")
                .font(.headline)

            Button("📦 Packing List") { showPackingList = true }
                .buttonStyle(.bordered)
        }
    }

    private var greeting: String {
        let hour = Calendar.current.component(.hour, from: .now)
        switch hour {
        case 5..<12: return "morning"
        case 12..<17: return "afternoon"
        default: return "evening"
        }
    }
}
