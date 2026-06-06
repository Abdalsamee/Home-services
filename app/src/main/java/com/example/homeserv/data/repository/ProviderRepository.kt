package com.example.homeserv.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.homeserv.data.model.Provider
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.SortFilter
import com.example.homeserv.data.model.SortOption
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col get() = db.collection(Provider.COLLECTION)

    // ── Safe document → Provider mapping ─────────────────────────
    // Reads every numeric field explicitly to avoid Float/Double confusion

    private fun DocumentSnapshot.toProvider(): Provider? {
        return try {
            Provider(
                id              = getString("id")             ?: id,
                name            = getString("name")           ?: "",
                imageUrl        = getString("imageUrl")       ?: "",
                description     = getString("description")    ?: "",
                categoryId      = getString("categoryId")     ?: "",
                categoryName    = getString("categoryName")   ?: "",
                address         = getString("address")        ?: "",
                phone           = getString("phone")          ?: "",
                latitude        = getDouble("latitude")       ?: 0.0,
                longitude       = getDouble("longitude")      ?: 0.0,
                pricePerHour    = getDouble("pricePerHour")   ?: 0.0,
                rate            = getDouble("rate")           ?: 0.0,
                reviewCount     = getLong("reviewCount")?.toInt()    ?: 0,
                jobsDone        = getLong("jobsDone")?.toInt()       ?: 0,
                experienceYears = getLong("experienceYears")?.toInt() ?: 0,
                isAvailable     = getBoolean("isAvailable")   ?: true,
                createdAt       = getLong("createdAt")        ?: 0L
            )
        } catch (e: Exception) { null }
    }

    // ── Fetch all ─────────────────────────────────────────────────

    suspend fun getProviders(): Resource<List<Provider>> {
        return try {
            val snap = col.orderBy("rate", Query.Direction.DESCENDING).get().await()
            Resource.Success(snap.documents.mapNotNull { it.toProvider() })
        } catch (e: Exception) {
            // Fallback without orderBy if index missing
            try {
                val snap = col.get().await()
                val list = snap.documents.mapNotNull { it.toProvider() }
                    .sortedByDescending { it.rate }
                Resource.Success(list)
            } catch (e2: Exception) {
                Resource.Error(e2.localizedMessage ?: "Failed to load providers")
            }
        }
    }

    // ── Fetch by category ─────────────────────────────────────────

    suspend fun getProvidersByCategory(categoryId: String): Resource<List<Provider>> {
        return try {
            val snap = col
                .whereEqualTo("categoryId", categoryId)
                .get().await()
            val list = snap.documents.mapNotNull { it.toProvider() }
                .sortedByDescending { it.rate }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load providers")
        }
    }

    // ── Fetch single ──────────────────────────────────────────────

    suspend fun getProvider(id: String): Resource<Provider> {
        return try {
            val doc = col.document(id).get().await()
            val p   = doc.toProvider()
                ?: return Resource.Error("Provider not found")
            Resource.Success(p)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load provider")
        }
    }

    // ── Search + filter ───────────────────────────────────────────

    suspend fun searchProviders(filter: SortFilter): Resource<List<Provider>> {
        return try {
            val base = if (filter.categoryId.isNotEmpty())
                getProvidersByCategory(filter.categoryId)
            else
                getProviders()

            if (base is Resource.Error) return base
            var list = base.dataOrNull() ?: emptyList()

            if (filter.query.isNotEmpty()) {
                val q = filter.query.lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                            it.categoryName.lowercase().contains(q) ||
                            it.address.lowercase().contains(q)
                }
            }
            if (filter.onlyAvailable)
                list = list.filter { it.isAvailable }
            if (filter.maxPrice < Double.MAX_VALUE)
                list = list.filter { it.pricePerHour <= filter.maxPrice }

            list = when (filter.sortBy) {
                SortOption.RATING     -> list.sortedByDescending { it.rate }
                SortOption.PRICE_LOW  -> list.sortedBy { it.pricePerHour }
                SortOption.PRICE_HIGH -> list.sortedByDescending { it.pricePerHour }
                SortOption.JOBS_DONE  -> list.sortedByDescending { it.jobsDone }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Search failed")
        }
    }

    // ── Top rated ─────────────────────────────────────────────────

    suspend fun getTopProviders(limit: Long = 5): Resource<List<Provider>> {
        return try {
            val snap = col
                .orderBy("rate", Query.Direction.DESCENDING)
                .limit(limit)
                .get().await()
            Resource.Success(snap.documents.mapNotNull { it.toProvider() })
        } catch (e: Exception) {
            try {
                val snap = col.get().await()
                val list = snap.documents.mapNotNull { it.toProvider() }
                    .sortedByDescending { it.rate }
                    .take(limit.toInt())
                Resource.Success(list)
            } catch (e2: Exception) {
                Resource.Error(e2.localizedMessage ?: "Failed to load top providers")
            }
        }
    }

    // ── Add ───────────────────────────────────────────────────────

    suspend fun addProvider(
        provider: Provider,
        localImagePath: String = ""
    ): Resource<Unit> {
        return try {
            val docRef     = col.document()
            val finalImage = if (localImagePath.isNotEmpty()) localImagePath else provider.imageUrl
            val final      = provider.copy(id = docRef.id, imageUrl = finalImage)
            docRef.set(final.toMap()).await()

            // Sync category count to Firestore
            if (provider.categoryId.isNotEmpty())
                syncCategoryCount(provider.categoryId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add provider")
        }
    }

    // ── Update ────────────────────────────────────────────────────

    suspend fun updateProvider(
        provider: Provider,
        localImagePath: String = ""
    ): Resource<Unit> {
        return try {
            if (provider.id.isEmpty())
                return Resource.Error("Provider ID is missing")
            val finalImage = if (localImagePath.isNotEmpty()) localImagePath else provider.imageUrl
            val updated    = provider.copy(imageUrl = finalImage)
            col.document(updated.id).set(updated.toMap()).await()

            // Sync category count in case category changed
            if (provider.categoryId.isNotEmpty())
                syncCategoryCount(provider.categoryId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update provider")
        }
    }

    // ── Delete ────────────────────────────────────────────────────

    suspend fun deleteProvider(id: String): Resource<Unit> {
        return try {
            val doc = col.document(id).get().await()
            val p   = doc.toProvider()
            col.document(id).delete().await()

            // Sync category count after deletion
            p?.let {
                if (it.categoryId.isNotEmpty())
                    syncCategoryCount(it.categoryId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete provider")
        }
    }

    // ── Update provider average rating ────────────────────────────

    suspend fun updateProviderRating(
        providerId: String,
        newAverage: Float,
        reviewCount: Int
    ): Resource<Unit> {
        return try {
            if (providerId.isEmpty()) return Resource.Error("Provider ID is empty")

            val rateDouble = newAverage.toDouble()

            // Use set with merge so it works even if document has issues
            col.document(providerId)
                .set(
                    mapOf(
                        "rate"        to rateDouble,
                        "reviewCount" to reviewCount
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()

            android.util.Log.d("Rating", "✓ Provider $providerId updated: rate=$rateDouble reviewCount=$reviewCount")
            Resource.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Rating", "✗ Failed to update provider: ${e.message}")
            Resource.Error(e.localizedMessage ?: "Failed to update rating")
        }
    }

    // ── Sync category provider count directly from Firestore ──────

    private suspend fun syncCategoryCount(categoryId: String) {
        try {
            val snap = col
                .whereEqualTo("categoryId", categoryId)
                .get().await()
            val count = snap.documents.size
            db.collection("categories")
                .document(categoryId)
                .update("providerCount", count)
                .await()
            android.util.Log.d("Category", "✓ Synced count for $categoryId = $count")
        } catch (e: Exception) {
            android.util.Log.e("Category", "✗ Failed to sync count: ${e.message}")
        }
    }

    // ── Migrate rate fields to Double ─────────────────────────────

    suspend fun migrateRatesToDouble() {
        try {
            val snap = col.get().await()
            for (doc in snap.documents) {
                val raw = doc.get("rate") ?: 0.0
                // Always rewrite as Double regardless of current type
                val rateDouble = when (raw) {
                    is Double -> raw
                    is Float  -> raw.toDouble()
                    is Long   -> raw.toDouble()
                    is Int    -> raw.toDouble()
                    is Number -> raw.toDouble()
                    else      -> 0.0
                }
                col.document(doc.id).update("rate", rateDouble).await()
            }
        } catch (_: Exception) {}
    }
}