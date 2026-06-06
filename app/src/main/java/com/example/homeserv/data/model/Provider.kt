package com.example.homeserv.data.model

data class Provider(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pricePerHour: Double = 0.0,
    val rate: Double = 0.0,          // Changed to Double — Firestore stores numbers as Double
    val reviewCount: Int = 0,
    val jobsDone: Int = 0,
    val experienceYears: Int = 0,
    val isAvailable: Boolean = true,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        "", "", "", "", "", "", "", 0.0, 0.0,
        0.0, 0.0, 0, 0, 0, true, "", 0L
    )

    companion object {
        const val COLLECTION = "providers"
    }

    /** Formatted price string e.g. "$25.00 / hr" */
    fun formattedPrice(): String = "$%.2f / hr".format(pricePerHour)

    /** Formatted rating string e.g. "4.8" */
    fun formattedRate(): String = if (rate > 0) "%.1f".format(rate) else "0.0"

    /** Float rate for RatingBar */
    fun rateAsFloat(): Float = rate.toFloat().coerceIn(0f, 5f)

    fun toMap(): Map<String, Any> = mapOf(
        "id"               to id,
        "name"             to name,
        "imageUrl"         to imageUrl,
        "description"      to description,
        "categoryId"       to categoryId,
        "categoryName"     to categoryName,
        "address"          to address,
        "latitude"         to latitude,
        "longitude"        to longitude,
        "pricePerHour"     to pricePerHour,
        "rate"             to rate,
        "reviewCount"      to reviewCount,
        "jobsDone"         to jobsDone,
        "experienceYears"  to experienceYears,
        "isAvailable"      to isAvailable,
        "phone"            to phone,
        "createdAt"        to createdAt
    )
}