# Phase 3: Jetpack Compose UI & Separated Components
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement\Md files\phases`  
**Date**: September 2026  

---

## 1. Goal

Build an attractive, modern, single-screen dashboard using Jetpack Compose and Material 3, keeping all visual components cleanly separated in `ui/components/` for easy search and modification, wired directly to `MainActivity`.

---

## 2. Main Tasks

### Task 3.1: Modular UI Components (`ui/components/`)
Implement each visual element in its dedicated file:

1. **`SummaryHeaderCard.kt`**:
   - Displays application title and current per-person budget indicator.
   - Houses primary action buttons:
     - **"Load Built-in"** (Triggers `onLoadBuiltInAndCalculate()`)
     - **"Calculate"** (Triggers `onCalculateClicked()`)
     - **"Reset"** (Triggers `onResetClicked()`)
   - Prominent summary badge displaying: `Compatible Dishes: X` (accent container).
2. **`SearchBarComponent.kt`**:
   - `OutlinedTextField` with a search magnifying-glass leading icon.
   - Trailing `IconButton` with a clear (`X`) icon when text is non-empty, resetting query to `""`.
   - Placeholder: `"Search compatible dishes by cafe, dish, or ingredient..."`.
3. **`ResidentSection.kt`**:
   - Card container with title "Hostel Group Residents".
   - Renders each resident (Asha, Dev, Mira) with their diet badge (`VEGAN`, `VEGETARIAN`, `NO_RESTRICTION`) and allergen chips (`PEANUT`, `MILK`, or `none`).
4. **`DishSection.kt`**:
   - Card container with title "Available Dishes".
   - Lists dishes in source order (D01–D05).
   - Shows Dish ID, Cafe name, Dish name, Diet badge, Ingredient tag chips, and Price (`₹XXX`).
   - Includes an **"Edit"** button on each card triggering `onSelectDishForEdit(dish)`.
5. **`ResultsSection.kt`**:
   - **Compatible Dishes Sub-section**:
     - Light green background / outline cards.
     - Preserves original source order.
     - Visual checkmark indicator.
   - **Excluded Dishes Sub-section**:
     - Muted gray/red border cards.
     - Displays dish info alongside styled badges for each exact failure reason (`DIET:Asha`, `ALLERGEN:Mira:MILK`, `OVER_BUDGET`).
6. **`EditDishDialog.kt`**:
   - Modal `AlertDialog` allowing focused row editing.
   - Form fields: Dish Name, Cafe Name, Price, Ingredient tags.
   - Allows changing price (e.g. testing ₹130 budget boundary or entering `0` to test validation failure).

### Task 3.2: Screen Coordinator (`ui/screens/BoardScreen.kt`)
- Collects `state by viewModel.state.collectAsState()`.
- Implements `Scaffold` with `TopAppBar`.
- Uses a `LazyColumn` containing:
  1. `SummaryHeaderCard`
  2. Error Banner Card (rendered only when `state.errorMessage != null`)
  3. `SearchBarComponent`
  4. `ResidentSection`
  5. `DishSection`
  6. `ResultsSection` (rendered only when `state.isCalculated && state.errorMessage == null`)
- Renders `EditDishDialog` when `state.selectedDishForEdit != null`.

### Task 3.3: App Entry Point (`MainActivity.kt`)
- Wire `MainActivity` to invoke `HostelFoodCompatibilityBoardTheme { BoardScreen() }`.
- Zero Compose Navigation setup required.

---

## 3. Checkpoint & Verification Before Moving to Phase 4

Build and run the app on Android Studio emulator or connected physical device:
```bash
./gradlew installDebug
```

### Success Criteria:
- [x] Single screen loads with built-in data cleanly displayed.
- [x] Tapping "Load Built-in" displays D01 & D02 under Compatible, D03–D05 under Excluded, and badge shows `Compatible: 2`.
- [x] Tapping "Reset" restores defaults and clears all result cards and count badge until calculated again.
- [x] Tapping "Edit" on a dish opens `EditDishDialog` and saves updates seamlessly.
- [x] Typing in Search Bar filters compatible dishes instantly; clear button ('X') resets search query.
- [x] No UI clipping, keyboard obstruction, or layout overflow errors.
