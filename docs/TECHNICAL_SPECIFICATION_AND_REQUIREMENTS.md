# Technical Specification & Requirements Document
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement`  
**Date**: September 2026  

---

## 1. Executive Summary & Problem Scope

The **Hostel Food Compatibility Board** is a single-screen decision-support application designed for a group of hostel residents. The system determines which dishes from campus cafes are edible by **all** members of the group simultaneously, taking into account:
1. **Dietary restrictions** (Vegan, Vegetarian, Non-Vegetarian, No-Restriction)
2. **Allergen exclusions** (Authoritative exact tag matching)
3. **Per-person budget limits** (Whole rupee threshold)

### Key Scope Guidelines
- **100% In-Memory**: No backend server, no database, no authentication, no network requests, and no file uploads.
- **Contract Adherence**: Exact matching of strings, error codes (`INVALID_INPUT`, `DUPLICATE_DISH_ID`), and failure reason formats (`DIET:<resident>`, `ALLERGEN:<resident>:<tag>`, `OVER_BUDGET`).
- **State Integrity**: Errors or duplicate IDs must immediately clear calculated outputs and counters.

---

## 2. Inputs & Built-in Data Specifications

### 2.1 Resident Model
| Field | Type | Description / Constraints |
| :--- | :--- | :--- |
| `name` | `String` | Non-empty trimmed string. Table order establishes priority in output. |
| `diet` | `DietClass` | Valid: `VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`, `NO_RESTRICTION` (*resident only*). |
| `allergens` | `List<String>` | List of normalized uppercase strings. Keyword `"none"` represents an empty list. |

#### Built-in Residents:
| Name | Diet Class | Allergens |
| :--- | :--- | :--- |
| **Asha** | `VEGAN` | `none` (Empty list) |
| **Dev** | `VEGETARIAN` | `PEANUT` |
| **Mira** | `NO_RESTRICTION` | `MILK` |

---

### 2.2 Dish Model
| Field | Type | Description / Constraints |
| :--- | :--- | :--- |
| `id` | `String` | Non-empty trimmed string. **Must be globally unique**. |
| `cafe` | `String` | Non-empty trimmed string. Target for search filtering. |
| `name` | `String` | Non-empty trimmed string. Target for search filtering. |
| `diet` | `DietClass` | Valid: `VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`. |
| `ingredientTags` | `List<String>` | Non-empty ordered list of normalized uppercase strings. Authoritative for allergen checks. |
| `price` | `Int` | Positive whole rupee integer ($> 0$). |

#### Built-in Dishes (Source Order Preserved):
| Dish ID | Cafe Name | Dish Name | Diet Class | Ingredient Tags | Price (₹) |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **D01** | Hostel Cafe | Lentil Rice Bowl | `VEGAN` | `LENTIL`, `RICE`, `SPINACH` | ₹110 |
| **D02** | Library Cafe | Tomato Pasta | `VEGAN` | `WHEAT`, `TOMATO` | ₹150 |
| **D03** | Hostel Cafe | Paneer Wrap | `VEGETARIAN` | `MILK`, `WHEAT` | ₹140 |
| **D04** | East Cafe | Peanut Noodles | `VEGAN` | `PEANUT`, `WHEAT` | ₹130 |
| **D05** | Library Cafe | Egg Sandwich | `NON_VEGETARIAN` | `EGG`, `WHEAT` | ₹100 |

---

### 2.3 Group Budget
- **Type**: `Int` (Positive whole number).
- **Default**: `₹150`.
- **Constraint**: Evaluated per person for one serving. **Never multiplied by group size**.

---

## 3. Business & Compatibility Rules

A dish is **Compatible** if and only if it passes all three checks for all residents. Otherwise, it is marked **Excluded** with specific violation reasons.

### 3.1 Normalization Contract
- Before comparison, all diet classes, ingredient tags, and allergen tags must be:
  1. Trimmed of surrounding whitespace (`trim()`).
  2. Converted to uppercase (`uppercase()`).

### 3.2 Dietary Rule
- **`VEGAN` resident**: Accepts **only** `VEGAN` dishes.
- **`VEGETARIAN` resident**: Accepts `VEGAN` or `VEGETARIAN` dishes.
- **`NO_RESTRICTION` resident**: Accepts any dish (`VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`).
- *Failure Format*: `DIET:<residentName>`

### 3.3 Allergen Rule
- Triggers if **any normalized ingredient tag of the dish** equals **any normalized allergen tag of any resident**.
- **Strictly Authoritative**: Literal exact string match only. Do not infer parent ingredients, aliases, or cross-contamination.
- *Failure Format*: `ALLERGEN:<residentName>:<tag>`

### 3.4 Budget Rule
- Evaluates: $\text{dish.price} \le \text{groupBudget}$.
- Boundary condition: $\text{Price} = \text{Budget}$ passes.
- *Failure Format*: `OVER_BUDGET`

### 3.5 Deterministic Exclusion Reason Ordering Contract
When a dish fails, its reasons list must be ordered strictly by:
1. **Resident Table Order**: Asha $\to$ Dev $\to$ Mira.
2. **Category Priority per Resident**: `DIET` reason must precede `ALLERGEN` reason(s).
3. **Ingredient Order**: If multiple allergens trigger, sort by the dish's **original `ingredientTags` declaration order**.
4. **Budget Last**: `OVER_BUDGET` is always placed at the very end.

*Contract Verification*:
- **D03**: `DIET:Asha, ALLERGEN:Mira:MILK`
- **D04**: `ALLERGEN:Dev:PEANUT`
- **D05**: `DIET:Asha, DIET:Dev`

---

## 4. Validation Rules & State Clearing

| Condition | Verification Rule | Error Code / Behavior |
| :--- | :--- | :--- |
| **Blank Fields** | Name, ID, Cafe, Dish Name, Ingredients cannot be empty after trimming. | `INVALID_INPUT: <Table>, <Row/ID>, <Field>` |
| **Non-Positive Price/Budget** | Price $\le 0$ or Budget $\le 0$. | `INVALID_INPUT: <Table>, <Row/ID>, <Field>` |
| **Duplicate Dish ID** | Two dishes share the same ID. | `DUPLICATE_DISH_ID: <ID>` |
| **Output Clearing** | Any validation error or duplicate ID occurs. | **Instantly clear all compatible rows, exclusion rows, and counters.** |
| **Reset Action** | User clicks Reset. | Restores default group, dishes, ₹150 budget, clears search, and clears all error/result states. |

---

## 5. Search Behavior & Narrowing

- **Scope**: Evaluated **only** against dishes that are already verified as **Compatible**.
- **Matching Algorithm**: Case-insensitive substring match against:
  1. Cafe name (e.g., `"hostel"`)
  2. Dish name (e.g., `"lentil"`)
  3. Any ingredient tag (e.g., `"wheat"`)
- **Count Decoupling (Critical Requirement)**:
  - When the search narrows down the visible compatible dishes, **the overall compatible count summary must not change** (it continues to show the unfiltered total compatible count).
  - *Example*: Searching `"wheat"` displays only **D02**, but the count badge stays **`2`**.

---

## 6. Edge Cases & Boundary Handling

1. **Boundary Price Match**: Dish price equals budget (e.g., D02 at ₹150 with budget ₹150) must pass.
2. **Zero Price**: D01 price set to `0` triggers `INVALID_INPUT: Dishes, D01, Price` and immediately clears old calculation outputs.
3. **Keyword "none"**: Resident allergen `"none"` is parsed as an empty list (does not match an ingredient called "NONE").
4. **Whitespace Tolerance**: Strings with leading/trailing spaces (`"  PEANUT  "`) are normalized without triggering validation errors.
5. **Multiple Failure Causes**: A dish failing diet, multiple allergens, and budget must output all reasons in the exact specified sort order.
6. **No Search Matches**: A query with zero matches displays an empty list state, while the overall compatible count remains untouched.

---

## 7. The 6 Mandatory Acceptance Test Scenarios

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. Built-in Run:                                                            │
│    Action: Load default data with ₹150 budget.                               │
│    Result: Compatible = [D01, D02]; Compatible Count = 2.                   │
├─────────────────────────────────────────────────────────────────────────────┤
│ 2. Contracted Exclusion Verification:                                       │
│    Action: Check excluded dishes list.                                      │
│    Result:                                                                  │
│      - D03: DIET:Asha, ALLERGEN:Mira:MILK                                   │
│      - D04: ALLERGEN:Dev:PEANUT                                             │
│      - D05: DIET:Asha, DIET:Dev                                             │
│      - Verify D02 (₹150) passes boundary budget.                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ 3. Deterministic Search Narrowing:                                          │
│    Action: Type "wheat" into search box.                                    │
│    Result: Displays only D02; Overall Compatible Count stays 2.             │
│    Action: Clear search box.                                                │
│    Result: Displays both D01 and D02; Count stays 2.                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ 4. Budget Boundary Shift:                                                   │
│    Action: Change budget from ₹150 to ₹130.                                 │
│    Result: Compatible = [D01]; Count = 1; D02 moves to Excluded             │
│            with reason "OVER_BUDGET".                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ 5. Input Validation & Result Clearing:                                      │
│    Action: Set D01 price to 0.                                              │
│    Result: Displays "INVALID_INPUT: Dishes, D01, Price"; all result         │
│            rows and counts are cleared.                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ 6. Reset State Synchronization:                                             │
│    Action: Click Reset after invalid price scenario.                        │
│    Result: Restores built-in rows, ₹150 budget, clears error state,         │
│            cleanses screen of stale outputs.                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 8. Technical Architecture for Android (Kotlin + Jetpack Compose)

### 8.1 Architecture Pattern: MVVM + Pure Domain Engine
```
   ┌──────────────────────────────────────────────────────────┐
   │                  Jetpack Compose UI                      │
   │  (Single scrollable screen: Header, Cards, Dialogs)      │
   └────────────────────────────▲─────────────────────────────┘
                                │ Observes UiState
                                │ Emits User Intents
   ┌────────────────────────────┴─────────────────────────────┐
   │                     BoardViewModel                       │
   │      (Manages StateFlow<BoardUiState>, Coroutines)       │
   └────────────────────────────▲─────────────────────────────┘
                                │ Calls Domain Logic
   ┌────────────────────────────┴─────────────────────────────┐
   │                  CompatibilityEngine                     │
   │   (Pure Kotlin: Validation, Normalization, Reason Sort)  │
   │          * 100% JVM Testable (<1s Execution) *           │
   └──────────────────────────────────────────────────────────┘
```

### 8.2 Why Pure Kotlin Domain Logic Matters
- **Decoupled Business Rules**: The `CompatibilityEngine` has zero Android SDK imports (`android.*`), allowing all business rules, ordering algorithms, and validation checks to be tested via **JUnit tests running on the local JVM in under 1 second**.
- **Fast Live Modification**: In a 30-minute interview where a live modification is required, changes can be made and validated in unit tests immediately without waiting for Gradle APK assembly or emulator launch.

### 8.3 Recommended Data Structures
- `DietClass` (Kotlin Enum): Encapsulates diet hierarchy and rules.
- `Resident` & `Dish` (Kotlin `data class`): Immutable models.
- `ExcludedDish` (Kotlin `data class`): Pairs a dish with its sorted string reasons.
- `BoardUiState` (Kotlin Sealed Interface): Encapsulates UI state transitions (`Initial`, `Success`, `Error`).

---

## 9. Interview Readiness & Evaluation Rubric Alignment

1. **Thoughtful AI Collaboration**: Presenting this structured specification proves clear problem translation into technical requirements.
2. **Code Ownership**: Kotlin and Jetpack Compose align with the candidate's existing background, ensuring confidence during code explanation.
3. **No Over-Engineering**: Strictly in-memory data structures, eliminating unnecessary database (Room) or network (Retrofit) dependencies.
4. **Testing Rigour**: JVM unit test suite verifying all 6 acceptance criteria and edge cases.
5. **Live Modification Ready**: Modular architecture allows adding new diet types, allergen aliases, or UI columns within minutes during the live interview session.
