package com.example.homeserv.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String = "",
    val role: String = ROLE_CUSTOMER,       // "customer" | "admin"
    val createdAt: Long = System.currentTimeMillis()
) {
    // Firestore requires a no-arg constructor
    constructor() : this("", "", "", "", "", ROLE_CUSTOMER, 0L)

    companion object {
        const val ROLE_CUSTOMER = "customer"
        const val ROLE_ADMIN    = "admin"

        const val COLLECTION = "users"
    }

    fun toMap(): Map<String, Any> = mapOf(
        "uid"             to uid,
        "fullName"        to fullName,
        "email"           to email,
        "phone"           to phone,
        "profileImageUrl" to profileImageUrl,
        "role"            to role,
        "createdAt"       to createdAt
    )
}