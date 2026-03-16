import SwiftUI
import SwiftData

struct TripsView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: TripsViewModel?
    @State private var showPlanSheet = false
    @State private var selectedTrip: TripModel?

    var body: some View {
        NavigationStack {
            Group {
                if let vm = viewModel {
                    tripList(vm: vm)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("My Trips")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showPlanSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .onAppear {
                if viewModel == nil {
                    viewModel = TripsViewModel(modelContext: modelContext)
                }
                viewModel?.load()
            }
            .sheet(isPresented: $showPlanSheet) {
                PlanTripSheet { dest, country, start, end, notes in
                    viewModel?.createTrip(destination: dest, country: country,
                                          startDate: start, endDate: end, notes: notes)
                }
            }
            .sheet(item: $selectedTrip) { trip in
                TripDetailView(trip: trip, viewModel: viewModel!)
            }
        }
    }

    @ViewBuilder
    private func tripList(vm: TripsViewModel) -> some View {
        if vm.upcomingTrips.isEmpty && vm.cancelledTrips.isEmpty {
            ContentUnavailableView("No Trips Yet",
                systemImage: "airplane",
                description: Text("Tap + to plan your first trip."))
        } else {
            List {
                if !vm.upcomingTrips.isEmpty {
                    Section("Upcoming & Active") {
                        ForEach(vm.upcomingTrips) { trip in
                            TripRow(trip: trip)
                                .onTapGesture { selectedTrip = trip }
                        }
                    }
                }
                if !vm.cancelledTrips.isEmpty {
                    Section("Cancelled") {
                        ForEach(vm.cancelledTrips) { trip in
                            TripRow(trip: trip)
                                .swipeActions {
                                    Button("Recreate") { vm.recreateTrip(trip) }
                                        .tint(.blue)
                                }
                        }
                    }
                }
            }
        }
    }
}

struct TripRow: View {
    let trip: TripModel

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(trip.destination)
                    .font(.headline)
                Spacer()
                statusBadge
            }
            Text(trip.country)
                .font(.subheadline).foregroundStyle(.secondary)
            Text("\(trip.startDate.formatted(date: .abbreviated, time: .omitted)) – \(trip.endDate.formatted(date: .abbreviated, time: .omitted))")
                .font(.caption).foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }

    private var statusBadge: some View {
        Text(trip.status.rawValue.capitalized)
            .font(.caption2).bold()
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(statusColor.opacity(0.15), in: Capsule())
            .foregroundStyle(statusColor)
    }

    private var statusColor: Color {
        switch trip.status {
        case .upcoming:  .blue
        case .active:    .green
        case .past:      .secondary
        case .cancelled: .red
        }
    }
}
