# Hostel Food Compatibility Board 🍲

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Pure%20Domain%20Engine-blue.svg)](docs/ARCHITECTURE_PROPOSAL.md)
[![Tests](https://img.shields.io/badge/Tests-20%2F20%20Passing%20(JVM%20%3C1.5s)-brightgreen.svg)](docs/phases/ACCEPTANCE_EVIDENCE_REPORT.md)

An intelligent, single-screen decision-support Android application built with **Kotlin** and **Jetpack Compose** (Material 3). It enables a group of hostel residents to find campus cafe dishes that satisfy everyone's dietary requirements, allergen exclusions, and per-person budget constraints simultaneously.

Designed for the **Cisco AI-Assisted Coding Interview (Problem SI26_P02)**.

---

## 📑 Project Documentation Index

All architectural decisions, technical specifications, and phase plans are maintained in the [`docs/`](docs/) directory:

| Document | Description |
| :--- | :--- |
| 📋 [**Technical Specification & Requirements**](docs/TECHNICAL_SPECIFICATION_AND_REQUIREMENTS.md) | Full problem contract, exact tag formats, normalization rules, and data specs |
| 🏛️ [**Architecture Proposal (MVVM)**](docs/ARCHITECTURE_PROPOSAL.md) | Component architecture, data flow, state management, and rationale |
| 📊 [**Problem Analysis Report**](docs/ANALYSIS_REPORT.md) | Initial problem breakdown, constraints, platform comparison, and evaluation rubric |
| 🗺️ [**Master 4-Phase Implementation Plan**](docs/IMPLEMENTATION_PLAN.md) | High-level roadmap with verification checkpoints for interview evaluation |
| 🧪 [**Acceptance Test Evidence Report**](docs/phases/ACCEPTANCE_EVIDENCE_REPORT.md) | Complete execution report for all 20 automated tests & live modification drills |

---

## 🏗️ Architecture Overview

The application adopts a clean **3-layer MVVM** architecture with **Unidirectional Data Flow (UDF)**:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Presentation Layer (Jetpack Compose Material 3)          │
│    • BoardScreen.kt (Single-screen dashboard coordinator)   │
│    • Modular components in ui/components/                   │
└──────────────────────────────▲──────────────────────────────┘
                               │ Observes BoardScreenState (StateFlow)
                               │ Emits User Events (Calculate, Edit, Search, Reset)
┌──────────────────────────────┴──────────────────────────────┐
│ 2. ViewModel Layer (Android Lifecycle ViewModel)            │
│    • BoardViewModel.kt                                      │
│    • In-memory state holder (Zero Repositories needed)      │
│    • Decouples search query filtering from overall count    │
└──────────────────────────────▲──────────────────────────────┘
                               │ Invokes pure business logic
┌──────────────────────────────┴──────────────────────────────┐
│ 3. Domain Engine Layer (Pure Kotlin - Zero Android SDK)     │
│    • CompatibilityEngine.kt                                 │
│    • Input validation (INVALID_INPUT, DUPLICATE_DISH_ID)    │
│    • Isolated rule functions: checkDiet, checkAllergens...  │
│    • 100% JVM Unit Testable in <1s                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Core Business Rules & Contract Tags

A dish is **Compatible** if and only if it passes all three checks across all residents:

| Rule Category | Verification Logic | Failure Reason Format |
| :--- | :--- | :--- |
| **1. Dietary Rule** | • `VEGAN` resident accepts **only** `VEGAN` dishes.<br>• `VEGETARIAN` resident accepts `VEGAN` or `VEGETARIAN` dishes.<br>• `NO_RESTRICTION` resident accepts any dish. | `DIET:<residentName>`<br>*(e.g., `DIET:Asha`)* |
| **2. Allergen Rule** | Exact literal match between normalized dish `ingredientTags` and resident `allergens`. Authoritative tags only (no inference). | `ALLERGEN:<residentName>:<matchedTag>`<br>*(e.g., `ALLERGEN:Mira:MILK`)* |
| **3. Budget Rule** | Dish price $\le$ Per-person group budget. (Never multiplied by group size). | `OVER_BUDGET` |

### Deterministic Reason Ordering Contract:
Excluded dish reasons must strictly follow this exact order:
1. **Resident Table Order** (`Asha` $\to$ `Dev` $\to$ `Mira`).
2. **Category Order per Resident**: `DIET` reason must precede `ALLERGEN` reason(s).
3. **Ingredient Tag Order**: Allergens ordered according to the dish's original ingredient declaration order.
4. **Budget Last**: `OVER_BUDGET` is always placed at the very end.

---

## 🚀 Detailed Phase-by-Phase Breakdown & How to Test

The project was executed in **4 distinct, practical phases** directly aligned with the Cisco evaluation rubric:

```
Phase 1: Domain Models & Pure Kotlin Engine ──► Phase 2: ViewModel & StateFlow ──► Phase 3: Compose Single-Screen UI ──► Phase 4: Acceptance Proof & Drills
```

---

### ⚙️ Phase 1: Domain Models & Pure Kotlin Compatibility Engine
📄 **Specification**: [`docs/phases/PHASE_1_DOMAIN_ENGINE_AND_MODELS.md`](docs/phases/PHASE_1_DOMAIN_ENGINE_AND_MODELS.md)

#### What Phase 1 Does:
- **Core Entities (`model/`)**: Defines immutable data models (`DietClass`, `Resident`, `Dish`, `ExcludedDish`, `EngineResult`).
- **Default Dataset (`data/SampleData.kt`)**: Sets up built-in residents (`Asha`, `Dev`, `Mira`), default dishes (`D01`–`D05`), and the default ₹150 budget.
- **Pure Domain Engine (`engine/CompatibilityEngine.kt`)**:
  - **Normalization**: Trims and converts all diet classes, ingredient tags, and allergen tags to uppercase. Treats `"none"` as an empty allergen list.
  - **Isolated Rule Functions**: `checkDiet()`, `checkAllergens()`, and `checkBudget()` implemented as single-responsibility functions for fast live modifications.
  - **Deterministic Reason Sorter**: Sorts rejection reasons strictly by resident order, `DIET` before `ALLERGEN`, dish ingredient declaration order, and `OVER_BUDGET` last.
  - **Strict Validation**: Checks non-empty trimmed strings, unique dish IDs (`DUPLICATE_DISH_ID`), and positive integers (`INVALID_INPUT`).
  - **Zero Android SDK Dependencies**: Contains no `android.*` imports, enabling sub-second execution on the JVM.

#### How to Test Phase 1 According to the PS:
Run the pure engine unit tests in Git Bash / Terminal:
```bash
./gradlew testDebugUnitTest --tests "*.CompatibilityEngineTest"
```
**What gets verified:**
1. **Built-in Dataset Run**: Confirms D01 & D02 are compatible in that order.
2. **Contracted Exclusion Strings**: Asserts character-for-character:
   - D03: `["DIET:Asha", "ALLERGEN:Mira:MILK"]`
   - D04: `["ALLERGEN:Dev:PEANUT"]`
   - D05: `["DIET:Asha", "DIET:Dev"]`
3. **Boundary Price Pass**: Confirms D02 (₹150) passes the ₹150 budget.
4. **Tag Normalization**: Confirms whitespace padding (`"  peanut  "`) is normalized without failure.
5. **Validation Errors**: Asserts price 0 produces `INVALID_INPUT: Dishes, D01, Price` and duplicate IDs produce `DUPLICATE_DISH_ID`.

---

### 🔄 Phase 2: ViewModel & State Management (Unidirectional Data Flow)
📄 **Specification**: [`docs/phases/PHASE_2_VIEWMODEL_AND_STATE_MANAGEMENT.md`](docs/phases/PHASE_2_VIEWMODEL_AND_STATE_MANAGEMENT.md)

#### What Phase 2 Does:
- **Unified UI State (`viewmodel/BoardScreenState.kt`)**: Holds in-memory collections (`residents`, `dishes`, `budget`), active search query, filtered lists, total counts, and error messages.
- **State Coordination (`viewmodel/BoardViewModel.kt`)**:
  - **Single-Action Load (`onLoadBuiltInAndCalculate`)**: Fulfills Acceptance Criterion 1 by loading defaults and evaluating compatibility in a single action.
  - **Search Narrowing with Count Decoupling (`onSearchQueryChanged`)**: Narrows `displayedCompatibleDishes` by case-insensitive substring match against Cafe, Dish name, or Ingredients, **while strictly keeping `totalCompatibleCount` independent** (Acceptance Criterion 3).
  - **Budget Shift (`onBudgetChanged`)**: Updates budget and dynamically triggers recalculation.
  - **Strict Reset Contract (`onResetClicked`)**: Restores defaults and **strictly wipes all calculation outputs and counts until calculated again** (Acceptance Criterion 6).
  - **Strict State Clearing on Error**: On invalid input, immediately clears all compatible rows, exclusion rows, and counters to prevent stale data.

#### How to Test Phase 2 According to the PS:
Run the ViewModel state transition unit tests:
```bash
./gradlew testDebugUnitTest --tests "*.BoardViewModelTest"
```
**What gets verified:**
1. **Initial State**: Confirms calculation is inactive on startup.
2. **Search Count Decoupling**: Searches `"wheat"` $\to$ narrows displayed list to `D02`, while asserting that `totalCompatibleCount` **strictly remains 2**. Clearing search restores both dishes.
3. **Budget Boundary Shift**: Changes budget to ₹130 $\to$ confirms D01 is compatible and D02 moves to Excluded with `OVER_BUDGET`.
4. **Error State Clearing**: Sets price to 0 $\to$ confirms all results and counts are wiped.
5. **Reset Contract**: Clicks reset $\to$ confirms clean slate with zero stale outputs.

---

### 🎨 Phase 3: Jetpack Compose UI & Separated Components
📄 **Specification**: [`docs/phases/PHASE_3_COMPOSE_UI_AND_COMPONENTS.md`](docs/phases/PHASE_3_COMPOSE_UI_AND_COMPONENTS.md)

#### What Phase 3 Does:
Translates the headless state into an attractive, single-screen Material 3 dashboard, structured into separated files under `ui/components/`:
- **`SummaryHeaderCard.kt`**: Displays budget input, preset toggle (`Set ₹130` / `Set ₹150`), green count badge (`COMPATIBLE SUMMARY`), and action buttons (**Load Built-in**, **Calculate**, **Reset**).
- **`SearchBarComponent.kt`**: Outlined search box with clear (`✕`) action.
- **`ResidentSection.kt`**: Renders residents with colored diet pills and allergen chips.
- **`DishSection.kt`**: Renders dishes in source order with cafe names, tags, prices, and an **"Edit"** button.
- **`ResultsSection.kt`**: Displays compatible cards (light green, with evidence chips) and excluded cards (with exact red contracted failure badges).
- **`EditDishDialog.kt`**: Modal dialog to modify dish price or ingredients live (includes a **"Set ₹0 (Test Error)"** button).
- **`BoardScreen.kt`**: Main layout coordinator with a smooth scrolling `LazyColumn` and dynamic red error banner.
- **`MainActivity.kt`**: App entry point setting `BoardScreen()`.

#### How to Test Phase 3 According to the PS:
1. **Compile & Build Debug APK**:
   ```bash
   ./gradlew compileDebugKotlin assembleDebug
   ```
2. **Run on Emulator / Connected Physical Device**:
   - In Android Studio, click the green **Run ▶️** button (or press `Shift + F10`).
3. **Manual Interactive Verification**:
   - Tap **"Load Built-in"** $\to$ Verify D01 & D02 show in green cards and badge shows `2 DISHES FOUND`.
   - Scroll to **Excluded Dishes** $\to$ Verify D03 (`DIET:Asha`, `ALLERGEN:Mira:MILK`), D04 (`ALLERGEN:Dev:PEANUT`), and D05 (`DIET:Asha`, `DIET:Dev`).
   - Type `"wheat"` in search $\to$ Verify only D02 is visible and badge **stays at 2**. Tap `✕` $\to$ D01 & D02 return.
   - Tap **"Set ₹130"** $\to$ Verify D02 moves to Excluded with badge `OVER_BUDGET`.
   - Tap **"Edit"** on D01 $\to$ Tap **"Set ₹0"** $\to$ Save $\to$ Verify red error banner appears and results vanish.
   - Tap **"Reset"** $\to$ Verify clean initial state restored.

---

### 🧪 Phase 4: Acceptance Criteria Verification & Live Modification Drills
📄 **Specification**: [`docs/phases/PHASE_4_ACCEPTANCE_TESTING_AND_INTERVIEW_PREP.md`](docs/phases/PHASE_4_ACCEPTANCE_TESTING_AND_INTERVIEW_PREP.md)  
📄 **Evidence Report**: [`docs/phases/ACCEPTANCE_EVIDENCE_REPORT.md`](docs/phases/ACCEPTANCE_EVIDENCE_REPORT.md)

#### What Phase 4 Does:
- **Dedicated Acceptance Suite (`AcceptanceCriteriaTest.kt`)**: Houses 6 end-to-end programmatic tests executing the exact requirements of the Problem Statement.
- **Live Modification Drills**: Provides pre-rehearsed code patterns for the 15-minute live coding challenge (e.g. adding a new dietary class, an allergen alias, or a hard price ceiling).
- **Interview Script & Talking Points**: Prepares concise, senior-level explanations for design choices, AI collaboration, and testing mindset.

#### How to Test Phase 4 According to the PS:
Run the complete acceptance test suite:
```bash
# Run the 6 required Cisco Acceptance Criteria tests:
./gradlew testDebugUnitTest --tests "*.AcceptanceCriteriaTest"

# Run the complete 20-test project suite:
./gradlew testDebugUnitTest
```
**Test Results Summary:**
```text
AcceptanceCriteriaTest > testAcceptanceCriterion1_BuiltInLoadAndCount PASSED [0.124s]
AcceptanceCriteriaTest > testAcceptanceCriterion2_ContractedExclusionsAndBoundary PASSED
AcceptanceCriteriaTest > testAcceptanceCriterion3_SearchNarrowingAndCountDecoupling PASSED
AcceptanceCriteriaTest > testAcceptanceCriterion4_BudgetBoundaryShift PASSED
AcceptanceCriteriaTest > testAcceptanceCriterion5_InvalidPriceHandlingAndClearing PASSED
AcceptanceCriteriaTest > testAcceptanceCriterion6_ResetStateSynchronization PASSED

Total Tests: 20 | Failures: 0 | Skipped: 0 | Success Rate: 100% (1.497s)
```

---

## 📋 The 6 Mandatory Acceptance Criteria Matrix

| # | Acceptance Scenario | Action in App / Test | Expected Output | Contract Verified |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Built-in Run** | Tap **"Load Built-in"** | Shows **D01** then **D02**; Compatible Count = **`2`**. | Source order preserved. |
| **2** | **Contracted Exclusions** | Scroll to Excluded Section | • D03: `DIET:Asha, ALLERGEN:Mira:MILK`<br>• D04: `ALLERGEN:Dev:PEANUT`<br>• D05: `DIET:Asha, DIET:Dev`<br>• D02 (₹150) passes ₹150 budget. | Exact strings match specification. |
| **3** | **Search Narrowing** | Type `"wheat"` into Search Bar | • Only **D02** is displayed in the list.<br>⚠️ **Compatible Count STAYS `2`**.<br>• Tap `✕` $\to$ D01 & D02 show again. | Count is decoupled from filtered view. |
| **4** | **Budget Boundary Shift** | Tap **"Set ₹130"** | • Only D01 is compatible (Count = 1).<br>• D02 moves to Excluded with `OVER_BUDGET`. | Boundary price transition. |
| **5** | **Input Validation** | Set D01 price to 0 | • Red banner: `INVALID_INPUT: Dishes, D01, Price`.<br>⚠️ **All result rows and counts are cleared**. | Output clearing contract. |
| **6** | **Reset State Synchronization**| Tap **"Reset"** | • Built-in data restored, budget = ₹150, search empty.<br>• **Calculated outputs and errors cleared until run again**. | Reset synchronization contract. |

---

## 💻 Complete Testing & Verification Commands Table

| Goal | Command (Git Bash / PowerShell) | Expected Result |
| :--- | :--- | :--- |
| **Run All 20 Unit Tests** | `./gradlew testDebugUnitTest` | `BUILD SUCCESSFUL (20 tests passed in <1.5s)` |
| **Run 6 Acceptance Tests Only** | `./gradlew testDebugUnitTest --tests "*.AcceptanceCriteriaTest"` | `6 tests passed in ~0.1s` |
| **Run Pure Domain Engine Tests** | `./gradlew testDebugUnitTest --tests "*.CompatibilityEngineTest"` | `7 tests passed in ~0.03s` |
| **Run ViewModel State Tests** | `./gradlew testDebugUnitTest --tests "*.BoardViewModelTest"` | `6 tests passed in ~0.01s` |
| **Compile All Kotlin Sources** | `./gradlew compileDebugKotlin` | `BUILD SUCCESSFUL` |
| **Build Full Android Debug APK**| `./gradlew assembleDebug` | `app-debug.apk generated` |
| **Install App on Emulator/Device**| `./gradlew installDebug` | `App installed on active device` |

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose (BOM 2026.02.01) + Material 3
- **State Management**: Android `ViewModel` + `StateFlow`
- **Testing**: JUnit 4 (100% JVM execution without emulator)
- **Zero Heavy Over-Engineering**: No Room DB, no Retrofit/networking, no Dagger/Hilt, no Compose Navigation overhead. Pure in-memory architecture matching the problem statement.
