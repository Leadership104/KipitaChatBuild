package com.kipita.di

import com.kipita.BuildConfig
import com.kipita.data.api.BitcoinPriceApiService
import com.kipita.data.api.BtcMerchantApiService
import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.CoinbaseApiService
import com.kipita.data.api.CurrencyApiService
import com.kipita.data.api.DwaatApiService
import com.kipita.data.api.ErrorReportApiService
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.GeminiCryptoApiService
import com.kipita.data.api.GovernmentApiService
import com.kipita.data.api.GooglePlacesApiService
import com.kipita.data.api.NomadApiService
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.api.RiverApiService
import com.kipita.data.api.WalletApiService
import com.kipita.data.api.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner = CertificatePinner.Builder()
        .add("api.kipita.app", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .add("api.openai.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .add("api.anthropic.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .add("generativelanguage.googleapis.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .add("api.btcmap.org", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .add("api.nomadlist.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .add("api.exchangerate.host", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()

    @Provides
    @Singleton
    fun provideOkHttp(certificatePinner: CertificatePinner): OkHttpClient = OkHttpClient.Builder()
        // Demo mode: disable strict pin checks so third-party APIs (CoinGecko/Maps/Gemini) can resolve reliably.
        // Placeholder pins in this repo block live responses.
        .retryOnConnectionFailure(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
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

    @Provides
    @Singleton
    @CurrencyApi
    fun provideCurrencyRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.exchangerate.host/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

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
    fun provideWeatherApiService(@WeatherApi retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    @Provides
    fun provideOpenAiApiService(@OpenAiApi retrofit: Retrofit): OpenAiApiService = retrofit.create(OpenAiApiService::class.java)

    @Provides
    fun provideClaudeApiService(@ClaudeApi retrofit: Retrofit): ClaudeApiService = retrofit.create(ClaudeApiService::class.java)

    @Provides
    fun provideGeminiApiService(@GeminiApi retrofit: Retrofit): GeminiApiService = retrofit.create(GeminiApiService::class.java)

    @Provides
    @Singleton
    @CoinbaseApi
    fun provideCoinbaseRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coinbase.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @GeminiCryptoApi
    fun provideGeminiCryptoRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.gemini.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @RiverApi
    fun provideRiverRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://app.river.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @GooglePlacesApi
    fun provideGooglePlacesRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://places.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @DwaatApi
    fun provideDwaatRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.dwaat.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideCoinbaseApiService(@CoinbaseApi retrofit: Retrofit): CoinbaseApiService = retrofit.create(CoinbaseApiService::class.java)

    @Provides
    fun provideGeminiCryptoApiService(@GeminiCryptoApi retrofit: Retrofit): GeminiCryptoApiService = retrofit.create(GeminiCryptoApiService::class.java)

    @Provides
    fun provideRiverApiService(@RiverApi retrofit: Retrofit): RiverApiService = retrofit.create(RiverApiService::class.java)

    @Provides
    fun provideGooglePlacesApiService(@GooglePlacesApi retrofit: Retrofit): GooglePlacesApiService = retrofit.create(GooglePlacesApiService::class.java)

    @Provides
    fun provideDwaatApiService(@DwaatApi retrofit: Retrofit): DwaatApiService = retrofit.create(DwaatApiService::class.java)

    @Provides
    @Singleton
    @CoinGeckoApi
    fun provideCoinGeckoRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideBitcoinPriceApiService(@CoinGeckoApi retrofit: Retrofit): BitcoinPriceApiService =
        retrofit.create(BitcoinPriceApiService::class.java)
}
