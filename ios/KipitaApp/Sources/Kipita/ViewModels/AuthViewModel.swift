import SwiftUI
import SwiftData

@Observable
@MainActor
final class AuthViewModel {
    var currentUser: UserModel?
    var isLoading = false
    var error: String?

    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func signIn(email: String, password: String) async {
        isLoading = true
        error = nil
        // TODO: wire Firebase Auth
        // let result = try await Auth.auth().signIn(withEmail: email, password: password)
        // currentUser = fetchOrCreateUser(firebaseUser: result.user)
        isLoading = false
    }

    func createAccount(email: String, password: String, displayName: String) async {
        isLoading = true
        error = nil
        // TODO: wire Firebase Auth
        // let result = try await Auth.auth().createUser(withEmail: email, password: password)
        isLoading = false
    }

    func signInWithGoogle() async {
        isLoading = true
        error = nil
        // TODO: GoogleSignIn + Firebase credential
        // let googleUser = try await GIDSignIn.sharedInstance.signIn(...)
        // let credential = GoogleAuthProvider.credential(...)
        // let result = try await Auth.auth().signIn(with: credential)
        isLoading = false
    }

    func signOut() {
        // try? Auth.auth().signOut()
        currentUser = nil
    }

    func updateProfile(displayName: String, avatarUrl: String) {
        guard let user = currentUser else { return }
        user.displayName = displayName
        user.avatarUrl = avatarUrl
        try? modelContext.save()
    }

    func deleteAccount() async {
        isLoading = true
        // TODO: Firebase delete + SwiftData cleanup
        signOut()
        isLoading = false
    }
}
