package com.example.homeserv.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.ServiceRequest
import kotlinx.coroutines.tasks.await

class RequestRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col get() = db.collection(ServiceRequest.COLLECTION)

    // ── Submit new request ────────────────────────────────────────

    suspend fun submitRequest(request: ServiceRequest): Resource<Unit> {
        return try {
            val docRef = col.document()
            val final  = request.copy(id = docRef.id)
            docRef.set(final.toMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit request")
        }
    }

    // ── Fetch customer orders ─────────────────────────────────────

    suspend fun getCustomerRequests(customerId: String): Resource<List<ServiceRequest>> {
        return try {
            val snap = col
                .whereEqualTo("customerId", customerId)
                .get().await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(ServiceRequest::class.java)?.copy(
                    // Safely read rating — defaults to 0f if field missing
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat()
                )
            }.sortedByDescending { it.createdAt }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load orders")
        }
    }

    // ── Fetch all requests (admin) ────────────────────────────────

    suspend fun getAllRequests(): Resource<List<ServiceRequest>> {
        return try {
            val snap = col.get().await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(ServiceRequest::class.java)?.copy(
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat()
                )
            }.sortedByDescending { it.createdAt }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load requests")
        }
    }

    // ── Update status (admin) ─────────────────────────────────────

    suspend fun updateStatus(requestId: String, status: String): Resource<Unit> {
        return try {
            col.document(requestId).update(
                mapOf(
                    "status"    to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update status")
        }
    }

    // ── Submit rating ─────────────────────────────────────────────

    suspend fun submitRating(requestId: String, rating: Float): Resource<Unit> {
        return try {
            col.document(requestId)
                .set(
                    mapOf(
                        "rating"    to rating,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit rating")
        }
    }

    // ── Get rated completed requests for a provider ───────────────

    suspend fun getCompletedRequestsForProvider(
        providerId: String
    ): Resource<List<ServiceRequest>> {
        return try {
            // Only filter by providerId — filter status client-side
            // to avoid composite index requirement
            val snap = col
                .whereEqualTo("providerId", providerId)
                .get().await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(ServiceRequest::class.java)?.copy(
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat()
                )
            }.filter {
                it.status == ServiceRequest.STATUS_COMPLETED && it.rating > 0f
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load ratings")
        }
    }

    // ── Dashboard stats ───────────────────────────────────────────

    suspend fun getStats(): Resource<Map<String, Any>> {
        return try {
            val all = col.get().await().toObjects(ServiceRequest::class.java)
            val stats = mapOf(
                "total"      to all.size,
                "pending"    to all.count { it.status == ServiceRequest.STATUS_PENDING },
                "inProgress" to all.count { it.status == ServiceRequest.STATUS_IN_PROGRESS },
                "completed"  to all.count { it.status == ServiceRequest.STATUS_COMPLETED },
                "cancelled"  to all.count { it.status == ServiceRequest.STATUS_CANCELLED },
                "byCategory" to all
                    .groupingBy { it.categoryName }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .associate { it.key to it.value }
            )
            Resource.Success(stats)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load stats")
        }
    }
}