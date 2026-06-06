package com.example.homeserv.data.model

data class ServiceRequest(
    val id: String = "",
    // Customer info
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    // Provider info
    val providerId: String = "",
    val providerName: String = "",
    val providerImageUrl: String = "",
    val categoryName: String = "",
    // Booking details
    val address: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val notes: String = "",
    val estimatedHours: Int = 1,
    val pricePerHour: Double = 0.0,
    // Status & rating
    val status: String = STATUS_PENDING,
    val rating: Float = 0f,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    // Firestore no-arg constructor
    constructor() : this(
        "", "", "", "", "", "", "", "",
        "", "", "", "", 1, 0.0,
        STATUS_PENDING, 0f, 0L, 0L
    )

    companion object {
        const val COLLECTION = "requests"

        const val STATUS_PENDING     = "pending"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED   = "completed"
        const val STATUS_CANCELLED   = "cancelled"
    }

    val totalPrice: Double get() = pricePerHour * estimatedHours
    fun formattedTotal(): String = "$%.2f".format(totalPrice)

    val isRated: Boolean get() = rating > 0f

    fun toMap(): Map<String, Any> = mapOf(
        "id"               to id,
        "customerId"       to customerId,
        "customerName"     to customerName,
        "customerPhone"    to customerPhone,
        "providerId"       to providerId,
        "providerName"     to providerName,
        "providerImageUrl" to providerImageUrl,
        "categoryName"     to categoryName,
        "address"          to address,
        "scheduledDate"    to scheduledDate,
        "scheduledTime"    to scheduledTime,
        "notes"            to notes,
        "estimatedHours"   to estimatedHours,
        "pricePerHour"     to pricePerHour,
        "status"           to status,
        "rating"           to rating,
        "createdAt"        to createdAt,
        "updatedAt"        to updatedAt
    )
}