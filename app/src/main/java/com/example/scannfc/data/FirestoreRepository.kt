package com.example.scannfc.data

import com.example.scannfc.models.Group
import com.example.scannfc.models.ScanRecord
import com.example.scannfc.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Сохранить данные пользователя после регистрации
    suspend fun saveUserProfile(user: User): Boolean {
        return try {
            db.collection("users").document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 2. Получить данные текущего пользователя
    suspend fun getUserProfile(uid: String): User? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // 3. Получить список всех групп для выбора при регистрации
    suspend fun getAllGroups(): List<Group> {
        return try {
            val snapshot = db.collection("groups").get().await()
            snapshot.documents.mapNotNull { it.toObject(Group::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 4. Сохранить результат сканирования NFC метки
    suspend fun saveScan(scan: ScanRecord): Boolean {
        return try {
            val docRef = db.collection("scans").document()
            val scanWithId = scan.copy(id = docRef.id)
            docRef.set(scanWithId).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 5. Получить историю сканирований (для студента - свои, для учителя - по группе)
    suspend fun getScanHistory(userId: String? = null, groupId: String? = null): List<ScanRecord> {
        return try {
            var query: Query = db.collection("scans")
            
            if (userId != null) {
                query = query.whereEqualTo("userId", userId)
            } else if (groupId != null) {
                query = query.whereEqualTo("userGroupId", groupId)
            }
            
            val snapshot = query.orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            snapshot.documents.mapNotNull { it.toObject(ScanRecord::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
