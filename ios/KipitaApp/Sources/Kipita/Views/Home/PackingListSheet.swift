import SwiftUI

struct PackingListSheet: View {
    @Environment(\.dismiss) private var dismiss
    var onOpenWebView: (String, String) -> Void

    @State private var checkedItems: Set<String> = []

    private let categories: [(String, [String])] = [
        ("Documents", ["Passport", "Visa", "Travel insurance", "Vaccination records", "Emergency contacts"]),
        ("Electronics", ["Laptop + charger", "Phone + charger", "Universal power adapter", "Portable battery", "Earbuds"]),
        ("Clothing", ["Layering pieces", "Rain jacket", "Comfortable walking shoes", "Quick-dry clothes"]),
        ("Health", ["Prescription meds", "First aid kit", "Sunscreen", "Insect repellent", "Water bottle"]),
        ("Money", ["Local currency", "Travel credit card", "Bitcoin wallet backup", "Emergency cash"]),
    ]

    var body: some View {
        NavigationStack {
            List {
                ForEach(categories, id: \.0) { category, items in
                    Section(category) {
                        ForEach(items, id: \.self) { item in
                            Button {
                                if checkedItems.contains(item) {
                                    checkedItems.remove(item)
                                } else {
                                    checkedItems.insert(item)
                                }
                            } label: {
                                HStack {
                                    Image(systemName: checkedItems.contains(item) ? "checkmark.circle.fill" : "circle")
                                        .foregroundStyle(checkedItems.contains(item) ? .green : .secondary)
                                    Text(item)
                                        .strikethrough(checkedItems.contains(item))
                                        .foregroundStyle(checkedItems.contains(item) ? .secondary : .primary)
                                }
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
            .navigationTitle("Packing List")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
                ToolbarItem(placement: .topBarLeading) {
                    Button("Clear") { checkedItems.removeAll() }
                }
            }
        }
    }
}
