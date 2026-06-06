package com.example.homeserv.utils


object Constants {

    // ── Firestore Collections ─────────────────────────────────────
    const val COLLECTION_USERS     = "users"
    const val COLLECTION_PROVIDERS = "providers"
    const val COLLECTION_CATEGORIES= "categories"
    const val COLLECTION_REQUESTS  = "requests"

    // ── Storage Paths ─────────────────────────────────────────────
    const val STORAGE_PROVIDERS  = "providers"
    const val STORAGE_CATEGORIES = "categories"
    const val STORAGE_PROFILES   = "profiles"

    // ── SharedPreferences ─────────────────────────────────────────
    const val PREFS_NAME      = "homeserv_prefs"
    const val PREF_USER_ROLE  = "user_role"
    const val PREF_USER_ID    = "user_id"
    const val PREF_USER_NAME  = "user_name"

    // ── Intent / Bundle Keys ──────────────────────────────────────
    const val KEY_PROVIDER_ID   = "provider_id"
    const val KEY_CATEGORY_ID   = "category_id"
    const val KEY_CATEGORY_NAME = "category_name"
    const val KEY_REQUEST_ID    = "request_id"
    const val KEY_PROVIDER       = "provider"
    const val KEY_CATEGORY       = "category"
    const val KEY_FROM_ADMIN     = "from_admin"

    // ── User Roles ────────────────────────────────────────────────
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_ADMIN    = "admin"

    // ── Request Status ────────────────────────────────────────────
    const val STATUS_PENDING     = "pending"
    const val STATUS_IN_PROGRESS = "in_progress"
    const val STATUS_COMPLETED   = "completed"
    const val STATUS_CANCELLED   = "cancelled"

    // ── Map ───────────────────────────────────────────────────────
    const val DEFAULT_ZOOM       = 14f
    const val DEFAULT_LAT        = 31.9539 // Fallback: Amman, Jordan
    const val DEFAULT_LNG        = 35.9106

    // ── Permissions Request Codes ─────────────────────────────────
    const val REQUEST_LOCATION_PERMISSION = 1001
    const val REQUEST_IMAGE_PICK          = 1002
    const val REQUEST_CAMERA              = 1003

    // ── Animation Durations ───────────────────────────────────────
    const val ANIM_DURATION_SHORT  = 150L
    const val ANIM_DURATION_MEDIUM = 300L
    const val ANIM_DURATION_LONG   = 500L

    // ── Splash delay ─────────────────────────────────────────────
    const val SPLASH_DELAY = 2000L
}