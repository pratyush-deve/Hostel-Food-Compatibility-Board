package com.pratyush.hostelfood_compatibility_board.viewmodel

import com.pratyush.hostelfood_compatibility_board.data.SampleData
import com.pratyush.hostelfood_compatibility_board.model.Dish
import com.pratyush.hostelfood_compatibility_board.model.ExcludedDish
import com.pratyush.hostelfood_compatibility_board.model.Resident

/**
 * Unified immutable UI state for the Hostel Food Compatibility Board screen.
 */
data class BoardScreenState(
    val residents: List<Resident> = SampleData.defaultResidents(),
    val dishes: List<Dish> = SampleData.defaultDishes(),
    val budget: Int = SampleData.DEFAULT_BUDGET,
    val searchQuery: String = "",
    val isCalculated: Boolean = false,
    val totalCompatibleCount: Int = 0,
    val allCompatibleDishes: List<Dish> = emptyList(),
    val displayedCompatibleDishes: List<Dish> = emptyList(),
    val excludedDishes: List<ExcludedDish> = emptyList(),
    val errorMessage: String? = null,
    val selectedDishForEdit: Dish? = null
)
