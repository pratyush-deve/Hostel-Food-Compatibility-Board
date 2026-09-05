package com.pratyush.hostelfood_compatibility_board

import com.pratyush.hostelfood_compatibility_board.data.SampleData
import com.pratyush.hostelfood_compatibility_board.engine.CompatibilityEngine
import com.pratyush.hostelfood_compatibility_board.model.DietClass
import com.pratyush.hostelfood_compatibility_board.model.Dish
import com.pratyush.hostelfood_compatibility_board.model.EngineResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM Unit test suite verifying CompatibilityEngine against all Cisco problem statement contracts.
 * Executes on the local JVM in under 1 second without an emulator.
 */
class CompatibilityEngineTest {

    // =========================================================================
    // Acceptance Test 1 & 2: Built-in Dataset, Exclusions & Boundary Price
    // =========================================================================

    @Test
    fun testBuiltInDatasetCompatibility() {
        val residents = SampleData.defaultResidents()
        val dishes = SampleData.defaultDishes()
        val budget = SampleData.DEFAULT_BUDGET // 150

        val result = CompatibilityEngine.evaluate(residents, dishes, budget)

        assertTrue("Evaluation should succeed", result is EngineResult.Success)
        val success = result as EngineResult.Success

        // 1. Exactly two compatible dishes: D01 followed by D02
        assertEquals(2, success.compatibleDishes.size)
        assertEquals("D01", success.compatibleDishes[0].id)
        assertEquals("D02", success.compatibleDishes[1].id)

        // 2. Boundary test: D02 (priced at 150) passes budget of 150
        assertEquals(150, success.compatibleDishes[1].price)

        // 3. Excluded dishes count
        assertEquals(3, success.excludedDishes.size)

        // 4. Verify exact contracted exclusion reasons
        val d03Exclusion = success.excludedDishes.first { it.dish.id == "D03" }
        assertEquals(listOf("DIET:Asha", "ALLERGEN:Mira:MILK"), d03Exclusion.reasons)

        val d04Exclusion = success.excludedDishes.first { it.dish.id == "D04" }
        assertEquals(listOf("ALLERGEN:Dev:PEANUT"), d04Exclusion.reasons)

        val d05Exclusion = success.excludedDishes.first { it.dish.id == "D05" }
        assertEquals(listOf("DIET:Asha", "DIET:Dev"), d05Exclusion.reasons)
    }

    // =========================================================================
    // Acceptance Test 4: Budget Boundary Shift (130)
    // =========================================================================

    @Test
    fun testBudgetBoundaryExclusion() {
        val residents = SampleData.defaultResidents()
        val dishes = SampleData.defaultDishes()
        val budget = 130 // Changed budget

        val result = CompatibilityEngine.evaluate(residents, dishes, budget)

        assertTrue("Evaluation should succeed", result is EngineResult.Success)
        val success = result as EngineResult.Success

        // Only D01 (₹110) is compatible
        assertEquals(1, success.compatibleDishes.size)
        assertEquals("D01", success.compatibleDishes[0].id)

        // D02 (₹150) is now excluded with OVER_BUDGET
        val d02Exclusion = success.excludedDishes.first { it.dish.id == "D02" }
        assertEquals(listOf("OVER_BUDGET"), d02Exclusion.reasons)
    }

    // =========================================================================
    // Acceptance Test 5: Input Validation & Error Formatting
    // =========================================================================

    @Test
    fun testInvalidPriceValidation() {
        val residents = SampleData.defaultResidents()
        // Change D01 price to 0
        val dishes = SampleData.defaultDishes().map {
            if (it.id == "D01") it.copy(price = 0) else it
        }
        val budget = 150

        val result = CompatibilityEngine.evaluate(residents, dishes, budget)

        assertTrue("Evaluation should fail on price 0", result is EngineResult.Failure)
        val failure = result as EngineResult.Failure

        assertEquals("INVALID_INPUT", failure.errorCode)
        assertEquals("INVALID_INPUT: Dishes, D01, Price", failure.message)
    }

    @Test
    fun testInvalidBudgetValidation() {
        val residents = SampleData.defaultResidents()
        val dishes = SampleData.defaultDishes()

        val resultZero = CompatibilityEngine.evaluate(residents, dishes, 0)
        assertTrue(resultZero is EngineResult.Failure)
        assertEquals("INVALID_INPUT: Budget, Global, Budget", (resultZero as EngineResult.Failure).message)

        val resultNegative = CompatibilityEngine.evaluate(residents, dishes, -50)
        assertTrue(resultNegative is EngineResult.Failure)
        assertEquals("INVALID_INPUT: Budget, Global, Budget", (resultNegative as EngineResult.Failure).message)
    }

    @Test
    fun testDuplicateDishIdValidation() {
        val residents = SampleData.defaultResidents()
        // Add a duplicate D01 dish
        val dishes = SampleData.defaultDishes() + Dish(
            id = "D01",
            cafe = "East Cafe",
            name = "Duplicate Lentil Bowl",
            diet = DietClass.VEGAN,
            ingredientTags = listOf("LENTIL"),
            price = 100
        )
        val budget = 150

        val result = CompatibilityEngine.evaluate(residents, dishes, budget)

        assertTrue("Evaluation should fail on duplicate dish ID", result is EngineResult.Failure)
        val failure = result as EngineResult.Failure

        assertEquals("DUPLICATE_DISH_ID", failure.errorCode)
        assertEquals("DUPLICATE_DISH_ID: D01", failure.message)
    }

    // =========================================================================
    // Edge Cases: Tag Normalization & Strict Authoritative Matching
    // =========================================================================

    @Test
    fun testTagNormalizationAndWhitespace() {
        val residents = listOf(
            SampleData.defaultResidents()[0], // Asha: VEGAN, no allergens
            SampleData.defaultResidents()[1].copy(allergens = listOf("  peanut  ")), // lowercase + spaces
            SampleData.defaultResidents()[2]  // Mira: MILK
        )

        val dishes = listOf(
            Dish(
                id = "D10",
                cafe = "North Cafe",
                name = "Nutty Salad",
                diet = DietClass.VEGAN,
                ingredientTags = listOf(" lettuce ", "PEANUT"),
                price = 120
            )
        )

        val result = CompatibilityEngine.evaluate(residents, dishes, 150)
        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success

        assertEquals(0, success.compatibleDishes.size)
        assertEquals(1, success.excludedDishes.size)
        assertEquals(listOf("ALLERGEN:Dev:PEANUT"), success.excludedDishes[0].reasons)
    }

    @Test
    fun testReasonOrderingOrder() {
        // A dish failing Diet for Asha, Allergen for Dev, Allergen for Mira, and Over Budget
        val residents = SampleData.defaultResidents()
        val badDish = Dish(
            id = "DX",
            cafe = "Fusion Cafe",
            name = "Mega Dish",
            diet = DietClass.NON_VEGETARIAN, // Fails Asha (VEGAN) and Dev (VEGETARIAN)
            ingredientTags = listOf("PEANUT", "MILK"), // Fails Dev (PEANUT) and Mira (MILK)
            price = 200 // Exceeds budget 150
        )

        val result = CompatibilityEngine.evaluate(residents, listOf(badDish), 150)
        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success

        val reasons = success.excludedDishes[0].reasons
        // Expected order:
        // 1. Asha: DIET:Asha
        // 2. Dev: DIET:Dev, then ALLERGEN:Dev:PEANUT
        // 3. Mira: ALLERGEN:Mira:MILK
        // 4. OVER_BUDGET last
        val expected = listOf(
            "DIET:Asha",
            "DIET:Dev",
            "ALLERGEN:Dev:PEANUT",
            "ALLERGEN:Mira:MILK",
            "OVER_BUDGET"
        )
        assertEquals(expected, reasons)
    }
}
