# Acceptance Test Evidence & Interview Rehearsal Report
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Status**: 100% Verified (20/20 Tests Passing on Local JVM)  
**Date**: September 2026  

---

## 1. Executive Summary

Phase 4 completes the verification and preparation cycle for the Cisco AI-assisted coding interview. 
The entire test suite—spanning domain engine rules, state transitions, count decoupling, and all 6 required acceptance scenarios—runs automatically on the local JVM in **under 1.5 seconds**, providing definitive evidence of contract compliance.

```
Total Tests Executed: 20
Failures: 0
Skipped: 0
Success Rate: 100%
Execution Time: 1.497s
```

---

## 2. Test Suite Breakdown

| Test Suite Class | Tests Run | Duration | Result | Scope Covered |
| :--- | :---: | :---: | :---: | :--- |
| **`AcceptanceCriteriaTest`** | 6 | 0.124s | **PASSED** | The 6 exact acceptance scenarios from the problem PDF |
| **`CompatibilityEngineTest`** | 7 | 0.004s | **PASSED** | Normalization, isolated rules, sorting, edge cases |
| **`BoardViewModelTest`** | 6 | 0.007s | **PASSED** | StateFlow UDF, search count decoupling, reset & error clearing |
| **`ExampleUnitTest`** | 1 | 0.000s | **PASSED** | Sanity check |
| **Total** | **20** | **0.135s** | **100% PASS** | **Complete Project Coverage** |

---

## 3. The 6 Required Acceptance Tests (Execution Evidence)

### ✅ Test 1: Built-in Data Load & Count
- **Input Action**: Tap "Load Built-in" (executes `onLoadBuiltInAndCalculate()`).
- **Assertion**:
  - `isCalculated == true`
  - `totalCompatibleCount == 2`
  - `displayedCompatibleDishes == ["D01", "D02"]` in source order:
    1. D01: Lentil Rice Bowl (Hostel Cafe, ₹110)
    2. D02: Tomato Pasta (Library Cafe, ₹150)
- **Status**: **PASSED**

---

### ✅ Test 2: Contracted Exclusion Reasons & Boundary Price
- **Input Action**: Inspect `excludedDishes` output.
- **Assertion**:
  - Boundary-priced `D02` (₹150) passes the ₹150 budget.
  - `D03` reasons: `["DIET:Asha", "ALLERGEN:Mira:MILK"]`
  - `D04` reasons: `["ALLERGEN:Dev:PEANUT"]`
  - `D05` reasons: `["DIET:Asha", "DIET:Dev"]`
- **Status**: **PASSED** (Character-for-character match)

---

### ✅ Test 3: Deterministic Search Narrowing & Count Independence
- **Input Action**: Search query `"wheat"`.
- **Assertion**:
  - `displayedCompatibleDishes` narrows to only `["D02"]` (Tomato Pasta).
  - **`totalCompatibleCount` STAYS `2`** (overall count is decoupled from search filter).
- **Input Action**: Clear search query `""`.
  - `displayedCompatibleDishes` restores both `["D01", "D02"]`.
  - `totalCompatibleCount` remains `2`.
- **Status**: **PASSED**

---

### ✅ Test 4: Budget Boundary Shift
- **Input Action**: Change budget from ₹150 to ₹130.
- **Assertion**:
  - `totalCompatibleCount == 1`
  - `displayedCompatibleDishes == ["D01"]`
  - `D02` (₹150) moves to `excludedDishes` with exact reason `["OVER_BUDGET"]`.
- **Status**: **PASSED**

---

### ✅ Test 5: Invalid Input Handling & Output Clearing
- **Input Action**: Change D01 price to `0`.
- **Assertion**:
  - `errorMessage == "INVALID_INPUT: Dishes, D01, Price"`
  - `isCalculated == false`
  - `totalCompatibleCount == 0`
  - `displayedCompatibleDishes` is completely empty.
  - `excludedDishes` is completely empty.
- **Status**: **PASSED** (No stale results or counts remain visible)

---

### ✅ Test 6: Reset State Synchronization
- **Input Action**: Click "Reset" after invalid price scenario.
- **Assertion**:
  - Restores default residents (3) and dishes (5, D01 restored to ₹110).
  - Budget restored to `150`, search query restored to `""`.
  - `errorMessage == null`.
  - `isCalculated == false`, `totalCompatibleCount == 0`, compatible and excluded lists cleared until compatibility is run again.
- **Status**: **PASSED**

---

## 4. Live Modification Drills (Prepared for the Interviewer)

The interview format reserves 10–15 minutes for a live surprise modification. Three pre-rehearsed drills are ready:

```kotlin
// Drill 1: Add Dietary Restriction (e.g. Vegetarian cannot eat eggs)
// In CompatibilityEngine.kt -> checkDiet():
if (resident.diet == DietClass.VEGETARIAN && dish.ingredientTags.any { it.trim().equals("EGG", ignoreCase = true) }) {
    return "DIET:${resident.name.trim()}"
}

// Drill 2: Allergen Alias (e.g. DAIRY matches MILK or CHEESE)
// In CompatibilityEngine.kt -> checkAllergens():
val isDairyAllergy = residentAllergens.contains("DAIRY") && normalizedIngredient in listOf("MILK", "CHEESE")
if (normalizedIngredient in residentAllergens || isDairyAllergy) { ... }

// Drill 3: Hard Price Ceiling (e.g. Max price ₹180)
// In CompatibilityEngine.kt -> checkPriceCap():
fun checkPriceCap(dish: Dish): String? = if (dish.price > 180) "EXCEEDS_PRICE_CAP" else null
```

---

## 5. Verification Commands for the Terminal

Run the full 20-test acceptance suite:
```bash
./gradlew testDebugUnitTest
```

Run only the 6 required Cisco acceptance tests:
```bash
./gradlew testDebugUnitTest --tests "*.AcceptanceCriteriaTest"
```

Build the complete release/debug APK:
```bash
./gradlew assembleDebug
```
