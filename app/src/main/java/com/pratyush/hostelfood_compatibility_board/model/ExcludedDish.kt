package com.pratyush.hostelfood_compatibility_board.model

/**
 * Represents an excluded dish along with its contracted, ordered exclusion reasons.
 */
data class ExcludedDish(
    val dish: Dish,
    val reasons: List<String>
)
