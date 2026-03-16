// swift-tools-version: 5.10
import PackageDescription

let package = Package(
    name: "KipitaApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "KipitaApp", targets: ["Kipita"])
    ],
    dependencies: [
        // Add in Xcode: File → Add Package Dependencies
        // .package(url: "https://github.com/firebase/firebase-ios-sdk", from: "11.0.0"),
        // .package(url: "https://github.com/google/GoogleSignIn-iOS", from: "8.0.0"),
    ],
    targets: [
        .target(
            name: "Kipita",
            path: "Sources/Kipita"
        )
    ]
)
