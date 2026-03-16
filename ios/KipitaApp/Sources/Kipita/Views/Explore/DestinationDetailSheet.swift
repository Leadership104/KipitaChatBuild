import SwiftUI

struct DestinationDetailSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(AppState.self) private var appState
    let destination: Destination

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    heroPlaceholder

                    VStack(alignment: .leading, spacing: 8) {
                        Text(destination.name).font(.largeTitle).bold()
                        Text(destination.country).font(.title3).foregroundStyle(.secondary)
                    }
                    .padding(.horizontal)

                    HStack(spacing: 16) {
                        StatPill(icon: "shield", value: "\(destination.safetyScore)/5", label: "Safety")
                        StatPill(icon: "wifi", value: "\(destination.wifiScore)/5", label: "WiFi")
                        StatPill(icon: "figure.walk", value: "\(destination.walkabilityScore)/5", label: "Walk")
                        StatPill(icon: "dollarsign.circle", value: "$\(destination.costPerDay)", label: "/day")
                    }
                    .padding(.horizontal)

                    Text(destination.description)
                        .padding(.horizontal)

                    HStack(spacing: 12) {
                        Button {
                            // share
                            let url = "https://www.google.com/maps/place/\(destination.name)+\(destination.country)"
                            ShareLink(item: url)
                        } label: {
                            Label("Share", systemImage: "square.and.arrow.up")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)

                        Button {
                            appState.selectedTab = .trips
                            dismiss()
                        } label: {
                            Label("Add to Trips", systemImage: "plus.circle.fill")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 40)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }

    private var heroPlaceholder: some View {
        Rectangle()
            .fill(LinearGradient(colors: [.blue.opacity(0.4), .purple.opacity(0.3)],
                                 startPoint: .topLeading, endPoint: .bottomTrailing))
            .frame(height: 220)
            .overlay {
                Text(destination.name.prefix(2).uppercased())
                    .font(.system(size: 72, weight: .black))
                    .foregroundStyle(.white.opacity(0.3))
            }
    }
}
