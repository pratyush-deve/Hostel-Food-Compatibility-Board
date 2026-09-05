package com.pratyush.hostelfood_compatibility_board.viewmodel

import androidx.lifecycle.ViewModel
import com.pratyush.hostelfood_compatibility_board.data.SampleData
import com.pratyush.hostelfood_compatibility_board.engine.CompatibilityEngine
import com.pratyush.hostelfood_compatibility_board.model.Dish
import com.pratyush.hostelfood_compatibility_board.model.EngineResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing state and user intents for the Compatibility Board.
 * Connects pure CompatibilityEngine to UI state via StateFlow.
 * In-memory state holder with zero repository overhead.
 */
class BoardViewModel : ViewModel() {

    private val _state = MutableStateFlow(BoardScreenState())
    val state: StateFlow<BoardScreenState> = _state.asStateFlow()

    /**
     * Acceptance Criterion 1: Loads built-in group and dishes in one single action,
     * and calculates compatibility immediately.
     */
    fun onLoadBuiltInAndCalculate() {
        _state.value = BoardScreenState(
            residents = SampleData.defaultResidents(),
            dishes = SampleData.defaultDishes(),
            budget = SampleData.DEFAULT_BUDGET,
            searchQuery = "",
            isCalculated = false
        )
        calculateCompatibility()
    }

    /**
     * Triggers compatibility evaluation on the current data and budget.
     */
    fun onCalculateClicked() {
        calculateCompatibility()
    }

    /**
     * Acceptance Criterion 3: Narrows displayed compatible dishes by search query substring.
     * Case-insensitively checks cafe, dish name, or any ingredient tag.
     * CRITICAL CONTRACT: totalCompatibleCount remains based on the unfiltered compatible list.
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            val filtered = filterCompatibleDishes(current.allCompatibleDishes, query)
            current.copy(
                searchQuery = query,
                displayedCompatibleDishes = filtered
                // Notice: totalCompatibleCount remains completely untouched!
            )
        }
    }

    /**
     * Acceptance Criterion 4: Updates the per-person group budget and recalculates.
     */
    fun onBudgetChanged(newBudget: Int) {
        _state.update { it.copy(budget = newBudget) }
        calculateCompatibility()
    }

    /**
     * Updates an existing dish (e.g., changing price to test budget boundary or 0 for invalid input).
     */
    fun onUpdateDish(updatedDish: Dish) {
        _state.update { current ->
            val updatedList = current.dishes.map { if (it.id == updatedDish.id) updatedDish else it }
            current.copy(
                dishes = updatedList,
                selectedDishForEdit = null
            )
        }
        calculateCompatibility()
    }

    /**
     * Sets the dish to be edited in the modal dialog, or null to dismiss.
     */
    fun onSelectDishForEdit(dish: Dish?) {
        _state.update { it.copy(selectedDishForEdit = dish) }
    }

    /**
     * Acceptance Criterion 6: Reset contract:
     * Restores valid built-in group, dishes, ₹150 budget, and empty search,
     * then clears validation and calculated output until compatibility is run again.
     */
    fun onResetClicked() {
        _state.value = BoardScreenState(
            residents = SampleData.defaultResidents(),
            dishes = SampleData.defaultDishes(),
            budget = SampleData.DEFAULT_BUDGET,
            searchQuery = "",
            isCalculated = false,
            totalCompatibleCount = 0,
            allCompatibleDishes = emptyList(),
            displayedCompatibleDishes = emptyList(),
            excludedDishes = emptyList(),
            errorMessage = null,
            selectedDishForEdit = null
        )
    }

    /**
     * Performs compatibility evaluation and applies strict state clearing rules on failure.
     */
    private fun calculateCompatibility() {
        val current = _state.value
        when (val result = CompatibilityEngine.evaluate(current.residents, current.dishes, current.budget)) {
            is EngineResult.Success -> {
                val filtered = filterCompatibleDishes(result.compatibleDishes, current.searchQuery)
                _state.update {
                    it.copy(
                        isCalculated = true,
                        totalCompatibleCount = result.compatibleDishes.size,
                        allCompatibleDishes = result.compatibleDishes,
                        displayedCompatibleDishes = filtered,
                        excludedDishes = result.excludedDishes,
                        errorMessage = null
                    )
                }
            }
            is EngineResult.Failure -> {
                // Strict Output Clearing Contract:
                // Show no compatibility or exclusion rows, and clear earlier counts.
                _state.update {
                    it.copy(
                        isCalculated = false,
                        totalCompatibleCount = 0,
                        allCompatibleDishes = emptyList(),
                        displayedCompatibleDishes = emptyList(),
                        excludedDishes = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Case-insensitive substring match against Cafe, Dish Name, or any Ingredient Tag.
     */
    private fun filterCompatibleDishes(dishes: List<Dish>, query: String): List<Dish> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return dishes

        return dishes.filter { dish ->
            dish.cafe.contains(trimmedQuery, ignoreCase = true) ||
            dish.name.contains(trimmedQuery, ignoreCase = true) ||
            dish.ingredientTags.any { it.contains(trimmedQuery, ignoreCase = true) }
        }
    }
}
