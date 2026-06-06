package com.example.homeserv.data.model



/**
 * Holds the current search query + active filters for the Search screen.
 */
data class SortFilter(
    val query: String = "",
    val categoryId: String = "",        // "" means all categories
    val sortBy: SortOption = SortOption.RATING,
    val onlyAvailable: Boolean = false,
    val maxPrice: Double = Double.MAX_VALUE
)

enum class SortOption(val label: String) {
    RATING("Top Rated"),
    PRICE_LOW("Price: Low to High"),
    PRICE_HIGH("Price: High to Low"),
    JOBS_DONE("Most Jobs Done")
}