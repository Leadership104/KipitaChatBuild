import SwiftUI
import CoreLocation

struct SosSheet: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Emergency Services") {
                    Link(destination: URL(string: "tel:911")!) {
                        Label("Call 911", systemImage: "phone.fill")
                            .foregroundStyle(.red)
                    }
                    Link(destination: URL(string: "https://www.google.com/maps/search/hospital+near+me")!) {
                        Label("Find Nearest Hospital", systemImage: "cross.fill")
                    }
                    Link(destination: URL(string: "https://www.google.com/maps/search/fire+station+near+me")!) {
                        Label("Find Fire Station", systemImage: "flame.fill")
                    }
                    Link(destination: URL(string: "https://www.google.com/maps/search/police+near+me")!) {
                        Label("Find Police Station", systemImage: "shield.fill")
                    }
                }

                Section("Alert Your Group") {
                    Button {
                        sendEmailAlert()
                    } label: {
                        Label("Email Trip Members", systemImage: "envelope.fill")
                    }
                }
            }
            .navigationTitle("SOS Emergency")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }

    private func sendEmailAlert() {
        let subject = "SOS Alert from Kipita"
        let body = "I need help. This is an emergency alert sent via Kipita."
        if let url = URL(string: "mailto:?subject=\(subject.urlEncoded)&body=\(body.urlEncoded)") {
            UIApplication.shared.open(url)
        }
    }
}

private extension String {
    var urlEncoded: String {
        addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? self
    }
}
