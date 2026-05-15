package com.example.scannfc.data

import android.util.Log
import com.example.scannfc.models.Group
import com.example.scannfc.models.ScanRecord
import com.example.scannfc.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUserProfile(user: User): Boolean {
        return try {
            db.collection("users").document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error saving user profile", e)
            false
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting user profile", e)
            null
        }
    }

    suspend fun getAllGroups(): List<Group> {
        return try {
            val snapshot = db.collection("groups").get().await()
            Log.d("FirestoreData", "Groups found: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                val group = doc.toObject(Group::class.java)
                if (group == null) {
                    Log.e("FirestoreError", "Failed to parse group: ${doc.id}")
                }
                group
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting groups", e)
            emptyList()
        }
    }

    suspend fun saveScan(scan: ScanRecord): Boolean {
        return try {
            val docRef = db.collection("scans").document()
            val scanWithId = scan.copy(id = docRef.id)
            docRef.set(scanWithId).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error saving scan", e)
            false
        }
    }

    suspend fun getScanHistory(userId: String? = null, groupId: String? = null): List<ScanRecord> {
        return try {
            var query: Query = db.collection("scans")
            if (userId != null) query = query.whereEqualTo("userId", userId)
            else if (groupId != null) query = query.whereEqualTo("userGroupId", groupId)
            
            val snapshot = query.orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            snapshot.documents.mapNotNull { it.toObject(ScanRecord::class.java) }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting history", e)
            emptyList()
        }
    }
}
