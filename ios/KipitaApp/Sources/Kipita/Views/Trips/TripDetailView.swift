import SwiftUI

struct TripDetailView: View {
    @Environment(\.dismiss) private var dismiss
    let trip: TripModel
    let viewModel: TripsViewModel
    @State private var showCancelAlert = false
    @State private var cancelReason = ""

    var body: some View {
        NavigationStack {
            List {
                Section("Details") {
                    LabeledContent("Destination", value: trip.destination)
                    LabeledContent("Country", value: trip.country)
                    LabeledContent("Start", value: trip.startDate.formatted(date: .long, time: .omitted))
                    LabeledContent("End", value: trip.endDate.formatted(date: .long, time: .omitted))
                    if !trip.notes.isEmpty {
                        LabeledContent("Notes", value: trip.notes)
                    }
                }

                if trip.status == .upcoming || trip.status == .active {
                    Section("Actions") {
                        Button {
                            viewModel.markComplete(trip)
                            dismiss()
                        } label: {
                            Label("Mark Complete ✓", systemImage: "checkmark.circle.fill")
                                .foregroundStyle(.green)
                        }

                        Button(role: .destructive) {
                            showCancelAlert = true
                        } label: {
                            Label("Cancel Trip", systemImage: "xmark.circle.fill")
                        }
                    }
                }

                if trip.status == .cancelled {
                    Section("Cancellation") {
                        if let reason = trip.cancellationReason, !reason.isEmpty {
                            LabeledContent("Reason", value: reason)
                        }
                        Button("Recreate Trip") {
                            viewModel.recreateTrip(trip)
                            dismiss()
                        }
                    }
                }
            }
            .navigationTitle(trip.destination)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
            .alert("Cancel Trip", isPresented: $showCancelAlert) {
                TextField("Reason (optional)", text: $cancelReason)
                Button("Cancel Trip", role: .destructive) {
                    viewModel.cancelTrip(trip, reason: cancelReason)
                    dismiss()
                }
                Button("Keep Trip", role: .cancel) {}
            } message: {
                Text("Are you sure you want to cancel \(trip.destination)?")
            }
        }
    }
}
