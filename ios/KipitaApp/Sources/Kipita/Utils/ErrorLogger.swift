import Foundation

final class ErrorLogger {
    static let shared = ErrorLogger()
    private var logs: [(Date, String, String)] = []

    func log(tag: String, message: String) {
        logs.append((.now, tag, message))
        #if DEBUG
        print("[Kipita] [\(tag)] \(message)")
        #endif
    }

    func log(tag: String, error: Error) {
        log(tag: tag, message: error.localizedDescription)
    }

    func allLogs() -> [(Date, String, String)] { logs }
    func flush() { logs.removeAll() }
}
