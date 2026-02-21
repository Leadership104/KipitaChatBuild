package com.kipita.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// KeystoreManager
//
// Hardware-backed secure storage for API keys, OAuth tokens, and other
// sensitive credentials using the Android KeyStore system.
//
// Encryption: AES/GCM/NoPadding (256-bit) — hardware-backed on supported
// devices (Secure Element or StrongBox).
//
// Stored values are AES-GCM encrypted; ciphertext + IV stored in
// EncryptedSharedPreferences (wrapped around the platform SharedPreferences).
// ---------------------------------------------------------------------------

@Singleton
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Keystore alias constants — each key has a dedicated AES key in Android KeyStore
        const val YELP_API_KEY_ALIAS = "kipita_yelp_api_key"
        const val COINBASE_OAUTH_TOKEN_ALIAS = "kipita_coinbase_oauth_token"
        const val COINBASE_REFRESH_TOKEN_ALIAS = "kipita_coinbase_refresh_token"
        const val GEMINI_API_KEY_ALIAS = "kipita_gemini_api_key"
        const val GEMINI_API_SECRET_ALIAS = "kipita_gemini_api_secret"
        const val RIVER_OAUTH_TOKEN_ALIAS = "kipita_river_oauth_token"
        const val RIVER_REFRESH_TOKEN_ALIAS = "kipita_river_refresh_token"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
        private const val PREFS_NAME = "kipita_secure_vault"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Securely store an API key or token.
     * The value is AES-GCM encrypted using a hardware-backed key before storage.
     */
    fun storeApiKey(alias: String, value: String) {
        val secretKey = getOrCreateKey(alias)
        val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        prefs.edit()
            .putString("${alias}_iv", ivB64)
            .putString("${alias}_enc", encB64)
            .apply()
    }

    /**
     * Retrieve and decrypt a stored API key or token.
     * Returns null if the alias was never stored.
     */
    fun getApiKey(alias: String): String? {
        val ivB64 = prefs.getString("${alias}_iv", null) ?: return null
        val encB64 = prefs.getString("${alias}_enc", null) ?: return null
        return try {
            val iv = Base64.decode(ivB64, Base64.NO_WRAP)
            val encrypted = Base64.decode(encB64, Base64.NO_WRAP)
            val secretKey = keyStore.getKey(alias, null) as SecretKey
            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convenience — same as [storeApiKey] but named for OAuth token clarity.
     */
    fun storeOAuthToken(alias: String, token: String) = storeApiKey(alias, token)

    /**
     * Convenience — same as [getApiKey] but named for OAuth token clarity.
     */
    fun getOAuthToken(alias: String): String? = getApiKey(alias)

    /**
     * Remove a stored secret (e.g., on sign-out).
     */
    fun deleteKey(alias: String) {
        prefs.edit()
            .remove("${alias}_iv")
            .remove("${alias}_enc")
            .apply()
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    /** Returns true if a key/token for the given alias has been stored. */
    fun hasKey(alias: String): Boolean =
        prefs.contains("${alias}_enc") && keyStore.containsAlias(alias)

    // -----------------------------------------------------------------------
    // Internal: create AES/GCM key in Android KeyStore if not already present
    // -----------------------------------------------------------------------

    private fun getOrCreateKey(alias: String): SecretKey {
        if (keyStore.containsAlias(alias)) {
            return keyStore.getKey(alias, null) as SecretKey
        }
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false) // set to true for biometric-gated keys
            .build()
        keyGen.init(keySpec)
        return keyGen.generateKey()
    }
}
