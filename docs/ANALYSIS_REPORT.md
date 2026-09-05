# Problem Analysis Report: Hostel Food Compatibility Board
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Folder**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement`  
**Date**: September 2026

---

## 1. What the Problem Requires

The objective is to build a compact **Hostel Food Compatibility Board** for hostel residents who need to choose a dish that everyone in the group can eat.

The application evaluates a candidate set of dishes against:
1. **Dietary Restrictions** (Vegan, Vegetarian, Non-Vegetarian, No-Restriction)
2. **Allergen Exclusions** (Exact tag matching)
3. **Per-Person Budget** (Positive whole rupee amount)

It must output:
- Exactly which dishes are **compatible** (preserving source order)
- An accurate **compatible-count summary**
- Exact, contracted **exclusion reasons** for every incompatible dish
- A **search filter** that narrows displayed compatible dishes without mutating the overall compatible count
- **Sample data load**, **interactive table/form editing**, and a full **Reset** mechanism
- Strict **validation** producing specified error codes on invalid inputs

---

## 2. Main Functional Requirements

### 2.1 Single Primary Screen Layout
The application must feature a single attractive view containing:
- **Group Table / Form**: Resident Name, Diet Class, Allergens list.
- **Dish Table / Form**: Dish ID, Cafe Name, Dish Name, Diet Class, Ingredient Tags, Price per serving.
- **Budget Control**: A positive integer input for the per-person group budget (Default: ₹150).
- **Compatibility Action**: Trigger button ("Calculate Compatibility" / auto-sync).
- **Result Area**:
  - **Compatible Dishes**: List of dishes passing all rules in their original source order.
  - **Overall Compatible Count Summary**: Prominent counter showing total compatible dishes.
  - **Excluded Dishes & Exact Reasons**: Clearly displaying why each dish failed.
- **Focused Search Box**: Substring filter for compatible dishes.
- **Sample / Reset Controls**: Quick actions to load defaults or reset state.

### 2.2 In-Memory Pre-loaded Data
* **Default Budget**: ₹150 per person.
* **Residents**:
  1. `Asha` | Diet: `VEGAN` | Allergens: `none`
  2. `Dev` | Diet: `VEGETARIAN` | Allergens: `PEANUT`
  3. `Mira` | Diet: `NO_RESTRICTION` | Allergens: `MILK`
* **Dishes** (in exact source order):
  1. `D01` | `Hostel Cafe` | `Lentil Rice Bowl` | `VEGAN` | `LENTIL, RICE, SPINACH` | ₹110
  2. `D02` | `Library Cafe` | `Tomato Pasta` | `VEGAN` | `WHEAT, TOMATO` | ₹150
  3. `D03` | `Hostel Cafe` | `Paneer Wrap` | `VEGETARIAN` | `MILK, WHEAT` | ₹140
  4. `D04` | `East Cafe` | `Peanut Noodles` | `VEGAN` | `PEANUT, WHEAT` | ₹130
  5. `D05` | `Library Cafe` | `Egg Sandwich` | `NON_VEGETARIAN` | `EGG, WHEAT` | ₹100

### 2.3 Compatibility Evaluation Logic
A dish is compatible **if and only if** it passes:
1. **Diet Rule** for *all* residents.
2. **Allergen Rule** for *all* residents.
3. **Budget Rule** (Dish price $\le$ Budget).

---

## 3. Important Constraints & Exact Contracts

| Area | Contract / Rule Specification |
| :--- | :--- |
| **Data Normalization** | Trim whitespace and convert to **UPPERCASE** for diet classes, ingredient tags, and allergen tags. |
| **Diet Hierarchy** | • `VEGAN` resident: accepts **only** `VEGAN` dishes.<br>• `VEGETARIAN` resident: accepts `VEGAN` or `VEGETARIAN` dishes.<br>• `NO_RESTRICTION` resident: accepts any dish class (`VEGAN`, `VEGETARIAN`, `NON_VEGETARIAN`).<br>*Note*: `NO_RESTRICTION` is only valid for residents, not dishes. |
| **Allergen Matching** | Exact normalized string equality between dish ingredient tag and resident allergen tag.<br>• **Strictly authoritative**: No inferring ingredients, no aliases, no cross-contamination logic. |
| **Budget Rule** | Maximum allowed price for **one serving for one resident**.<br>• Dish price $\le$ Budget passes.<br>• **Do not** multiply by group size. |
| **Exclusion Reason Formats** | • Diet failure: `DIET:<resident>`<br>• Allergen failure: `ALLERGEN:<resident>:<tag>`<br>• Over budget: `OVER_BUDGET` |
| **Reason Ordering Contract** | For each dish, order reasons strictly by:<br>1. Resident order in resident table.<br>2. For each resident: `DIET` reason comes before `ALLERGEN` reason(s).<br>3. Matched allergens ordered according to the **dish's ingredient tag order**.<br>4. `OVER_BUDGET` is always listed **last**. |
| **Search Filter** | • Case-insensitive substring match against Cafe, Dish Name, or any Ingredient Tag.<br>• Applied **only to compatible dishes**.<br>• **Critical**: The overall compatible count badge/summary **must remain based on the unfiltered compatible list** even when search narrows displayed items. |
| **Validation & Error Handling** | • Non-empty strings required for names, IDs, cafes, ingredients.<br>• Dish IDs must be unique. Duplicate ID $\to$ report `DUPLICATE_DISH_ID`.<br>• Budget and prices must be positive whole rupee integers ($> 0$).<br>• On invalid input $\to$ report `INVALID_INPUT` with affected **table**, **row**, and **field**.<br>• **Clearing rule**: On error or duplicate ID, clear all compatibility/exclusion rows and reset count to 0 / hide it. |
| **Reset Behavior** | Restores built-in group, dishes, ₹150 budget, empty search, and clears all error and calculation outputs until compatibility is executed. |

---

## 4. What Needs to be Demonstrated

The interview evaluation is based on **6 Required Acceptance Scenarios** + **1 Optional Enhancement**:

1. **Built-in Run**:
   - Load built-in data in 1 action $\to$ displays **D01** followed by **D02**, compatible count = `2`.
2. **Contracted Exclusion Verification**:
   - `D03`: `DIET:Asha, ALLERGEN:Mira:MILK`
   - `D04`: `ALLERGEN:Dev:PEANUT`
   - `D05`: `DIET:Asha, DIET:Dev`
   - Demonstrate boundary pricing: **D02 (₹150)** passes the ₹150 budget.
3. **Deterministic Search Narrowing**:
   - Enter search query `wheat` $\to$ display **only D02**.
   - Verify overall compatible count **remains 2**.
   - Clear query $\to$ both D01 and D02 reappear.
4. **Budget Boundary Change**:
   - Change budget to **₹130** $\to$ only **D01** compatible; **D02** becomes excluded with reason `OVER_BUDGET`.
5. **Invalid Input Handling**:
   - Change D01 price to `0` $\to$ display `INVALID_INPUT` identifying the D01 row and price field.
   - Verify all result rows and counts are cleared.
6. **Reset State Synchronization**:
   - Click Reset after invalid price $\to$ restore built-in rows, ₹150 budget, empty search, no stale errors or stale results.
7. **(Optional UI Feature)**:
   - Compact visual evidence chips (Diet check chip, Allergen check chip, Budget check chip) without altering the contracted text results.

### Interview Format & Live Modification Expectations
- **Time window**: 30–40 minutes live interview.
- **Evaluation pillars**:
  1. Presentation of a clean 3–5 step plan.
  2. AI prompting strategy and engineering rigor.
  3. Design choices, data structures, and edge-case handling.
  4. **Live modification**: Implementing 1 or 2 surprise live feature modifications in real-time during the call with AI assistance.

---

## 5. Whether Web or Android is a Better Fit

| Criteria | Web (React + TypeScript + Vite / Tailwind) | Android (Kotlin / Jetpack Compose) |
| :--- | :--- | :--- |
| **Screen Space & Layout** | **Exceptional**. Wide desktop display accommodates Resident table, Dish table (with 6 columns), controls, and side-by-side results without cramped scrolling. | **Challenging**. Fitting two multi-column editable tables + search + results on a vertical phone screen requires heavy nesting, tabs, or endless vertical scrolling. |
| **Interview Screen Share** | Direct display in Chrome / Edge; interviewer can read every column and reason clearly without emulator scaling issues. | Requires running Android Emulator or screen mirroring (scrcpy), which adds screen clutter and resolution scaling. |
| **Build & Iteration Speed** | **Instant (Vite HMR < 50ms)**. Zero waiting time when making changes. | **Slow (Gradle build 20s–90s)** per compilation / cold deploy. |
| **Live Modification Risk** | **Very Low Risk**. In a 30-minute interview, you can modify logic, add a column or rule, save, and see results instantly without losing interview momentum. | **High Risk**. Gradle sync issues, emulator freezes, or build slowdowns can consume 5–10 minutes of a 30-minute interview. |
| **Testing Speed** | Fast Vitest/Jest suite executes in under 1 second in the terminal. | JVM unit tests run fast, but UI instrumented tests are slow. |
| **Problem Statement Fit** | The specification explicitly says: *"You may use in-memory data structures, a spreadsheet or notebook, a browser, desktop or mobile tools, or a CLI..."* No mobile-specific features (camera, GPS, sensors) are needed. | Fits the prompt, but adds unnecessary complexity and latency. |

> **Conclusion**: **Web is significantly better suited** for this interview task. It offers better desktop visibility for complex tables, zero build friction, instant Hot Reload during the live modification phase, and clean unit testing.  
> *(Note: If the interview is explicitly for an Android Developer role, Android Jetpack Compose would be mandatory; otherwise, for general software engineering, Web is the clear winner).*

---

## 6. Recommended Tech Stack and Why

### Recommended Stack
- **Framework**: **React (with Vite)** or **Next.js / Vanilla TypeScript**
- **Language**: **TypeScript** (Strict mode)
- **Styling**: **Tailwind CSS** + **Lucide Icons**
- **Testing**: **Vitest** (or Jest) for deterministic automated contract testing

### Why this Stack?
1. **Pure In-Memory Domain Model**:
   TypeScript interfaces provide strict type-checking for domain objects (`Resident`, `Dish`, `CompatibilityResult`, `ValidationError`), eliminating runtime property typos.
2. **Instant Live Modifications**:
   Vite's Hot Module Replacement allows instant code changes within seconds during the live coding section of the interview.
3. **Tailwind for Compact "Compatibility Board" Aesthetic**:
   Effortlessly format tables, badges, status chips, and error banners into a clean, modern, single-view dashboard.
4. **Deterministic Unit Tests**:
   Vitest can execute test cases representing all 6 required acceptance scenarios (Boundary budget ₹150 vs ₹130, D01 price = 0, wheat search query, exact reason string formatting) with single-command verification (`npm test`).
