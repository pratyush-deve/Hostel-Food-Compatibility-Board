package com.pratyush.hostelfood_compatibility_board.engine

import com.pratyush.hostelfood_compatibility_board.model.Dish
import com.pratyush.hostelfood_compatibility_board.model.EngineResult
import com.pratyush.hostelfood_compatibility_board.model.ExcludedDish
import com.pratyush.hostelfood_compatibility_board.model.Resident

/**
 * Pure Kotlin business logic engine for evaluating dish compatibility.
 * Zero dependencies on the Android SDK, enabling sub-second local JVM testing.
 */
object CompatibilityEngine {

    /**
     * Evaluates a list of dishes against a group of residents and a per-person budget.
     */
    fun evaluate(
        residents: List<Resident>,
        dishes: List<Dish>,
        budget: Int
    ): EngineResult {
        // 1. Validate inputs
        val validationError = validateInputs(residents, dishes, budget)
        if (validationError != null) return validationError

        val compatibleDishes = mutableListOf<Dish>()
        val excludedDishes = mutableListOf<ExcludedDish>()

        // 2. Evaluate each dish in declared source order
        for (dish in dishes) {
            val reasons = mutableListOf<String>()

            // A. Evaluate rules for each resident (in resident table order)
            for (resident in residents) {
                // Diet check must precede allergen check for this resident
                checkDiet(resident, dish)?.let { reasons.add(it) }

                // Allergen check follows dish's ingredient tag order
                reasons.addAll(checkAllergens(resident, dish))
            }

            // B. Budget check is always evaluated and emitted last
            checkBudget(dish, budget)?.let { reasons.add(it) }

            // C. Classification
            if (reasons.isEmpty()) {
                compatibleDishes.add(dish)
            } else {
                excludedDishes.add(ExcludedDish(dish = dish, reasons = reasons))
            }
        }

        return EngineResult.Success(
            compatibleDishes = compatibleDishes,
            excludedDishes = excludedDishes
        )
    }

    // =========================================================================
    // Isolated Rule Functions (Enables quick, safe live modifications)
    // =========================================================================

    /**
     * Checks if a dish satisfies a resident's dietary restrictions.
     * Contract: DIET:<resident>
     */
    fun checkDiet(resident: Resident, dish: Dish): String? {
        return if (!resident.diet.accepts(dish.diet)) {
            "DIET:${resident.name.trim()}"
        } else null
    }

    /**
     * Checks if any ingredient tag matches any allergen tag of the resident.
     * Normalized by trim and uppercase. Strictly authoritative (no aliases/inferences).
     * Order of results strictly follows the dish's ingredient tags declaration order.
     * Contract: ALLERGEN:<resident>:<tag>
     */
    fun checkAllergens(resident: Resident, dish: Dish): List<String> {
        val residentAllergens = resident.allergens
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() && it != "NONE" }
            .toSet()

        if (residentAllergens.isEmpty()) return emptyList()

        val matchedReasons = mutableListOf<String>()
        for (ingredient in dish.ingredientTags) {
            val normalizedIngredient = ingredient.trim().uppercase()
            if (normalizedIngredient in residentAllergens) {
                matchedReasons.add("ALLERGEN:${resident.name.trim()}:$normalizedIngredient")
            }
        }
        return matchedReasons
    }

    /**
     * Checks if the positive whole-rupee price exceeds the per-person group budget.
     * Boundary condition: price <= budget passes.
     * Contract: OVER_BUDGET
     */
    fun checkBudget(dish: Dish, budget: Int): String? {
        return if (dish.price > budget) "OVER_BUDGET" else null
    }

    // =========================================================================
    // Input Validation & Normalization
    // =========================================================================

    /**
     * Validates input bounds, non-empty strings, and ID uniqueness.
     * Returns Failure on invalid input or duplicate dish ID.
     */
    fun validateInputs(
        residents: List<Resident>,
        dishes: List<Dish>,
        budget: Int
    ): EngineResult.Failure? {
        // Validate Budget
        if (budget <= 0) {
            return EngineResult.Failure(
                errorCode = "INVALID_INPUT",
                message = "INVALID_INPUT: Budget, Global, Budget"
            )
        }

        // Validate Dishes
        val seenDishIds = mutableSetOf<String>()
        for (dish in dishes) {
            val trimmedId = dish.id.trim()
            if (trimmedId.isEmpty()) {
                return EngineResult.Failure(
                    errorCode = "INVALID_INPUT",
                    message = "INVALID_INPUT: Dishes, ${dish.id}, ID"
                )
            }

            // Uniqueness check
            if (!seenDishIds.add(trimmedId)) {
                return EngineResult.Failure(
                    errorCode = "DUPLICATE_DISH_ID",
                    message = "DUPLICATE_DISH_ID: $trimmedId"
                )
            }

            if (dish.cafe.trim().isEmpty()) {
                return EngineResult.Failure(
                    errorCode = "INVALID_INPUT",
                    message = "INVALID_INPUT: Dishes, ${dish.id}, Cafe"
                )
            }

            if (dish.name.trim().isEmpty()) {
                return EngineResult.Failure(
                    errorCode = "INVALID_INPUT",
                    message = "INVALID_INPUT: Dishes, ${dish.id}, Dish"
                )
            }

            if (dish.price <= 0) {
                return EngineResult.Failure(
                    errorCode = "INVALID_INPUT",
                    message = "INVALID_INPUT: Dishes, ${dish.id}, Price"
                )
            }

            if (dish.ingredientTags.isEmpty() || dish.ingredientTags.any { it.trim().isEmpty() }) {
                return EngineResult.Failure(
                    errorCode = "INVALID_INPUT",
                    message = "INVALID_INPUT: Dishes, ${dish.id}, Ingredients"
                )
            }
        }

        // Validate Residents
        for (resident in residents) {
            if (resident.name.trim().isEmpty()) {
                return EngineResult.Failure(
                    errorCode = "INVALID_INPUT",
                    message = "INVALID_INPUT: Residents, ${resident.name}, Name"
                )
            }
        }

        return null
    }
}
