package com.example.homeserv.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.homeserv.data.model.User
import com.google.firebase.auth.FirebaseAuth

/**
 * Persists the logged-in user's basic info locally via SharedPreferences.
 * Firebase Auth handles token refresh automatically — this just caches
 * role + name so we can route correctly without a Firestore round-trip.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    // ── Save after login / register ───────────────────────────────

    fun saveUser(user: User) {
        prefs.edit()
            .putString(Constants.PREF_USER_ID,    user.uid)
            .putString(Constants.PREF_USER_NAME,   user.fullName)
            .putString(Constants.PREF_USER_ROLE,   user.role)
            .apply()
    }

    // ── Read ──────────────────────────────────────────────────────

    val userId:   String get() = prefs.getString(Constants.PREF_USER_ID,   "") ?: ""
    val userName: String get() = prefs.getString(Constants.PREF_USER_NAME,  "") ?: ""
    val userRole: String get() = prefs.getString(Constants.PREF_USER_ROLE,  "") ?: ""

    // isLoggedIn checks BOTH local prefs AND Firebase Auth token
    // so a cleared cache or expired session is handled correctly
    val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null && userId.isNotEmpty()

    val isAdmin:    Boolean get() = userRole == User.ROLE_ADMIN
    val isCustomer: Boolean get() = userRole == User.ROLE_CUSTOMER

    // ── Clear on logout ───────────────────────────────────────────

    fun clear() = prefs.edit().clear().apply()
}