package com.kipita.di

import com.kipita.BuildConfig
import com.kipita.data.api.BtcMerchantApiService
import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.CoinbaseApiService
import com.kipita.data.api.CurrencyApiService
import com.kipita.data.api.ErrorReportApiService
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.GeminiCryptoApiService
import com.kipita.data.api.GovernmentApiService
import com.kipita.data.api.NomadApiService
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.api.RiverApiService
import com.kipita.data.api.WalletApiService
import com.kipita.data.api.WeatherApiService
import com.kipita.data.api.YelpApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ETag cache: url → (etag, last successful response)
    private val eTagCache = mutableMapOf<String, Pair<String, Response>>()

    private val eTagInterceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url.toString()
        // Attach If-None-Match if we have a cached ETag for this URL
        val requestWithETag = eTagCache[url]?.first?.let { etag ->
            request.newBuilder().header("If-None-Match", etag).build()
        } ?: request
        val response = chain.proceed(requestWithETag)
        // Store new ETag from successful responses
        val newETag = response.header("ETag")
        if (newETag != null && response.isSuccessful) {
            eTagCache[url] = Pair(newETag, response)
        }
        // On 304 Not Modified, return cached response
        if (response.code == 304) {
            eTagCache[url]?.second ?: response
        } else {
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .addInterceptor(eTagInterceptor)
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

    // -----------------------------------------------------------------------
    // Yelp Fusion — local business search
    // -----------------------------------------------------------------------

    @Provides
    @Singleton
    @YelpApi
    fun provideYelpRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.yelp.com/v3/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideYelpApiService(@YelpApi retrofit: Retrofit): YelpApiService =
        retrofit.create(YelpApiService::class.java)

    // -----------------------------------------------------------------------
    // Coinbase — OAuth2 wallet balances
    // -----------------------------------------------------------------------

    @Provides
    @Singleton
    @CoinbaseApi
    fun provideCoinbaseRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coinbase.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideCoinbaseApiService(@CoinbaseApi retrofit: Retrofit): CoinbaseApiService =
        retrofit.create(CoinbaseApiService::class.java)

    // -----------------------------------------------------------------------
    // Gemini Crypto — HMAC-signed REST + market data
    // -----------------------------------------------------------------------

    @Provides
    @Singleton
    @GeminiCryptoApi
    fun provideGeminiCryptoRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.gemini.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideGeminiCryptoApiService(@GeminiCryptoApi retrofit: Retrofit): GeminiCryptoApiService =
        retrofit.create(GeminiCryptoApiService::class.java)

    // -----------------------------------------------------------------------
    // River Financial — Lightning + on-chain Bitcoin
    // -----------------------------------------------------------------------

    @Provides
    @Singleton
    @RiverApi
    fun provideRiverRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://app.river.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    fun provideRiverApiService(@RiverApi retrofit: Retrofit): RiverApiService =
        retrofit.create(RiverApiService::class.java)
}
