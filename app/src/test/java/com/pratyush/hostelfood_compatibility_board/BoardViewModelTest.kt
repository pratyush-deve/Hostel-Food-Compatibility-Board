package com.pratyush.hostelfood_compatibility_board

import com.pratyush.hostelfood_compatibility_board.viewmodel.BoardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JVM Unit test suite verifying BoardViewModel and state management.
 * Verifies unidirectional data flow, search count decoupling, reset contract, and error clearing.
 */
class BoardViewModelTest {

    private lateinit var viewModel: BoardViewModel

    @Before
    fun setUp() {
        viewModel = BoardViewModel()
    }

    @Test
    fun testDefaultState() {
        val state = viewModel.state.value

        assertEquals(3, state.residents.size)
        assertEquals(5, state.dishes.size)
        assertEquals(150, state.budget)
        assertEquals("", state.searchQuery)
        assertFalse(state.isCalculated)
        assertEquals(0, state.totalCompatibleCount)
        assertTrue(state.allCompatibleDishes.isEmpty())
        assertTrue(state.displayedCompatibleDishes.isEmpty())
        assertTrue(state.excludedDishes.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun testLoadBuiltInAndCalculate() {
        // Acceptance Criterion 1: Load built-in group and dishes in one single action
        viewModel.onLoadBuiltInAndCalculate()

        val state = viewModel.state.value
        assertTrue(state.isCalculated)
        assertNull(state.errorMessage)
        assertEquals(2, state.totalCompatibleCount)
        assertEquals(2, state.displayedCompatibleDishes.size)
        assertEquals("D01", state.displayedCompatibleDishes[0].id)
        assertEquals("D02", state.displayedCompatibleDishes[1].id)
        assertEquals(3, state.excludedDishes.size)
    }

    @Test
    fun testSearchNarrowingDecoupledFromTotalCount() {
        // Load default state
        viewModel.onLoadBuiltInAndCalculate()
        assertEquals(2, viewModel.state.value.totalCompatibleCount)

        // Acceptance Criterion 3: Search "wheat"
        viewModel.onSearchQueryChanged("wheat")

        val stateWithSearch = viewModel.state.value
        // Only D02 contains wheat (in ingredient tags)
        assertEquals(1, stateWithSearch.displayedCompatibleDishes.size)
        assertEquals("D02", stateWithSearch.displayedCompatibleDishes[0].id)

        // CRITICAL CONTRACT: Overall compatible count STAYS 2
        assertEquals(2, stateWithSearch.totalCompatibleCount)

        // Clear query -> both D01 and D02 display again
        viewModel.onSearchQueryChanged("")
        val stateCleared = viewModel.state.value
        assertEquals(2, stateCleared.displayedCompatibleDishes.size)
        assertEquals(2, stateCleared.totalCompatibleCount)
    }

    @Test
    fun testBudgetBoundaryShift() {
        viewModel.onLoadBuiltInAndCalculate()

        // Acceptance Criterion 4: Change budget to 130
        viewModel.onBudgetChanged(130)

        val state = viewModel.state.value
        assertEquals(130, state.budget)
        assertEquals(1, state.totalCompatibleCount)
        assertEquals(1, state.displayedCompatibleDishes.size)
        assertEquals("D01", state.displayedCompatibleDishes[0].id)

        // D02 is now in excluded with OVER_BUDGET
        val d02Exclusion = state.excludedDishes.first { it.dish.id == "D02" }
        assertEquals(listOf("OVER_BUDGET"), d02Exclusion.reasons)
    }

    @Test
    fun testInvalidPriceClearsResultsAndCounts() {
        viewModel.onLoadBuiltInAndCalculate()
        assertTrue(viewModel.state.value.isCalculated)

        // Acceptance Criterion 5: Change D01 price to 0
        val d01 = viewModel.state.value.dishes.first { it.id == "D01" }
        viewModel.onUpdateDish(d01.copy(price = 0))

        val state = viewModel.state.value
        // Contract: report INVALID_INPUT, show no compatibility or exclusion rows, clear earlier counts
        assertEquals("INVALID_INPUT: Dishes, D01, Price", state.errorMessage)
        assertFalse(state.isCalculated)
        assertEquals(0, state.totalCompatibleCount)
        assertTrue(state.displayedCompatibleDishes.isEmpty())
        assertTrue(state.excludedDishes.isEmpty())
    }

    @Test
    fun testResetContractClearsResults() {
        viewModel.onLoadBuiltInAndCalculate()
        assertTrue(viewModel.state.value.isCalculated)
        assertEquals(2, viewModel.state.value.totalCompatibleCount)

        // Acceptance Criterion 6: Reset restores defaults and clears validation/calculated output
        viewModel.onResetClicked()

        val state = viewModel.state.value
        assertFalse(state.isCalculated)
        assertEquals(0, state.totalCompatibleCount)
        assertTrue(state.displayedCompatibleDishes.isEmpty())
        assertTrue(state.excludedDishes.isEmpty())
        assertNull(state.errorMessage)
        assertEquals("", state.searchQuery)
        assertEquals(150, state.budget)
    }
}
