package com.pratyush.hostelfood_compatibility_board

import com.pratyush.hostelfood_compatibility_board.viewmodel.BoardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-End Acceptance Test Suite strictly validating the 6 required scenarios
 * and the optional evidence chips from the Cisco Problem Statement (SI26_P02).
 *
 * Runs on the local JVM in under 1 second.
 */
class AcceptanceCriteriaTest {

    private lateinit var viewModel: BoardViewModel

    @Before
    fun setup() {
        viewModel = BoardViewModel()
    }

    /**
     * Required Acceptance Test 1:
     * "Load the built-in group and dishes in one action; show exactly two
     * compatible dishes, D01 followed by D02, with an overall compatible count of 2."
     */
    @Test
    fun testAcceptanceCriterion1_BuiltInLoadAndCount() {
        // One action load and calculate
        viewModel.onLoadBuiltInAndCalculate()
        val state = viewModel.state.value

        assertTrue("Calculation must be active", state.isCalculated)
        assertNull("No error should be present", state.errorMessage)

        // Overall compatible count must be exactly 2
        assertEquals("Overall compatible count must be 2", 2, state.totalCompatibleCount)

        // Exactly two compatible dishes in source order: D01 followed by D02
        assertEquals(2, state.displayedCompatibleDishes.size)
        assertEquals("D01", state.displayedCompatibleDishes[0].id)
        assertEquals("Lentil Rice Bowl", state.displayedCompatibleDishes[0].name)
        assertEquals("D02", state.displayedCompatibleDishes[1].id)
        assertEquals("Tomato Pasta", state.displayedCompatibleDishes[1].name)
    }

    /**
     * Required Acceptance Test 2:
     * "Show the contracted exclusion reasons for D03, D04, and D05, and show
     * that the boundary-priced D02 passes the ₹150 budget."
     */
    @Test
    fun testAcceptanceCriterion2_ContractedExclusionsAndBoundary() {
        viewModel.onLoadBuiltInAndCalculate()
        val state = viewModel.state.value

        // Boundary test: D02 is priced at exactly ₹150 and passes budget of ₹150
        val d02 = state.displayedCompatibleDishes.find { it.id == "D02" }
        assertNotNull("D02 must be compatible at budget ₹150", d02)
        assertEquals(150, d02?.price)

        // Contracted reasons for excluded dishes
        val d03 = state.excludedDishes.find { it.dish.id == "D03" }
        assertNotNull("D03 must be excluded", d03)
        assertEquals(
            "D03 exclusion reasons must match contract character-for-character",
            listOf("DIET:Asha", "ALLERGEN:Mira:MILK"),
            d03?.reasons
        )

        val d04 = state.excludedDishes.find { it.dish.id == "D04" }
        assertNotNull("D04 must be excluded", d04)
        assertEquals(
            "D04 exclusion reasons must match contract character-for-character",
            listOf("ALLERGEN:Dev:PEANUT"),
            d04?.reasons
        )

        val d05 = state.excludedDishes.find { it.dish.id == "D05" }
        assertNotNull("D05 must be excluded", d05)
        assertEquals(
            "D05 exclusion reasons must match contract character-for-character",
            listOf("DIET:Asha", "DIET:Dev"),
            d05?.reasons
        )
    }

    /**
     * Required Acceptance Test 3:
     * "Enter the exact query wheat; display only D02 while keeping the overall
     * compatible count at 2, then clear the query and display both compatible dishes again."
     */
    @Test
    fun testAcceptanceCriterion3_SearchNarrowingAndCountDecoupling() {
        viewModel.onLoadBuiltInAndCalculate()

        // 1. Enter exact query "wheat"
        viewModel.onSearchQueryChanged("wheat")
        var state = viewModel.state.value

        // Display narrows to only D02 (which has WHEAT in ingredient tags)
        assertEquals("Displayed dishes should contain only D02", 1, state.displayedCompatibleDishes.size)
        assertEquals("D02", state.displayedCompatibleDishes[0].id)

        // CRITICAL CONTRACT: Overall compatible count must stay 2!
        assertEquals("Overall compatible count must remain 2 during search", 2, state.totalCompatibleCount)

        // 2. Clear query
        viewModel.onSearchQueryChanged("")
        state = viewModel.state.value

        // Displays both compatible dishes again
        assertEquals("Clearing search must restore both compatible dishes", 2, state.displayedCompatibleDishes.size)
        assertEquals("D01", state.displayedCompatibleDishes[0].id)
        assertEquals("D02", state.displayedCompatibleDishes[1].id)
        assertEquals(2, state.totalCompatibleCount)
    }

    /**
     * Required Acceptance Test 4:
     * "Change the group budget to ₹130; show only D01 as compatible and
     * identify D02 as OVER_BUDGET."
     */
    @Test
    fun testAcceptanceCriterion4_BudgetBoundaryShift() {
        viewModel.onLoadBuiltInAndCalculate()

        // Change budget to ₹130
        viewModel.onBudgetChanged(130)
        val state = viewModel.state.value

        assertEquals(130, state.budget)

        // Show only D01 as compatible
        assertEquals("Only D01 should be compatible", 1, state.totalCompatibleCount)
        assertEquals(1, state.displayedCompatibleDishes.size)
        assertEquals("D01", state.displayedCompatibleDishes[0].id)

        // Identify D02 as OVER_BUDGET
        val d02 = state.excludedDishes.find { it.dish.id == "D02" }
        assertNotNull("D02 must now be in excluded list", d02)
        assertEquals(
            "D02 must have OVER_BUDGET as exact failure reason",
            listOf("OVER_BUDGET"),
            d02?.reasons
        )
    }

    /**
     * Required Acceptance Test 5:
     * "Change D01 price to 0; report INVALID_INPUT naming the D01 row and
     * price field, and clear all result rows and counts from the previous calculation."
     */
    @Test
    fun testAcceptanceCriterion5_InvalidPriceHandlingAndClearing() {
        viewModel.onLoadBuiltInAndCalculate()
        assertTrue("Precondition: Calculation should be active", viewModel.state.value.isCalculated)

        // Change D01 price to 0
        val d01 = viewModel.state.value.dishes.first { it.id == "D01" }
        viewModel.onUpdateDish(d01.copy(price = 0))
        val state = viewModel.state.value

        // Contract: Report INVALID_INPUT naming D01 row and price field
        assertEquals(
            "Error message must name the D01 row and price field",
            "INVALID_INPUT: Dishes, D01, Price",
            state.errorMessage
        )

        // Contract: Show no compatibility or exclusion rows, and clear earlier counts
        assertFalse("Calculation active state must be false", state.isCalculated)
        assertEquals("Compatible count must be cleared to 0", 0, state.totalCompatibleCount)
        assertTrue("Compatible list must be cleared", state.displayedCompatibleDishes.isEmpty())
        assertTrue("Excluded list must be cleared", state.excludedDishes.isEmpty())
    }

    /**
     * Required Acceptance Test 6:
     * "After the invalid-price case, reset and confirm that the valid built-in rows,
     * ₹150 budget, and empty search return with no stale error or result; keep the screen
     * synchronized and include focused checks for the built-in result, deterministic
     * search narrowing, budget boundary, invalid price, and reset."
     */
    @Test
    fun testAcceptanceCriterion6_ResetStateSynchronization() {
        // Step A: Trigger invalid price error first
        viewModel.onLoadBuiltInAndCalculate()
        val d01 = viewModel.state.value.dishes.first { it.id == "D01" }
        viewModel.onUpdateDish(d01.copy(price = 0))
        assertEquals("INVALID_INPUT: Dishes, D01, Price", viewModel.state.value.errorMessage)

        // Step B: Reset
        viewModel.onResetClicked()
        val state = viewModel.state.value

        // Confirm valid built-in rows restored
        assertEquals(3, state.residents.size)
        assertEquals(5, state.dishes.size)
        assertEquals("D01 price must be restored to 110", 110, state.dishes.first { it.id == "D01" }.price)

        // Confirm ₹150 budget and empty search
        assertEquals(150, state.budget)
        assertEquals("", state.searchQuery)

        // Confirm no stale error or result
        assertNull("Error must be cleared on reset", state.errorMessage)
        assertFalse("Calculation output must be cleared until run again", state.isCalculated)
        assertEquals("Count must be 0", 0, state.totalCompatibleCount)
        assertTrue("Compatible rows must be empty", state.displayedCompatibleDishes.isEmpty())
        assertTrue("Excluded rows must be empty", state.excludedDishes.isEmpty())
    }
}
