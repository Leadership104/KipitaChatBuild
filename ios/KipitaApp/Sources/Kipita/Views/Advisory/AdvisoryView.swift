import SwiftUI

struct AdvisoryView: View {
    @State private var viewModel = AdvisoryViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView("Loading safety data…")
                } else if let report = viewModel.report {
                    reportContent(report)
                } else {
                    placeholderContent
                }
            }
            .navigationTitle("Safety")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        Task { await viewModel.refresh() }
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .task { await viewModel.loadWithLocation() }
        }
    }

    private func reportContent(_ report: RealTimeSafetyReport) -> some View {
        ScrollView {
            VStack(spacing: 16) {
                AiSafetyInsightCard(
                    safetyLevel: report.safetyLevel,
                    label: report.safetyLevelLabel,
                    country: report.country,
                    aiInsight: report.aiInsight
                )

                if !report.weatherLine.isEmpty {
                    InfoCard(title: "🌤 Weather", body: report.weatherLine)
                }

                ForEach(report.advisorySections) { section in
                    DwaatSectionCard(section: section)
                }
            }
            .padding()
        }
    }

    private var placeholderContent: some View {
        VStack(spacing: 16) {
            Image(systemName: "shield.slash")
                .font(.system(size: 56))
                .foregroundStyle(.secondary)
            Text("No safety data available")
                .font(.headline)
            Button("Try Again") { Task { await viewModel.loadWithLocation() } }
                .buttonStyle(.borderedProminent)
        }
    }
}

struct AiSafetyInsightCard: View {
    let safetyLevel: Int
    let label: String
    let country: String
    let aiInsight: String

    private var levelColor: Color {
        switch safetyLevel {
        case 1: .green
        case 2: .yellow
        case 3: .orange
        case 4: .red
        default: .secondary
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(country).font(.title2).bold()
                    Text("Safety Assessment").font(.caption).foregroundStyle(.secondary)
                }
                Spacer()
                VStack(spacing: 4) {
                    SafetyLevelBar(level: safetyLevel)
                    Text(label).font(.caption2).foregroundStyle(levelColor)
                }
            }

            Divider()

            HStack(alignment: .top, spacing: 8) {
                Image(systemName: "sparkles").foregroundStyle(.purple)
                Text(aiInsight).font(.subheadline)
            }
        }
        .padding()
        .background(Color(.systemBackground), in: RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 8, y: 2)
    }
}

struct SafetyLevelBar: View {
    let level: Int // 1–4

    private var color: Color {
        switch level {
        case 1: .green
        case 2: .yellow
        case 3: .orange
        case 4: .red
        default: .secondary
        }
    }

    var body: some View {
        HStack(spacing: 3) {
            ForEach(1...4, id: \.self) { i in
                RoundedRectangle(cornerRadius: 2)
                    .fill(i <= level ? color : Color(.systemGray5))
                    .frame(width: 12, height: 20)
            }
        }
    }
}

struct DwaatSectionCard: View {
    let section: DwaatSection

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(section.title).font(.subheadline).bold()
            Text(section.body).font(.caption).foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 12))
    }
}

struct InfoCard: View {
    let title: String
    let body: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.subheadline).bold()
            Text(body).font(.caption).foregroundStyle(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 12))
    }
}
