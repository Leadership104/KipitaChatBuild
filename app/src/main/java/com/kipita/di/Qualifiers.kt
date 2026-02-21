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
