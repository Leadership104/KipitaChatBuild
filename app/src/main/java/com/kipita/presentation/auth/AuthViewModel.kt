package com.kipita.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kipita.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearError() {
        _authError.value = null
    }

    fun createAccount(
        displayName: String,
        username: String,
        email: String,
        password: String,
        onSuccess: (displayName: String) -> Unit
    ) {
        viewModelScope.launch {
            val result = accountRepository.createAccount(displayName, username, email, password)
            result.onSuccess {
                _authError.value = null
                onSuccess(it.displayName)
            }.onFailure { err ->
                _authError.value = err.message ?: "Unable to create account."
            }
        }
    }

    fun signIn(
        email: String,
        password: String,
        onSuccess: (displayName: String) -> Unit
    ) {
        viewModelScope.launch {
            val result = accountRepository.signIn(email, password)
            result.onSuccess {
                _authError.value = null
                onSuccess(it.displayName)
            }.onFailure { err ->
                _authError.value = err.message ?: "Unable to sign in."
            }
        }
    }
}
