package com.pratyush.hostelfood_compatibility_board.model

/**
 * Valid dietary classifications.
 * NO_RESTRICTION is only valid for residents.
 */
enum class DietClass {
    VEGAN,
    VEGETARIAN,
    NON_VEGETARIAN,
    NO_RESTRICTION;

    /**
     * Checks if this resident dietary preference accepts a dish of [dishDiet].
     */
    fun accepts(dishDiet: DietClass): Boolean = when (this) {
        VEGAN -> dishDiet == VEGAN
        VEGETARIAN -> dishDiet == VEGAN || dishDiet == VEGETARIAN
        NON_VEGETARIAN -> true
        NO_RESTRICTION -> true
    }
}
