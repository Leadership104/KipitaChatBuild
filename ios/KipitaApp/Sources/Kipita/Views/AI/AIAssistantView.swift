import SwiftUI

struct AIAssistantView: View {
    @State private var viewModel = AIViewModel()
    @State private var showAddToTrips = false
    @FocusState private var inputFocused: Bool

    private let quickActions = [
        ("✈️ Plan a trip", "Plan a 7-day trip to Bangkok, Thailand for a digital nomad with a $50/day budget"),
        ("🛡 Travel safety", "Give me a travel safety briefing for Southeast Asia right now"),
        ("₿ Bitcoin travel", "What are the best countries for Bitcoin-friendly travel in 2025?"),
        ("💸 Cost of living", "Compare cost of living for digital nomads in Lisbon, Medellín, and Chiang Mai"),
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if viewModel.messages.isEmpty {
                    quickActionsPanel
                } else {
                    messageList
                }

                if viewModel.lastPlanDestination != nil {
                    addToTripsBar
                }

                inputBar
            }
            .navigationTitle("Kipita AI")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var quickActionsPanel: some View {
        ScrollView {
            VStack(spacing: 12) {
                VStack(spacing: 8) {
                    Image(systemName: "sparkles")
                        .font(.system(size: 48))
                        .foregroundStyle(.purple)
                    Text("Ask Kipita anything about travel")
                        .font(.headline)
                    Text("Safety, planning, costs, Bitcoin — I've got you.")
                        .font(.subheadline).foregroundStyle(.secondary)
                }
                .padding(.vertical, 24)

                ForEach(quickActions, id: \.0) { action in
                    Button {
                        Task { await viewModel.quickAction(action.1) }
                    } label: {
                        Text(action.0)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding()
                            .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding()
        }
    }

    private var messageList: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.messages) { msg in
                        MessageBubble(message: msg)
                            .id(msg.id)
                    }
                    if viewModel.isTyping {
                        TypingIndicator()
                    }
                }
                .padding()
            }
            .onChange(of: viewModel.messages.count) { _, _ in
                if let last = viewModel.messages.last {
                    withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                }
            }
        }
    }

    private var addToTripsBar: some View {
        HStack {
            if let dest = viewModel.lastPlanDestination {
                Text("Add \(dest) to Trips?")
                    .font(.subheadline)
                Spacer()
                Button("Add") { showAddToTrips = true }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)
                Button { viewModel.clearLastPlan() } label: {
                    Image(systemName: "xmark").font(.caption)
                }
                .foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(Color(.systemGray6))
    }

    private var inputBar: some View {
        HStack(spacing: 8) {
            TextField("Ask about travel…", text: $viewModel.inputText, axis: .vertical)
                .lineLimit(1...4)
                .padding(10)
                .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 12))
                .focused($inputFocused)

            Button {
                let text = viewModel.inputText
                Task { await viewModel.sendMessage(text) }
                inputFocused = false
            } label: {
                Image(systemName: "arrow.up.circle.fill")
                    .font(.title2)
                    .foregroundStyle(viewModel.inputText.isEmpty ? .secondary : .accentColor)
            }
            .disabled(viewModel.inputText.isEmpty || viewModel.isTyping)
        }
        .padding()
        .background(.ultraThinMaterial)
    }
}

struct MessageBubble: View {
    let message: ChatMessage

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            if message.role == .assistant {
                Image(systemName: "sparkles")
                    .font(.caption)
                    .foregroundStyle(.purple)
                    .padding(8)
                    .background(Color.purple.opacity(0.1), in: Circle())
            }

            VStack(alignment: message.role == .user ? .trailing : .leading, spacing: 4) {
                Text(message.text)
                    .padding(12)
                    .background(
                        message.role == .user ? Color.accentColor : Color(.systemGray6),
                        in: RoundedRectangle(cornerRadius: 16)
                    )
                    .foregroundStyle(message.role == .user ? .white : .primary)

                Text(message.timestamp.formatted(date: .omitted, time: .shortened))
                    .font(.caption2).foregroundStyle(.secondary)
            }

            if message.role == .user {
                Image(systemName: "person.circle.fill")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .padding(8)
                    .background(Color(.systemGray5), in: Circle())
            }
        }
        .frame(maxWidth: .infinity, alignment: message.role == .user ? .trailing : .leading)
    }
}

struct TypingIndicator: View {
    @State private var dotScale: [CGFloat] = [1, 1, 1]

    var body: some View {
        HStack(spacing: 4) {
            ForEach(0..<3) { i in
                Circle()
                    .fill(Color.secondary)
                    .frame(width: 8, height: 8)
                    .scaleEffect(dotScale[i])
                    .animation(.easeInOut(duration: 0.5)
                        .repeatForever()
                        .delay(Double(i) * 0.15), value: dotScale[i])
            }
        }
        .padding(12)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 16))
        .frame(maxWidth: .infinity, alignment: .leading)
        .onAppear {
            for i in 0..<3 { dotScale[i] = 0.5 }
        }
    }
}
