package com.example.scannfc.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    // Получить текущего пользователя
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Вход в систему
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Регистрация (возвращает UID пользователя или null)
    suspend fun signUp(email: String, password: String): String? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    // Выход
    fun signOut() {
        auth.signOut()
    }
}
