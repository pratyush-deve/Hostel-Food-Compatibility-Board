package com.pratyush.hostelfood_compatibility_board.model

/**
 * Result of the compatibility evaluation.
 */
sealed interface EngineResult {
    data class Success(
        val compatibleDishes: List<Dish>,
        val excludedDishes: List<ExcludedDish>
    ) : EngineResult

    data class Failure(
        val errorCode: String, // "INVALID_INPUT" or "DUPLICATE_DISH_ID"
        val message: String    // Exact formatted error string, e.g. "INVALID_INPUT: Dishes, D01, Price"
    ) : EngineResult
}
