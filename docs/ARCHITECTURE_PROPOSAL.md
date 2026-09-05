# Application Architecture Specification (MVVM)
**Project**: SI26_P02: Hostel Food Compatibility Board  
**Target Environment**: Android (Kotlin + Jetpack Compose)  
**Location**: `C:\Users\KIIT\OneDrive\Desktop\CISCO Prob Statement\Md files`  
**Pattern**: Clean MVVM (Zero Repositories, In-Memory Domain Engine, Modular Separated UI Components, Direct Activity-to-Screen)  
**Date**: September 2026  

---

## 1. Architectural Principles & Review Improvements

Following our architectural review against the Cisco requirements and live interview conditions, the architecture is refined with these core principles:

1. **Separated, Searchable UI Components (`ui/components/`)**:
   Instead of dumping all UI into one oversized file, each major visual component is kept in its own dedicated, easily searchable file (e.g., `DishSection.kt`, `ResultsSection.kt`, `SummaryHeaderCard.kt`).
2. **Zero Navigation Overhead**:
   Since the app is strictly a single-screen dashboard, we avoid adding `androidx.navigation:navigation-compose`. `MainActivity` directly sets `BoardScreen()`. This eliminates unnecessary Gradle dependencies, route boilerplate, and potential navigation backstack bugs.
3. **No Unnecessary Repositories**:
   Data is strictly in-memory. The `ViewModel` directly coordinates state with `SampleData` and the pure Kotlin `CompatibilityEngine`.
4. **Contract-Compliant Reset & Load Actions**:
   - **Reset**: Restores built-in rows, ₹150 budget, and empty search, **and clears calculated outputs and counts until compatibility is run again**.
   - **One-Action Load**: A dedicated "Load Built-in & Calculate" action that populates default data and displays results immediately in one tap (satisfying Acceptance Criterion 1).
5. **Isolated Rule Functions for Live Modification**:
   Rules inside `CompatibilityEngine` are written as independent, modular functions (`checkDiet`, `checkAllergens`, `checkBudget`). If the interviewer asks for a surprise modification, you only edit one 3-line function and verify it via JUnit in <1 second.

---

## 2. Package & File Directory Structure

```
com.pratyush.hostelfood_compatibility_board/
│
├── model/                               # Domain & Data Models
│   ├── DietClass.kt                     # Enum: VEGAN, VEGETARIAN, NON_VEGETARIAN, NO_RESTRICTION
│   ├── Resident.kt                      # Resident data class (name, diet, allergens)
│   ├── Dish.kt                          # Dish data class (id, cafe, name, diet, tags, price)
│   ├── ExcludedDish.kt                  # Excluded dish pairing with sorted reason strings
│   └── EngineResult.kt                  # Sealed interface: Success vs Failure (error codes)
│
├── data/                                # Initial Data
│   └── SampleData.kt                    # Built-in default datasets (Asha, Dev, Mira, D01-D05, ₹150)
│
├── engine/                              # Pure Kotlin Business Logic (Zero Android SDK)
│   └── CompatibilityEngine.kt           # Isolated Rule Functions: checkDiet, checkAllergens, checkBudget
│
├── viewmodel/                           # State & Presentation Logic
│   ├── BoardScreenState.kt              # Single unified immutable UI state
│   └── BoardViewModel.kt                # StateFlow, User Intents, Count Independence
│
├── ui/
│   ├── screens/
│   │   └── BoardScreen.kt               # Main Single-Screen Coordinator & Scaffold
│   │
│   ├── components/                      # Separated, Modular UI Components
│   │   ├── SummaryHeaderCard.kt         # Compatible count badge, budget display, Action Buttons
│   │   ├── SearchBarComponent.kt        # Outlined search field with clear (X) action
│   │   ├── ResidentSection.kt           # Group cards with diet and allergen chips
│   │   ├── DishSection.kt               # Dish list items showing cafe, tags, prices & edit button
│   │   ├── ResultsSection.kt            # Compatible cards (green) & Excluded cards with reasons
│   │   └── EditDishDialog.kt            # Dialog for price/ingredient editing & boundary testing
│   │
│   └── theme/                           # Material 3 Theme (Already in your project)
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── MainActivity.kt                      # setContent { BoardScreen() }
```

---

## 3. Data Models (`model/`)

```kotlin
// model/DietClass.kt
enum class DietClass {
    VEGAN,
    VEGETARIAN,
    NON_VEGETARIAN,
    NO_RESTRICTION; // Valid for residents only

    fun accepts(dishDiet: DietClass): Boolean = when (this) {
        VEGAN -> dishDiet == VEGAN
        VEGETARIAN -> dishDiet == VEGAN || dishDiet == VEGETARIAN
        NON_VEGETARIAN -> true
        NO_RESTRICTION -> true
    }
}

// model/Resident.kt
data class Resident(
    val name: String,
    val diet: DietClass,
    val allergens: List<String> // Normalized uppercase strings (e.g. ["PEANUT"])
)

// model/Dish.kt
data class Dish(
    val id: String,
    val cafe: String,
    val name: String,
    val diet: DietClass,
    val ingredientTags: List<String>, // Normalized uppercase strings
    val price: Int                   // Positive whole rupee integer
)

// model/ExcludedDish.kt
data class ExcludedDish(
    val dish: Dish,
    val reasons: List<String> // e.g. ["DIET:Asha", "ALLERGEN:Mira:MILK", "OVER_BUDGET"]
)

// model/EngineResult.kt
sealed interface EngineResult {
    data class Success(
        val compatibleDishes: List<Dish>,
        val excludedDishes: List<ExcludedDish>
    ) : EngineResult

    data class Failure(
        val errorCode: String, // "INVALID_INPUT" or "DUPLICATE_DISH_ID"
        val message: String    // e.g. "INVALID_INPUT: Dishes, D01, Price"
    ) : EngineResult
}
```

---

## 4. In-Memory Data (`data/SampleData.kt`)

```kotlin
// data/SampleData.kt
object SampleData {
    const val DEFAULT_BUDGET = 150

    fun defaultResidents(): List<Resident> = listOf(
        Resident("Asha", DietClass.VEGAN, emptyList()), // "none" parsed as empty
        Resident("Dev", DietClass.VEGETARIAN, listOf("PEANUT")),
        Resident("Mira", DietClass.NO_RESTRICTION, listOf("MILK"))
    )

    fun defaultDishes(): List<Dish> = listOf(
        Dish("D01", "Hostel Cafe", "Lentil Rice Bowl", DietClass.VEGAN, listOf("LENTIL", "RICE", "SPINACH"), 110),
        Dish("D02", "Library Cafe", "Tomato Pasta", DietClass.VEGAN, listOf("WHEAT", "TOMATO"), 150),
        Dish("D03", "Hostel Cafe", "Paneer Wrap", DietClass.VEGETARIAN, listOf("MILK", "WHEAT"), 140),
        Dish("D04", "East Cafe", "Peanut Noodles", DietClass.VEGAN, listOf("PEANUT", "WHEAT"), 130),
        Dish("D05", "Library Cafe", "Egg Sandwich", DietClass.NON_VEGETARIAN, listOf("EGG", "WHEAT"), 100)
    )
}
```

---

## 5. Pure Domain Engine (`engine/CompatibilityEngine.kt`)

Rules are split into **modular, single-responsibility functions** for live modification safety:

```kotlin
// engine/CompatibilityEngine.kt
object CompatibilityEngine {

    fun evaluate(
        residents: List<Resident>,
        dishes: List<Dish>,
        budget: Int
    ): EngineResult {
        // 1. Validation Checks
        val validationError = validateInputs(residents, dishes, budget)
        if (validationError != null) return validationError

        val compatible = mutableListOf<Dish>()
        val excluded = mutableListOf<ExcludedDish>()

        // 2. Evaluate each dish in source order
        for (dish in dishes) {
            val reasons = mutableListOf<String>()

            // A. Resident checks in Resident Table order
            for (resident in residents) {
                // Diet check before Allergen check
                checkDiet(resident, dish)?.let { reasons.add(it) }

                // Allergen check in Dish Ingredient order
                reasons.addAll(checkAllergens(resident, dish))
            }

            // B. Budget check (always last)
            checkBudget(dish, budget)?.let { reasons.add(it) }

            // C. Classification
            if (reasons.isEmpty()) {
                compatible.add(dish)
            } else {
                excluded.add(ExcludedDish(dish, reasons))
            }
        }

        return EngineResult.Success(compatible, excluded)
    }

    // --- Isolated Rule Functions (Easy to modify live in interview!) ---

    fun checkDiet(resident: Resident, dish: Dish): String? {
        return if (!resident.diet.accepts(dish.diet)) "DIET:${resident.name}" else null
    }

    fun checkAllergens(resident: Resident, dish: Dish): List<String> {
        val matches = mutableListOf<String>()
        for (ingredient in dish.ingredientTags) {
            val normalized = ingredient.trim().uppercase()
            if (resident.allergens.any { it.trim().uppercase() == normalized }) {
                matches.add("ALLERGEN:${resident.name}:$normalized")
            }
        }
        return matches
    }

    fun checkBudget(dish: Dish, budget: Int): String? {
        return if (dish.price > budget) "OVER_BUDGET" else null
    }

    // --- Strict Validation ---

    fun validateInputs(residents: List<Resident>, dishes: List<Dish>, budget: Int): EngineResult.Failure? {
        if (budget <= 0) {
            return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Budget, Global, Budget")
        }

        val seenIds = mutableSetOf<String>()
        for (dish in dishes) {
            val trimmedId = dish.id.trim()
            if (trimmedId.isEmpty()) return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Dishes, ${dish.id}, ID")
            if (!seenIds.add(trimmedId)) {
                return EngineResult.Failure("DUPLICATE_DISH_ID", "DUPLICATE_DISH_ID: $trimmedId")
            }
            if (dish.cafe.trim().isEmpty()) return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Dishes, ${dish.id}, Cafe")
            if (dish.name.trim().isEmpty()) return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Dishes, ${dish.id}, Dish")
            if (dish.price <= 0) return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Dishes, ${dish.id}, Price")
            if (dish.ingredientTags.isEmpty() || dish.ingredientTags.any { it.trim().isEmpty() }) {
                return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Dishes, ${dish.id}, Ingredients")
            }
        }

        for (resident in residents) {
            if (resident.name.trim().isEmpty()) {
                return EngineResult.Failure("INVALID_INPUT", "INVALID_INPUT: Residents, ${resident.name}, Name")
            }
        }

        return null
    }
}
```

---

## 6. ViewModel & State (`viewmodel/`)

```kotlin
// viewmodel/BoardScreenState.kt
data class BoardScreenState(
    val residents: List<Resident> = SampleData.defaultResidents(),
    val dishes: List<Dish> = SampleData.defaultDishes(),
    val budget: Int = SampleData.DEFAULT_BUDGET,
    val searchQuery: String = "",
    val isCalculated: Boolean = false,
    val totalCompatibleCount: Int = 0,             // Unfiltered total count (Stays 2 during search)
    val allCompatibleDishes: List<Dish> = emptyList(),
    val displayedCompatibleDishes: List<Dish> = emptyList(), // Narrowed by search query
    val excludedDishes: List<ExcludedDish> = emptyList(),
    val errorMessage: String? = null,
    val selectedDishForEdit: Dish? = null          // Controls Edit Dialog visibility
)

// viewmodel/BoardViewModel.kt
class BoardViewModel : ViewModel() {

    private val _state = MutableStateFlow(BoardScreenState())
    val state: StateFlow<BoardScreenState> = _state.asStateFlow()

    fun onLoadBuiltInAndCalculate() {
        _state.value = BoardScreenState(
            residents = SampleData.defaultResidents(),
            dishes = SampleData.defaultDishes(),
            budget = SampleData.DEFAULT_BUDGET,
            searchQuery = "",
            isCalculated = false
        )
        calculateCompatibility()
    }

    fun onCalculateClicked() = calculateCompatibility()

    fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            val filtered = if (query.isBlank()) {
                current.allCompatibleDishes
            } else {
                current.allCompatibleDishes.filter { dish ->
                    dish.cafe.contains(query, ignoreCase = true) ||
                    dish.name.contains(query, ignoreCase = true) ||
                    dish.ingredientTags.any { it.contains(query, ignoreCase = true) }
                }
            }
            // totalCompatibleCount remains based on unfiltered list
            current.copy(
                searchQuery = query,
                displayedCompatibleDishes = filtered
            )
        }
    }

    fun onBudgetChanged(newBudget: Int) {
        _state.update { it.copy(budget = newBudget) }
        calculateCompatibility()
    }

    fun onUpdateDish(updatedDish: Dish) {
        _state.update { current ->
            val updatedList = current.dishes.map { if (it.id == updatedDish.id) updatedDish else it }
            current.copy(dishes = updatedList, selectedDishForEdit = null)
        }
        calculateCompatibility()
    }

    fun onSelectDishForEdit(dish: Dish?) {
        _state.update { it.copy(selectedDishForEdit = dish) }
    }

    // Strictly adheres to Reset Contract: Clears calculations until run again
    fun onResetClicked() {
        _state.value = BoardScreenState(
            residents = SampleData.defaultResidents(),
            dishes = SampleData.defaultDishes(),
            budget = SampleData.DEFAULT_BUDGET,
            searchQuery = "",
            isCalculated = false,
            totalCompatibleCount = 0,
            allCompatibleDishes = emptyList(),
            displayedCompatibleDishes = emptyList(),
            excludedDishes = emptyList(),
            errorMessage = null,
            selectedDishForEdit = null
        )
    }

    private fun calculateCompatibility() {
        val current = _state.value
        when (val result = CompatibilityEngine.evaluate(current.residents, current.dishes, current.budget)) {
            is EngineResult.Success -> {
                val filtered = if (current.searchQuery.isBlank()) {
                    result.compatibleDishes
                } else {
                    result.compatibleDishes.filter { dish ->
                        dish.cafe.contains(current.searchQuery, ignoreCase = true) ||
                        dish.name.contains(current.searchQuery, ignoreCase = true) ||
                        dish.ingredientTags.any { it.contains(current.searchQuery, ignoreCase = true) }
                    }
                }
                _state.update {
                    it.copy(
                        isCalculated = true,
                        totalCompatibleCount = result.compatibleDishes.size,
                        allCompatibleDishes = result.compatibleDishes,
                        displayedCompatibleDishes = filtered,
                        excludedDishes = result.excludedDishes,
                        errorMessage = null
                    )
                }
            }
            is EngineResult.Failure -> {
                // Strict Clearing Contract: Clear all compatible/excluded rows and counts
                _state.update {
                    it.copy(
                        isCalculated = false,
                        totalCompatibleCount = 0,
                        allCompatibleDishes = emptyList(),
                        displayedCompatibleDishes = emptyList(),
                        excludedDishes = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
```

---

## 7. Separated UI Components (`ui/components/`)

Each component has a clear purpose and dedicated file:

1. **`SummaryHeaderCard.kt`**:
   - Displays title, current budget indicator, action buttons (**"Load Built-in"**, **"Calculate"**, **"Reset"**), and the prominent **Compatible Count badge** (`Compatible: 2`).
2. **`SearchBarComponent.kt`**:
   - `OutlinedTextField` with leading search icon and trailing clear (`X`) button that instantly clears the search query.
3. **`ResidentSection.kt`**:
   - Clean card section displaying Asha, Dev, Mira with their diet badges and allergen tags.
4. **`DishSection.kt`**:
   - Clean card list showing each dish's Cafe, Name, Diet class, Ingredient chips, Price, and an **Edit** button.
5. **`ResultsSection.kt`**:
   - Displays:
     - **Compatible Dishes**: Light green cards with a checkmark badge in source order.
     - **Excluded Dishes**: Subtle gray/red cards displaying each exact contracted reason tag (`DIET:Asha`, `ALLERGEN:Mira:MILK`, `OVER_BUDGET`).
6. **`EditDishDialog.kt`**:
   - Simple modal dialog with fields for Dish Name, Price, Cafe, and Ingredients, allowing quick edits to test budget boundaries (₹130) or invalid prices (`0`).

---

## 8. Screen Coordinator & MainActivity

### `ui/screens/BoardScreen.kt`
```kotlin
@Composable
fun BoardScreen(viewModel: BoardViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hostel Food Compatibility Board") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header & Actions
            item {
                SummaryHeaderCard(
                    state = state,
                    onLoadBuiltIn = viewModel::onLoadBuiltInAndCalculate,
                    onCalculate = viewModel::onCalculateClicked,
                    onReset = viewModel::onResetClicked,
                    onBudgetChanged = viewModel::onBudgetChanged
                )
            }

            // 2. Error Banner if invalid input
            state.errorMessage?.let { error ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 3. Search Bar
            item {
                SearchBarComponent(
                    query = state.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
            }

            // 4. Resident Group
            item { ResidentSection(residents = state.residents) }

            // 5. Dish List
            item {
                DishSection(
                    dishes = state.dishes,
                    onEditDish = viewModel::onSelectDishForEdit
                )
            }

            // 6. Results (Only shown when calculated and no error)
            if (state.isCalculated && state.errorMessage == null) {
                item {
                    ResultsSection(
                        totalCount = state.totalCompatibleCount,
                        compatibleDishes = state.displayedCompatibleDishes,
                        excludedDishes = state.excludedDishes
                    )
                }
            }
        }
    }

    // Modal Edit Dialog
    state.selectedDishForEdit?.let { dish ->
        EditDishDialog(
            dish = dish,
            onDismiss = { viewModel.onSelectDishForEdit(null) },
            onSave = viewModel::onUpdateDish
        )
    }
}
```

### `MainActivity.kt`
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelFoodCompatibilityBoardTheme {
                BoardScreen()
            }
        }
    }
}
```

---

## 9. Summary of Advantages for the Interview

1. **Clean Codebase Navigation**: Separate component files mean you can locate and open `DishSection.kt` or `ResultsSection.kt` in 1 second during screen sharing.
2. **Zero Gradle Sync Surprises**: No extra libraries or Compose Navigation needed.
3. **Strict Contract Alignment**: Fully respects Reset clearing and single-action built-in load.
4. **Live-Modification Resilience**: Modifying diet or allergen rules happens in pure, isolated Kotlin functions with instant JVM unit test feedback.
