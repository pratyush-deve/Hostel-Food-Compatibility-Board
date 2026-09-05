# Phase 2: ViewModel & State Management
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement\Md files\phases`  
**Date**: September 2026  

---

## 1. Goal

Connect the pure Kotlin domain engine to the presentation layer via an Android `ViewModel` using `StateFlow`, implementing unidirectional data flow, search query narrowing with count decoupling, and strict contract adherence for Reset and error states.

---

## 2. Main Tasks

### Task 2.1: Unified Immutable UI State (`viewmodel/BoardScreenState.kt`)
- Define a single immutable data class `BoardScreenState` containing:
  - `residents: List<Resident>` (Defaults to `SampleData.defaultResidents()`)
  - `dishes: List<Dish>` (Defaults to `SampleData.defaultDishes()`)
  - `budget: Int` (Defaults to `SampleData.DEFAULT_BUDGET`)
  - `searchQuery: String` (Defaults to `""`)
  - `isCalculated: Boolean` (False on initial launch and after Reset)
  - `totalCompatibleCount: Int` (Unfiltered count of compatible dishes)
  - `allCompatibleDishes: List<Dish>` (Master compatible list)
  - `displayedCompatibleDishes: List<Dish>` (Filtered view for search narrowing)
  - `excludedDishes: List<ExcludedDish>` (Dishes with reasons)
  - `errorMessage: String?` (Populated on validation failure, null otherwise)
  - `selectedDishForEdit: Dish?` (Controls Edit Dialog state)

### Task 2.2: Implement `BoardViewModel.kt`
- Expose `state: StateFlow<BoardScreenState> = _state.asStateFlow()`.
- Implement user event handlers:
  - **`onLoadBuiltInAndCalculate()`**: Restores default datasets and immediately triggers compatibility in a single action (fulfills Acceptance Criterion 1).
  - **`onCalculateClicked()`**: Evaluates the current state's data against the engine.
  - **`onSearchQueryChanged(query: String)`**:
    - Updates `searchQuery`.
    - Filters `displayedCompatibleDishes` using case-insensitive substring match against Cafe name, Dish name, or Ingredient tags.
    - **Crucial Rule**: Does NOT mutate `totalCompatibleCount` (badge remains based on `allCompatibleDishes.size`).
  - **`onBudgetChanged(newBudget: Int)`**: Updates budget and triggers recalculation.
  - **`onUpdateDish(updatedDish: Dish)`**: Updates dish in the in-memory list and triggers recalculation.
  - **`onSelectDishForEdit(dish: Dish?)`**: Opens or closes the edit dialog.
  - **`onResetClicked()`**: Restores defaults (built-in group, dishes, ₹150 budget, blank search), clears error, and **strictly sets `isCalculated = false`, `totalCompatibleCount = 0`, and clears result rows until run again**.

### Task 2.3: Implement Strict State Clearing
- Inside `calculateCompatibility()`, if the engine returns `EngineResult.Failure`:
  - Set `isCalculated = false`.
  - Set `totalCompatibleCount = 0`.
  - Set `displayedCompatibleDishes = emptyList()`.
  - Set `excludedDishes = emptyList()`.
  - Set `errorMessage = result.message`.
  - Guarantee zero stale results or counts remain visible on screen.

### Task 2.4: ViewModel State Unit Tests (`test/.../BoardViewModelTest.kt`)
- Write unit tests verifying:
  - Default initialization state.
  - Calculation state populates `totalCompatibleCount = 2`.
  - Typing `"wheat"` filters `displayedCompatibleDishes` to only `D02`, but `totalCompatibleCount` remains `2`.
  - Clearing search query restores both `D01` and `D02`.
  - Reset wipes all calculated output and sets count to `0`.
  - Setting price to `0` wipes all outputs and sets `errorMessage`.

---

## 3. Checkpoint & Verification Before Moving to Phase 3

Run ViewModel unit tests:
```bash
./gradlew testDebugUnitTest --tests "*.BoardViewModelTest"
```

### Success Criteria:
- [x] State flow accurately handles search query narrowing without altering `totalCompatibleCount`.
- [x] Reset action strictly removes calculation output until re-run.
- [x] Errors completely cleanse stale results from the UI state.
- [x] Tests run purely on the JVM in **<1 second**.
