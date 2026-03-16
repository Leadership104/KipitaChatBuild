import Foundation
import SwiftData

@Model
final class UserModel {
    @Attribute(.unique) var id: String
    var email: String
    var displayName: String
    var avatarUrl: String
    var createdAt: Date

    init(id: String, email: String, displayName: String,
         avatarUrl: String = "", createdAt: Date = .now) {
        self.id = id
        self.email = email
        self.displayName = displayName
        self.avatarUrl = avatarUrl
        self.createdAt = createdAt
    }
}
