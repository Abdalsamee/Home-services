package com.example.homeserv.data.model


data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val iconName: String = "",          // e.g. "ic_plumbing", used as fallback
    val providerCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0, 0L)

    companion object {
        const val COLLECTION = "categories"
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id"            to id,
        "name"          to name,
        "description"   to description,
        "imageUrl"      to imageUrl,
        "iconName"      to iconName,
        "providerCount" to providerCount,
        "createdAt"     to createdAt
    )
}