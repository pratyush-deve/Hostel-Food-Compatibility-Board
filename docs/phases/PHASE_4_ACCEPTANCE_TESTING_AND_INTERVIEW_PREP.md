# Phase 4: Acceptance Testing, Evidence & Live Modification Prep
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement\Md files\phases`  
**Date**: September 2026  

---

## 1. Goal

Execute and verify all **6 mandatory acceptance scenarios** on the running app, collect test evidence, rehearse the interview presentation, and prepare the development environment for surprise live modifications during the 30–40 minute interview.

---

## 2. Main Tasks

### Task 4.1: Execute the 6 Mandatory Acceptance Tests (Live Script)

Follow this exact test sequence during your demo:

| # | Demo Step | User Action in UI | Expected Result on Screen | Contract Check |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Built-in Run** | Tap **"Load Built-in"** button | Compatible list shows **D01** then **D02**.<br>Compatible count badge shows **`2`**. | Source order preserved; total count correct. |
| **2** | **Exclusion & Boundary Check** | Scroll to Excluded Section | • **D03**: `DIET:Asha, ALLERGEN:Mira:MILK`<br>• **D04**: `ALLERGEN:Dev:PEANUT`<br>• **D05**: `DIET:Asha, DIET:Dev`<br>• D02 (₹150) passes ₹150 budget. | Exact strings match problem statement character-for-character. |
| **3** | **Search Narrowing** | Type `"wheat"` into Search Bar | • Only **D02** is displayed in the list.<br>• **Compatible count badge STAYS `2`**.<br>• Tap 'X' to clear query $\to$ both **D01** & **D02** reappear. | Filter decouples from total compatible count. |
| **4** | **Budget Change** | Change Budget to **₹130** and tap "Calculate" | • Only **D01** is compatible (Count = `1`).<br>• **D02** moves to Excluded with reason `OVER_BUDGET`. | Boundary price transition verified. |
| **5** | **Invalid Input Handling** | Tap Edit on D01 $\to$ Change Price to **`0`** $\to$ Save | • Red error banner appears: `INVALID_INPUT: Dishes, D01, Price`.<br>• **All result rows and counts are cleared immediately**. | Strict state clearing rule verified. |
| **6** | **Reset Synchronization** | Tap **"Reset"** button | • Built-in data restored, budget = ₹150, search blank.<br>• Error banner dismissed.<br>• **Screen clean with no stale results until calculated again**. | Reset state synchronization contract verified. |

---

### Task 4.2: Live Modification Drills (Preparation for Interviewer Challenge)

The interview format reserves time for 1–2 surprise live modifications. Rehearse these common drill patterns:

#### Drill Scenario A: "Add a new dietary class (e.g. PESCATARIAN)"
- **Where to edit**: `model/DietClass.kt` + `engine/CompatibilityEngine.kt` (`checkDiet()`).
- **Steps**:
  1. Add `PESCATARIAN` to `DietClass` enum.
  2. In `DietClass.accepts()`, add rule: `PESCATARIAN accepts VEGAN, VEGETARIAN, or PESCATARIAN`.
  3. Run JUnit unit test in terminal (`./gradlew testDebugUnitTest`) in 1 second.
  4. Show interviewer the test passing before even touching the UI!

#### Drill Scenario B: "Add an Allergen Alias / Parent Group rule (e.g. DAIRY matches MILK)"
- **Where to edit**: `engine/CompatibilityEngine.kt` (`checkAllergens()`).
- **Steps**:
  1. Add an alias mapping: `val aliases = mapOf("DAIRY" to listOf("MILK", "CHEESE"))`.
  2. Update condition in `checkAllergens()`.
  3. Re-run JVM unit test to verify in seconds.

#### Drill Scenario C: "Add a maximum calories constraint"
- **Where to edit**: Add `calories: Int` to `Dish`, add `maxCalories: Int` to `evaluate()`, add `checkCalories()` in `CompatibilityEngine.kt`.

---

### Task 4.3: Presentation Talking Points (Scoring the Rubric)

Be prepared to explain:
1. **Plan & Execution**: "I broke the solution into 4 disciplined phases: Domain Engine, ViewModel State, Compose UI, and Acceptance Verification."
2. **Design Constraints**: "I chose MVVM with a pure Kotlin engine and zero repositories because the application is 100% in-memory with no APIs. This eliminated over-engineering."
3. **Testing Strategy**: "Because the domain engine has zero Android SDK imports, the entire business logic and edge cases are validated on the JVM in under 1 second."
4. **Contract Adherence**: "I paid special attention to exact string formatting (`DIET:<resident>`, `OVER_BUDGET`), reason ordering, search count decoupling, and strict output clearing on errors."

---

## 3. Checkpoint & Final Verification

### Success Criteria:
- [x] All 6 acceptance scenarios demonstrated end-to-end.
- [x] JVM unit tests execute in <1s.
- [x] Live modification drill executed confidently in <3 minutes.
- [x] Development environment hot and ready for the interview call.
