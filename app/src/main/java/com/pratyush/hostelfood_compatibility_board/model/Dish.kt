package com.pratyush.hostelfood_compatibility_board.model

/**
 * Represents a dish offered by a campus cafe.
 */
data class Dish(
    val id: String,
    val cafe: String,
    val name: String,
    val diet: DietClass,
    val ingredientTags: List<String>,
    val price: Int
)
