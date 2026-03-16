import SwiftUI
import SwiftData

struct ExploreView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: ExploreViewModel?
    @State private var selectedDestination: Destination?

    var body: some View {
        NavigationStack {
            Group {
                if let vm = viewModel {
                    content(vm: vm)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Explore")
            .onAppear {
                if viewModel == nil {
                    viewModel = ExploreViewModel(modelContext: modelContext)
                }
            }
        }
        .sheet(item: $selectedDestination) { dest in
            DestinationDetailSheet(destination: dest)
        }
    }

    @ViewBuilder
    private func content(vm: ExploreViewModel) -> some View {
        ScrollView {
            VStack(spacing: 16) {
                SearchBar(text: Binding(
                    get: { vm.searchQuery },
                    set: { vm.searchQuery = $0 }
                ))
                .padding(.horizontal)

                LazyVStack(spacing: 12) {
                    ForEach(vm.filteredDestinations) { dest in
                        DestinationCard(destination: dest)
                            .onTapGesture { selectedDestination = dest }
                            .padding(.horizontal)
                    }
                }
            }
            .padding(.top)
        }
    }
}

struct DestinationCard: View {
    let destination: Destination

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(destination.name).font(.headline)
                    Text(destination.country).font(.subheadline).foregroundStyle(.secondary)
                }
                Spacer()
                Text("~$\(destination.costPerDay)/day")
                    .font(.subheadline).bold()
                    .foregroundStyle(.green)
            }

            HStack(spacing: 16) {
                StatPill(icon: "shield", value: "\(destination.safetyScore)/5", label: "Safety")
                StatPill(icon: "wifi", value: "\(destination.wifiScore)/5", label: "WiFi")
                StatPill(icon: "figure.walk", value: "\(destination.walkabilityScore)/5", label: "Walk")
            }

            Text(destination.description)
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(2)
        }
        .padding()
        .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 6, y: 2)
    }
}

struct StatPill: View {
    let icon: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 2) {
            Image(systemName: icon).font(.caption)
            Text(value).font(.caption2).bold()
            Text(label).font(.system(size: 9)).foregroundStyle(.secondary)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 8))
    }
}

struct SearchBar: View {
    @Binding var text: String
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
            TextField("Search destinations...", text: $text)
            if !text.isEmpty {
                Button { text = "" } label: {
                    Image(systemName: "xmark.circle.fill").foregroundStyle(.secondary)
                }
            }
        }
        .padding(10)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 12))
    }
}
