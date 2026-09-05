# Hostel Food Compatibility Board 🍲

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Pure%20Domain%20Engine-blue.svg)](docs/ARCHITECTURE_PROPOSAL.md)
[![Tests](https://img.shields.io/badge/Tests-JUnit%20(JVM%20%3C1s)-orange.svg)](docs/phases/PHASE_1_DOMAIN_ENGINE_AND_MODELS.md)

An intelligent, single-screen decision-support Android application built with **Kotlin** and **Jetpack Compose** (Material 3). It enables a group of hostel residents to find campus cafe dishes that satisfy everyone's dietary requirements, allergen exclusions, and per-person budget constraints simultaneously.

Designed for the **Cisco AI-Assisted Coding Interview (Problem SI26_P02)**.

---

## 📑 Project Documentation Index

All architectural decisions, specifications, and phased implementation plans are fully documented:

| Document | Description |
| :--- | :--- |
| 📋 [**Technical Specification & Requirements**](docs/TECHNICAL_SPECIFICATION_AND_REQUIREMENTS.md) | Full problem contract, exact tag formats, normalization rules, and data specs |
| 🏛️ [**Architecture Proposal (MVVM)**](docs/ARCHITECTURE_PROPOSAL.md) | Component architecture, data flow, state management, and rationale |
| 📊 [**Problem Analysis Report**](docs/ANALYSIS_REPORT.md) | Initial problem breakdown, constraints, platform comparison, and evaluation rubric |
| 🗺️ [**Master 4-Phase Implementation Plan**](docs/IMPLEMENTATION_PLAN.md) | High-level roadmap with verification checkpoints for interview evaluation |

### 🚀 Implementation Phases
1. ⚙️ [**Phase 1: Domain Models & Pure Kotlin Engine**](docs/phases/PHASE_1_DOMAIN_ENGINE_AND_MODELS.md) - Models, normalization, rules, and sub-second JVM tests.
2. 🔄 [**Phase 2: ViewModel & State Management**](docs/phases/PHASE_2_VIEWMODEL_AND_STATE_MANAGEMENT.md) - StateFlow, search narrowing with count decoupling, and Reset state clearing.
3. 🎨 [**Phase 3: Jetpack Compose UI & Separated Components**](docs/phases/PHASE_3_COMPOSE_UI_AND_COMPONENTS.md) - Modular components (`SummaryHeaderCard`, `SearchBar`, `ResultsSection`, etc.).
4. 🧪 [**Phase 4: Acceptance Criteria Verification & Live Modification Drills**](docs/phases/PHASE_4_ACCEPTANCE_TESTING_AND_INTERVIEW_PREP.md) - 6 demo scenarios & live challenge prep.

---

## 🎯 The Core Business Rules

A dish is **Compatible** if and only if it passes all three checks across all residents:

| Rule Category | Verification Logic | Failure Reason Format |
| :--- | :--- | :--- |
| **1. Dietary Rule** | • `VEGAN` resident accepts **only** `VEGAN` dishes.<br>• `VEGETARIAN` resident accepts `VEGAN` or `VEGETARIAN` dishes.<br>• `NO_RESTRICTION` resident accepts any dish. | `DIET:<residentName>`<br>*(e.g., `DIET:Asha`)* |
| **2. Allergen Rule** | Exact literal match between normalized dish `ingredientTags` and resident `allergens`. Authoritative tags only (no inference). | `ALLERGEN:<residentName>:<matchedTag>`<br>*(e.g., `ALLERGEN:Mira:MILK`)* |
| **3. Budget Rule** | Dish price $\le$ Per-person group budget. (Never multiplied by group size). | `OVER_BUDGET` |

### Deterministic Reason Ordering Contract:
Excluded dish reasons must strictly follow this exact order:
1. **Resident Table Order** (Asha $\to$ Dev $\to$ Mira).
2. **Category Order per Resident**: `DIET` reason must precede `ALLERGEN` reason(s).
3. **Ingredient Tag Order**: Allergens ordered according to the dish's original ingredient declaration order.
4. **Budget Last**: `OVER_BUDGET` is always placed at the very end.

---

## 🏗️ Architecture: MVVM with Pure Domain Engine

```
┌─────────────────────────────────────────────────────────────┐
│ 1. UI Layer (Jetpack Compose Material 3)                    │
│    • BoardScreen.kt (Single-screen dashboard coordinator)   │
│    • Modular components in ui/components/                   │
└──────────────────────────────▲──────────────────────────────┘
                               │ Observes BoardScreenState (StateFlow)
                               │ Emits User Events (Calculate, Edit, Search, Reset)
┌──────────────────────────────┴──────────────────────────────┐
│ 2. ViewModel Layer (Android Lifecycle ViewModel)            │
│    • BoardViewModel.kt                                      │
│    • Holds in-memory state directly (Zero Repositories)     │
│    • Search query narrowing with count decoupling           │
└──────────────────────────────▲──────────────────────────────┘
                               │ Calls Pure Business Logic
┌──────────────────────────────┴──────────────────────────────┐
│ 3. Domain Engine Layer (Pure Kotlin - Zero Android SDK)     │
│    • CompatibilityEngine.kt                                 │
│    • Input validation (INVALID_INPUT, DUPLICATE_DISH_ID)    │
│    • Isolated rule functions: checkDiet, checkAllergens...  │
│    • 100% JVM Unit Testable in <1s                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 The 6 Mandatory Acceptance Criteria

| # | Demo Action | Expected Screen Output | Contract Verification |
| :---: | :--- | :--- | :--- |
| **1** | **Load Built-in** | Shows **D01** then **D02**; Compatible Count = **`2`**. | Source order preserved. |
| **2** | **Check Exclusions** | • D03: `DIET:Asha, ALLERGEN:Mira:MILK`<br>• D04: `ALLERGEN:Dev:PEANUT`<br>• D05: `DIET:Asha, DIET:Dev`<br>• D02 (₹150) passes ₹150 budget. | Exact strings match specification. |
| **3** | **Search Filter** | Type `"wheat"` $\to$ displays only **D02**.<br>⚠️ **Compatible Count STAYS `2`**.<br>Clear search $\to$ D01 & D02 show again. | Count is decoupled from filtered view. |
| **4** | **Budget Shift** | Set budget to ₹130 $\to$ only D01 compatible; D02 marked `OVER_BUDGET`. | Boundary price transition. |
| **5** | **Invalid Input** | Set D01 price to 0 $\to$ `INVALID_INPUT: Dishes, D01, Price`.<br>⚠️ **All result rows and counts are cleared**. | Output clearing contract. |
| **6** | **Reset State** | Click Reset $\to$ restores defaults and clears all calculated output until run again. | Reset synchronization contract. |

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose (BOM 2026.02.01) + Material 3
- **State Management**: Android `ViewModel` + `StateFlow`
- **Testing**: JUnit 4 (Running on local JVM in <1s)
- **Dependencies**: 100% standard Jetpack libraries (No Room, no Retrofit, no Dagger/Hilt needed)

---

## ⚡ Running Automated Unit Tests

To run the full pure Kotlin domain engine test suite:
```bash
./gradlew testDebugUnitTest
```
*Tests execute in under 1.5 seconds directly on your machine's JVM without requiring an emulator.*
