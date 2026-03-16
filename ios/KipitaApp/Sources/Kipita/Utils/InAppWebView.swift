import SwiftUI
import WebKit

struct InAppWebView: View {
    @Environment(\.dismiss) private var dismiss
    let url: String
    let title: String

    var body: some View {
        NavigationStack {
            Group {
                if let validUrl = URL(string: url) {
                    WebViewRepresentable(url: validUrl)
                } else {
                    ContentUnavailableView("Invalid URL", systemImage: "safari.slash")
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}

struct WebViewRepresentable: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
