package com.kipita.data.repository

import com.kipita.data.local.UserDao
import com.kipita.data.local.UserEntity
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun createAccount(
        displayName: String,
        username: String,
        email: String,
        password: String
    ): Result<UserEntity> {
        val normalizedUsername = normalizeUsername(username)
        val normalizedEmail = normalizeEmail(email)
        if (displayName.isBlank()) return Result.failure(IllegalArgumentException("Display name is required."))
        if (normalizedUsername.isBlank()) return Result.failure(IllegalArgumentException("Username is required."))
        if (normalizedEmail.isBlank()) return Result.failure(IllegalArgumentException("Email is required."))
        if (password.length < 8) return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
        if (userDao.usernameCount(normalizedUsername) > 0) {
            return Result.failure(IllegalStateException("Username is already taken."))
        }
        if (userDao.emailCount(normalizedEmail) > 0) {
            return Result.failure(IllegalStateException("Email is already in use."))
        }
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            displayName = displayName.trim(),
            username = normalizedUsername,
            usernameNormalized = normalizedUsername,
            email = email.trim(),
            emailNormalized = normalizedEmail,
            emailVerified = false,
            passwordHash = sha256(password),
            createdAtEpochMillis = System.currentTimeMillis()
        )
        userDao.upsert(user)
        return Result.success(user)
    }

    suspend fun signIn(email: String, password: String): Result<UserEntity> {
        val normalizedEmail = normalizeEmail(email)
        val user = userDao.getByEmail(normalizedEmail)
            ?: return Result.failure(IllegalStateException("No account found for this email."))
        if (user.passwordHash != sha256(password)) {
            return Result.failure(IllegalStateException("Invalid password."))
        }
        return Result.success(user)
    }

    private fun normalizeUsername(value: String): String =
        value.trim().lowercase().replace(Regex("[^a-z0-9_\\.]"), "")

    private fun normalizeEmail(value: String): String = value.trim().lowercase()

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
