package com.kipita.di

import com.kipita.BuildConfig
import com.kipita.data.api.BtcMerchantApiService
import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.CurrencyApiService
import com.kipita.data.api.ErrorReportApiService
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.GovernmentApiService
import com.kipita.data.api.NomadApiService
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.api.WalletApiService
import com.kipita.data.api.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @PrimaryApi
    fun providePrimaryRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @BtcMapApi
    fun provideBtcMapRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.btcmap.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @NomadApi
    fun provideNomadRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.nomadlist.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    // Frankfurter.app: free, no API key, covers 30+ currencies with ECB data
    @Provides
    @Singleton
    @CurrencyApi
    fun provideCurrencyRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.frankfurter.app/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    // Open-Meteo: free, no API key, real-time global weather
    @Provides
    @Singleton
    @WeatherApi
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @OpenAiApi
    fun provideOpenAiRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @ClaudeApi
    fun provideClaudeRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.anthropic.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @GeminiApi
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideGovernmentApiService(@PrimaryApi retrofit: Retrofit): GovernmentApiService = retrofit.create(GovernmentApiService::class.java)

    @Provides
    fun provideWalletApiService(@PrimaryApi retrofit: Retrofit): WalletApiService = retrofit.create(WalletApiService::class.java)

    @Provides
    fun provideErrorReportApiService(@PrimaryApi retrofit: Retrofit): ErrorReportApiService =
        retrofit.create(ErrorReportApiService::class.java)

    @Provides
    fun provideBtcMerchantApiService(@BtcMapApi retrofit: Retrofit): BtcMerchantApiService = retrofit.create(BtcMerchantApiService::class.java)

    @Provides
    fun provideNomadApiService(@NomadApi retrofit: Retrofit): NomadApiService = retrofit.create(NomadApiService::class.java)

    @Provides
    fun provideCurrencyApiService(@CurrencyApi retrofit: Retrofit): CurrencyApiService = retrofit.create(CurrencyApiService::class.java)

    @Provides
    fun provideWeatherApiService(@WeatherApi retrofit: Retrofit): WeatherApiService = retrofit.create(WeatherApiService::class.java)

    @Provides
    fun provideOpenAiApiService(@OpenAiApi retrofit: Retrofit): OpenAiApiService = retrofit.create(OpenAiApiService::class.java)

    @Provides
    fun provideClaudeApiService(@ClaudeApi retrofit: Retrofit): ClaudeApiService = retrofit.create(ClaudeApiService::class.java)

    @Provides
    fun provideGeminiApiService(@GeminiApi retrofit: Retrofit): GeminiApiService = retrofit.create(GeminiApiService::class.java)
}
