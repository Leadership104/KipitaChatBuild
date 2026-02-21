package com.kipita.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrimaryApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BtcMapApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAiApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ClaudeApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NomadApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class YelpApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinbaseApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiCryptoApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RiverApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinGeckoApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CashAppApi
