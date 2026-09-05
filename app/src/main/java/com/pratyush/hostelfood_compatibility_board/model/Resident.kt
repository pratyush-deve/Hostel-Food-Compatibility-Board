package com.pratyush.hostelfood_compatibility_board.model

/**
 * Represents a hostel resident with dietary preference and allergen exclusions.
 */
data class Resident(
    val name: String,
    val diet: DietClass,
    val allergens: List<String> = emptyList()
)
