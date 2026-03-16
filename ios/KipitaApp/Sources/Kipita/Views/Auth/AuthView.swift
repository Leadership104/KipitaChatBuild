import SwiftUI
import SwiftData

struct AuthView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: AuthViewModel?
    @State private var isSignUp = false
    @State private var email = ""
    @State private var password = ""
    @State private var displayName = ""

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Spacer()

                Image(systemName: "airplane.circle.fill")
                    .font(.system(size: 64))
                    .foregroundStyle(.accentColor)

                Text(isSignUp ? "Create Account" : "Sign In")
                    .font(.largeTitle).bold()

                VStack(spacing: 12) {
                    if isSignUp {
                        TextField("Name", text: $displayName)
                            .textFieldStyle(.roundedBorder)
                            .textContentType(.name)
                    }
                    TextField("Email", text: $email)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.emailAddress)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                    SecureField("Password", text: $password)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(isSignUp ? .newPassword : .password)
                }

                if let vm = viewModel {
                    if let error = vm.error {
                        Text(error).foregroundStyle(.red).font(.caption)
                    }

                    Button {
                        Task {
                            if isSignUp {
                                await vm.createAccount(email: email, password: password, displayName: displayName)
                            } else {
                                await vm.signIn(email: email, password: password)
                            }
                        }
                    } label: {
                        Group {
                            if vm.isLoading { ProgressView() }
                            else { Text(isSignUp ? "Create Account" : "Sign In") }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.accentColor, in: RoundedRectangle(cornerRadius: 14))
                        .foregroundStyle(.white)
                    }
                    .disabled(vm.isLoading)

                    Divider().overlay(Text("or").foregroundStyle(.secondary).font(.caption))

                    Button {
                        Task { await vm.signInWithGoogle() }
                    } label: {
                        HStack {
                            Image(systemName: "globe")
                            Text("Continue with Google")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(.systemGray4)))
                    }
                    .buttonStyle(.plain)
                }

                Button {
                    isSignUp.toggle()
                } label: {
                    Text(isSignUp ? "Already have an account? Sign in" : "New to Kipita? Create account")
                        .font(.subheadline)
                }

                Spacer()
            }
            .padding()
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") { dismiss() }
                }
            }
            .onAppear {
                if viewModel == nil {
                    viewModel = AuthViewModel(modelContext: modelContext)
                }
            }
        }
    }
}
