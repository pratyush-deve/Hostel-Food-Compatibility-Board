# Master 4-Phase Implementation Plan
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement\Md files`  
**Evaluation Standard**: Cisco AI-Assisted Interview Rubric (3–5 Ordered Steps with Verification Checkpoints)  
**Date**: September 2026  

---

## Plan Overview & Strategy

To satisfy the Cisco interview requirements—which evaluate your planning, architecture choices, AI collaboration, and live modification capability—the project is broken down into **4 practical, highly focused phases**:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: Domain Models & Pure Kotlin Engine (Rules, Normalization, Sort)    │
│ Checkpoint: 100% JVM JUnit test pass (<1s runtime, all 6 acceptance checks) │
├─────────────────────────────────────────────────────────────────────────────┤
│ PHASE 2: ViewModel & StateFlow (UDF, Search Filtering, State Clearing)      │
│ Checkpoint: State management unit tests verify count decoupling & reset     │
├─────────────────────────────────────────────────────────────────────────────┤
│ PHASE 3: Jetpack Compose UI & Separated Modular Components                  │
│ Checkpoint: Single-screen dashboard renders, edits, and triggers actions    │
├─────────────────────────────────────────────────────────────────────────────┤
│ PHASE 4: End-to-End Acceptance Verification & Live Modification Prep        │
│ Checkpoint: All 6 required interview demos verified + modification drills   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Detailed Phase Breakdown

### Phase 1: Core Domain Models & Pure Kotlin Engine
- **Goal**: Build and test the complete business logic, validation rules, string normalization, and deterministic failure-reason sorting in pure Kotlin without any Android dependencies.
- **Main Tasks**:
  1. Define immutable data models in `model/`: `DietClass`, `Resident`, `Dish`, `ExcludedDish`, `EngineResult`.
  2. Implement built-in datasets in `data/SampleData.kt` (Asha, Dev, Mira, D01–D05, ₹150 budget).
  3. Implement `CompatibilityEngine.kt` in `engine/` with isolated rule functions (`checkDiet`, `checkAllergens`, `checkBudget`) and strict input validation (`INVALID_INPUT`, `DUPLICATE_DISH_ID`).
  4. Write `CompatibilityEngineTest.kt` verifying all 6 acceptance criteria and edge cases.
- **Checkpoint / Verification**:
  - Run JUnit tests on local JVM: `gradlew test` passes in **<1 second**. Zero emulator or device required.
- **Detailed Specification**: [📄 Read Phase 1 Details](file:///C:/Users/KIIT/OneDrive/Desktop/CISCO%20Prob%20Statement/Md%20files/phases/PHASE_1_DOMAIN_ENGINE_AND_MODELS.md)

---

### Phase 2: ViewModel & State Management (Unidirectional Data Flow)
- **Goal**: Connect the pure engine to the presentation layer using Android `ViewModel` and `StateFlow`, managing UI state transitions and search narrowing.
- **Main Tasks**:
  1. Define unified immutable state in `viewmodel/BoardScreenState.kt`.
  2. Implement `BoardViewModel.kt` with event handlers:
     - `onLoadBuiltInAndCalculate()` (Single-tap load & evaluate)
     - `onCalculateClicked()`
     - `onResetClicked()` (Restores defaults and strictly clears results until run again)
     - `onSearchQueryChanged(query)` (Narrows displayed list without mutating total compatible count)
     - `onBudgetChanged(newBudget)`
     - `onUpdateDish(updatedDish)`
  3. Implement strict state clearing on validation error (`errorMessage != null`).
- **Checkpoint / Verification**:
  - Unit test ViewModel state transitions: Verify search `"wheat"` shows only D02 while `totalCompatibleCount` remains `2`; verify Reset clears output; verify error states immediately wipe calculated rows.
- **Detailed Specification**: [📄 Read Phase 2 Details](file:///C:/Users/KIIT/OneDrive/Desktop/CISCO%20Prob%20Statement/Md%20files/phases/PHASE_2_VIEWMODEL_AND_STATE_MANAGEMENT.md)

---

### Phase 3: Jetpack Compose UI & Separated Components
- **Goal**: Create an attractive, single-screen dashboard using Material 3, modularized into easily searchable component files.
- **Main Tasks**:
  1. Build `SummaryHeaderCard.kt` (Title, budget indicator, count badge `Compatible: 2`, action buttons: "Load Built-in", "Calculate", "Reset").
  2. Build `SearchBarComponent.kt` (Search input with instant clear 'X' icon).
  3. Build `ResidentSection.kt` (Resident cards with diet badges and allergen chips).
  4. Build `DishSection.kt` (Dish cards with cafe, tags, price, and "Edit" button).
  5. Build `ResultsSection.kt` (Compatible green cards in source order + Excluded cards with contracted reason chips).
  6. Build `EditDishDialog.kt` (Modal to modify prices/tags or test boundary/zero values).
  7. Assemble all components inside `BoardScreen.kt` with `Scaffold` and `LazyColumn`.
  8. Connect `MainActivity.kt` directly to `BoardScreen()` (zero Compose Navigation overhead).
- **Checkpoint / Verification**:
  - Launch app on emulator/device. Verify smooth rendering, no overlapping elements, clean keyboard dismiss on search, and instant UI updates.
- **Detailed Specification**: [📄 Read Phase 3 Details](file:///C:/Users/KIIT/OneDrive/Desktop/CISCO%20Prob%20Statement/Md%20files/phases/PHASE_3_COMPOSE_UI_AND_COMPONENTS.md)

---

### Phase 4: Acceptance Testing, Evidence & Live Modification Readiness
- **Goal**: Rehearse and validate the 6 mandatory demonstration scenarios, capture test evidence, and prepare the development environment for surprise live modifications.
- **Main Tasks**:
  1. **Demo 1**: Load built-in data in one action $\to$ D01, D02 shown, count = 2.
  2. **Demo 2**: Verify exclusion reasons for D03, D04, D05; show D02 (₹150) passes ₹150 budget.
  3. **Demo 3**: Search `"wheat"` $\to$ only D02 shown, count stays 2. Clear query $\to$ both shown.
  4. **Demo 4**: Change budget to ₹130 $\to$ D01 compatible, D02 marked `OVER_BUDGET`.
  5. **Demo 5**: Set D01 price to 0 $\to$ `INVALID_INPUT: Dishes, D01, Price`, all outputs cleared.
  6. **Demo 6**: Click Reset $\to$ clean state restored.
  7. **Live Modification Drills**: Practice 2 live modification scenarios (e.g. adding a new diet class `PESCATARIAN` or an allergen alias rule).
- **Checkpoint / Verification**:
  - All 6 acceptance scenarios pass without hesitation. Fast hot-reload and JVM test runs confirmed ready for the live 30-minute interview.
- **Detailed Specification**: [📄 Read Phase 4 Details](file:///C:/Users/KIIT/OneDrive/Desktop/CISCO%20Prob%20Statement/Md%20files/phases/PHASE_4_ACCEPTANCE_TESTING_AND_INTERVIEW_PREP.md)

---

## Summary of Phase Files in `Md files/phases/`

| File | Focus Area | Key Output |
| :--- | :--- | :--- |
| **`PHASE_1_DOMAIN_ENGINE_AND_MODELS.md`** | Business Rules & Domain Logic | `model/`, `data/SampleData.kt`, `engine/CompatibilityEngine.kt`, JVM Unit Tests |
| **`PHASE_2_VIEWMODEL_AND_STATE_MANAGEMENT.md`** | StateFlow & Coordination | `viewmodel/BoardScreenState.kt`, `viewmodel/BoardViewModel.kt` |
| **`PHASE_3_COMPOSE_UI_AND_COMPONENTS.md`** | Material 3 Jetpack Compose UI | `ui/screens/BoardScreen.kt`, `ui/components/*`, `MainActivity.kt` |
| **`PHASE_4_ACCEPTANCE_TESTING_AND_INTERVIEW_PREP.md`** | Verification & Live Modification | 6 Demo Scripts, Edge Case Suite, Live Modification Drill Plan |
