package com.example.homeserv.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.homeserv.data.model.Category
import com.example.homeserv.data.model.Resource
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col get() = db.collection(Category.COLLECTION)

    // ── Fetch all with live provider counts ───────────────────────

    suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val snap = col.orderBy("name").get().await()
            val categories = snap.toObjects(Category::class.java)

            // Count providers per category directly from Firestore
            val providerSnap = db.collection("providers").get().await()
            val countMap = providerSnap.documents
                .groupBy { it.getString("categoryId") ?: "" }
                .mapValues { it.value.size }

            // Update each category's providerCount in Firestore if changed
            val updated = categories.map { cat ->
                val liveCount = countMap[cat.id] ?: 0
                if (liveCount != cat.providerCount) {
                    // Save correct count back to Firestore
                    col.document(cat.id)
                        .update("providerCount", liveCount)
                        .await()
                }
                cat.copy(providerCount = liveCount)
            }
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load categories")
        }
    }

    // ── Add ───────────────────────────────────────────────────────

    suspend fun addCategory(
        category: Category,
        localImagePath: String = ""
    ): Resource<Unit> {
        return try {
            val docRef = col.document()
            val final  = category.copy(
                id           = docRef.id,
                imageUrl     = if (localImagePath.isNotEmpty()) localImagePath else category.imageUrl,
                providerCount = 0   // starts at 0, updated when providers are added
            )
            docRef.set(final.toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add category")
        }
    }

    // ── Update ────────────────────────────────────────────────────

    suspend fun updateCategory(
        category: Category,
        localImagePath: String = ""
    ): Resource<Unit> {
        return try {
            val updated = category.copy(
                imageUrl = if (localImagePath.isNotEmpty()) localImagePath else category.imageUrl
            )
            col.document(updated.id).set(updated.toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update category")
        }
    }

    // ── Delete ────────────────────────────────────────────────────

    suspend fun deleteCategory(categoryId: String): Resource<Unit> {
        return try {
            col.document(categoryId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete category")
        }
    }

    // ── Recalculate and sync all category counts to Firestore ─────
    // Call this after adding/deleting a provider

    suspend fun syncAllProviderCounts() {
        try {
            val categories   = col.get().await().documents
            val providerSnap = db.collection("providers").get().await()
            val countMap     = providerSnap.documents
                .groupBy { it.getString("categoryId") ?: "" }
                .mapValues { it.value.size }

            for (catDoc in categories) {
                val catId      = catDoc.id
                val liveCount  = countMap[catId] ?: 0
                val savedCount = catDoc.getLong("providerCount")?.toInt() ?: -1
                if (liveCount != savedCount) {
                    col.document(catId).update("providerCount", liveCount).await()
                }
            }
        } catch (_: Exception) {}
    }
}