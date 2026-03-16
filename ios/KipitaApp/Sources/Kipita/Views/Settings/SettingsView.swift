import SwiftUI
import SwiftData

struct SettingsView: View {
    @Environment(AppState.self) private var appState
    @Environment(\.modelContext) private var modelContext
    @State private var showDeleteConfirm = false
    @State private var showAbout = false

    var body: some View {
        NavigationStack {
            List {
                accountSection
                partnerSection
                supportSection
                legalSection
                dangerSection
            }
            .navigationTitle("Settings")
        }
    }

    private var accountSection: some View {
        Section("Account") {
            if let user = appState.currentUser {
                HStack {
                    Circle()
                        .fill(Color.accentColor.opacity(0.2))
                        .frame(width: 44, height: 44)
                        .overlay(Text(user.displayName.prefix(1)).font(.headline))
                    VStack(alignment: .leading) {
                        Text(user.displayName).font(.headline)
                        Text(user.email).font(.caption).foregroundStyle(.secondary)
                    }
                }
                Button("Sign Out", role: .destructive) { appState.currentUser = nil }
            } else {
                Button("Sign In / Create Account") {
                    appState.showAuth = true
                }
            }
        }
    }

    private var partnerSection: some View {
        Section("Partners") {
            Link("BTCMap.org", destination: URL(string: "https://btcmap.org")!)
            Link("Dwaat Travel Intelligence", destination: URL(string: "https://dwaat.com")!)
        }
    }

    private var supportSection: some View {
        Section("Support") {
            Link("Contact Support", destination: URL(string: "mailto:info@kipita.com")!)
            ShareLink("Share Kipita", item: URL(string: "https://kipita.com")!)
        }
    }

    private var legalSection: some View {
        Section("Legal") {
            Link("Privacy Policy", destination: URL(string: "https://kipita.com/privacy")!)
            Link("Terms of Service", destination: URL(string: "https://kipita.com/terms")!)
        }
    }

    private var dangerSection: some View {
        Section {
            Button("Delete Account", role: .destructive) {
                showDeleteConfirm = true
            }
        }
        .confirmationDialog("Delete Account?", isPresented: $showDeleteConfirm) {
            Button("Delete Account", role: .destructive) {
                appState.currentUser = nil
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This action cannot be undone. All your data will be removed.")
        }
    }
}
