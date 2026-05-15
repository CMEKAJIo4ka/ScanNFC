package com.example.scannfc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scannfc.data.AuthRepository
import com.example.scannfc.data.FirestoreRepository
import com.example.scannfc.models.Group
import com.example.scannfc.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val firestoreRepository = FirestoreRepository()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _groups.value = firestoreRepository.getAllGroups()
        }
    }

    fun signUp(email: String, password: String, group: Group) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val uid = authRepository.signUp(email, password)
            if (uid != null) {
                val user = User(
                    uid = uid,
                    email = email,
                    role = "student",
                    groupId = group.id
                )
                val success = firestoreRepository.saveUserProfile(user)
                if (success) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Ошибка при сохранении профиля")
                }
            } else {
                _authState.value = AuthState.Error("Ошибка регистрации")
            }
        }
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val success = authRepository.signIn(email, password)
            if (success) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Неверный логин или пароль")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
