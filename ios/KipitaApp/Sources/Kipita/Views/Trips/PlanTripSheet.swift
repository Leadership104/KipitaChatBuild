import SwiftUI

struct PlanTripSheet: View {
    @Environment(\.dismiss) private var dismiss
    var onCreate: (String, String, Date, Date, String) -> Void

    @State private var destination = ""
    @State private var country = ""
    @State private var startDate = Date.now
    @State private var endDate = Date.now.addingTimeInterval(7 * 86400)
    @State private var notes = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Trip Details") {
                    TextField("Destination", text: $destination)
                    TextField("Country", text: $country)
                }
                Section("Dates") {
                    DatePicker("Start Date", selection: $startDate, displayedComponents: .date)
                    DatePicker("End Date", selection: $endDate, in: startDate..., displayedComponents: .date)
                }
                Section("Notes") {
                    TextField("Optional notes...", text: $notes, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle("Plan a Trip")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Create") {
                        guard !destination.isEmpty, !country.isEmpty else { return }
                        onCreate(destination, country, startDate, endDate, notes)
                        dismiss()
                    }
                    .disabled(destination.isEmpty || country.isEmpty)
                }
            }
        }
    }
}
