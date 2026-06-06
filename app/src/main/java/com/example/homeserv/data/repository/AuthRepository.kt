package com.example.homeserv.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.homeserv.data.model.Resource
import com.example.homeserv.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Current user ──────────────────────────────────────────────

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // ── Login ─────────────────────────────────────────────────────
    // Used by BOTH admin and customer — role is determined from Firestore

    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid    = result.user?.uid ?: return Resource.Error("Login failed")
            fetchUser(uid)
        } catch (e: Exception) {
            Resource.Error(friendlyError(e.message))
        }
    }

    // ── Register ──────────────────────────────────────────────────
    // Only for customers — admin accounts are created manually in Firebase

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid    = result.user?.uid ?: return Resource.Error("Registration failed")

            val user = User(
                uid      = uid,
                fullName = fullName,
                email    = email,
                phone    = phone,
                role     = User.ROLE_CUSTOMER   // always customer on register
            )
            db.collection(User.COLLECTION)
                .document(uid)
                .set(user.toMap())
                .await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(friendlyError(e.message))
        }
    }

    // ── Fetch user profile from Firestore ─────────────────────────

    suspend fun fetchUser(uid: String): Resource<User> {
        return try {
            val doc  = db.collection(User.COLLECTION).document(uid).get().await()
            val user = doc.toObject(User::class.java)
                ?: return Resource.Error("User profile not found.")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(friendlyError(e.message))
        }
    }

    // ── Update profile ────────────────────────────────────────────
    // profileImageUrl is intentionally left empty — no Storage available

    suspend fun updateProfile(
        uid: String,
        fullName: String,
        phone: String
    ): Resource<Unit> {
        return try {
            db.collection(User.COLLECTION).document(uid).update(
                mapOf(
                    "fullName" to fullName,
                    "phone"    to phone
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(friendlyError(e.message))
        }
    }

    // ── Logout ────────────────────────────────────────────────────

    fun logout() = auth.signOut()

    // ── Friendly error messages ───────────────────────────────────

    private fun friendlyError(message: String?): String {
        return when {
            message == null                              -> "An unexpected error occurred."
            message.contains("no user record")          -> "No account found with this email."
            message.contains("password is invalid")     -> "Incorrect password. Please try again."
            message.contains("email address is badly")  -> "Please enter a valid email address."
            message.contains("email address is already")-> "This email is already registered."
            message.contains("network error")           -> "Network error. Check your connection."
            message.contains("blocked all requests")    -> "Too many attempts. Try again later."
            else                                        -> "Login failed. Please try again."
        }
    }
}