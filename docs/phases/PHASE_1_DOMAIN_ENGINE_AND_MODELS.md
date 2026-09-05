# Phase 1: Domain Models & Pure Kotlin Engine
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement\Md files\phases`  
**Date**: September 2026  

---

## 1. Goal

Build the complete domain model definitions, built-in datasets, input validation, string normalization, isolated compatibility rules (`checkDiet`, `checkAllergens`, `checkBudget`), and deterministic failure reason sorting in **pure Kotlin** (zero Android SDK dependencies), backed by a sub-second JVM JUnit test suite.

---

## 2. Main Tasks

### Task 1.1: Data Models Definition (`model/`)
- Create `DietClass.kt` enum with values `VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`, and `NO_RESTRICTION` (*resident only*). Implement `accepts(dishDiet: DietClass): Boolean` logic.
- Create immutable data classes:
  - `Resident(val name: String, val diet: DietClass, val allergens: List<String>)`
  - `Dish(val id: String, val cafe: String, val name: String, val diet: DietClass, val ingredientTags: List<String>, val price: Int)`
  - `ExcludedDish(val dish: Dish, val reasons: List<String>)`
- Create `EngineResult.kt` sealed interface (`Success` with lists vs `Failure` with `errorCode` and `message`).

### Task 1.2: Built-in Datasets (`data/SampleData.kt`)
- Create `SampleData` singleton with:
  - `DEFAULT_BUDGET = 150`
  - `defaultResidents()`: Asha (VEGAN, empty allergens list for `"none"`), Dev (VEGETARIAN, `["PEANUT"]`), Mira (NO_RESTRICTION, `["MILK"]`).
  - `defaultDishes()` in exact source order: D01 (₹110), D02 (₹150), D03 (₹140), D04 (₹130), D05 (₹100).

### Task 1.3: Pure Kotlin Compatibility Engine (`engine/CompatibilityEngine.kt`)
- **String Normalization**: Trim and convert all diet classes, ingredient tags, and allergen tags to uppercase.
- **Input Validation**:
  - Positive integer checks ($> 0$) on budget and dish prices.
  - Non-empty strings on names, IDs, cafes, ingredients.
  - Unique dish IDs check across the dish list.
  - Return exact formats: `INVALID_INPUT: <Table>, <Row/ID>, <Field>` or `DUPLICATE_DISH_ID: <ID>`.
- **Isolated Modular Rule Functions**:
  - `checkDiet(resident, dish): String?` $\to$ Returns `"DIET:${resident.name}"` or `null`.
  - `checkAllergens(resident, dish): List<String>` $\to$ Strict literal exact match; returns list of `"ALLERGEN:${resident.name}:$normalizedTag"`.
  - `checkBudget(dish, budget): String?` $\to$ Returns `"OVER_BUDGET"` if `dish.price > budget` or `null`.
- **Deterministic Reason Sorter**:
  - Group reasons by resident order (Asha $\to$ Dev $\to$ Mira).
  - Place `DIET` before `ALLERGEN` per resident.
  - Order allergens according to the dish's original ingredient declaration order.
  - Append `OVER_BUDGET` at the very end.
- **Source Order Preservation**: Retain exact dish table source order for compatible dishes.

### Task 1.4: JVM Unit Test Suite (`test/.../CompatibilityEngineTest.kt`)
- Implement JUnit tests testing all rules, boundary conditions, and exact string matches:
  - Built-in run matches D01 and D02.
  - Boundary test: D02 (₹150) passes ₹150 budget.
  - Exclusion reasons match D03, D04, D05 character-for-character.
  - Budget change to 130 marks D02 as `OVER_BUDGET`.
  - Price 0 triggers `INVALID_INPUT: Dishes, D01, Price`.
  - Duplicate ID triggers `DUPLICATE_DISH_ID: D01`.

---

## 3. Checkpoint & Verification Before Moving to Phase 2

Run the JVM test suite directly in terminal or Android Studio:
```bash
./gradlew testDebugUnitTest
```

### Success Criteria:
- [x] All JUnit tests pass in **<1.5 seconds**.
- [x] Exact contracted failure strings verified:
  - `D03` reasons: `["DIET:Asha", "ALLERGEN:Mira:MILK"]`
  - `D04` reasons: `["ALLERGEN:Dev:PEANUT"]`
  - `D05` reasons: `["DIET:Asha", "DIET:Dev"]`
- [x] Zero Android SDK dependencies in `engine/` or `model/`.
