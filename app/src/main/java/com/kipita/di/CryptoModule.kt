package com.kipita.di

// ---------------------------------------------------------------------------
// CryptoModule
//
// No explicit @Provides needed here. All crypto/Yelp singletons use
// @Singleton + @Inject constructor and are auto-bound by Hilt:
//
//   • KeystoreManager       — data/security/KeystoreManager.kt
//   • MarketDataWebSocket   — data/api/MarketDataWebSocket.kt
//   • CryptoWalletRepository— data/repository/CryptoWalletRepository.kt
//   • YelpPlacesRepository  — data/repository/YelpPlacesRepository.kt
//
// Their Retrofit service dependencies (CoinbaseApiService, GeminiCryptoApiService,
// RiverApiService, YelpApiService) are provided in NetworkModule via @Provides.
// ---------------------------------------------------------------------------
