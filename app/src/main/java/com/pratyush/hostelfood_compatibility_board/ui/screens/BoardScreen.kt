package com.pratyush.hostelfood_compatibility_board.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pratyush.hostelfood_compatibility_board.ui.components.DishSection
import com.pratyush.hostelfood_compatibility_board.ui.components.EditDishDialog
import com.pratyush.hostelfood_compatibility_board.ui.components.ResidentSection
import com.pratyush.hostelfood_compatibility_board.ui.components.ResultsSection
import com.pratyush.hostelfood_compatibility_board.ui.components.SearchBarComponent
import com.pratyush.hostelfood_compatibility_board.ui.components.SummaryHeaderCard
import com.pratyush.hostelfood_compatibility_board.viewmodel.BoardViewModel

/**
 * Main single-screen dashboard coordinator for the Hostel Food Compatibility Board.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    viewModel: BoardViewModel = remember { BoardViewModel() },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compatibility Board",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Header with Budget, Stats, and Action Buttons
                item {
                    SummaryHeaderCard(
                        state = state,
                        onLoadBuiltIn = viewModel::onLoadBuiltInAndCalculate,
                        onCalculate = viewModel::onCalculateClicked,
                        onReset = viewModel::onResetClicked,
                        onBudgetChanged = viewModel::onBudgetChanged
                    )
                }

                // 2. Strict Error Banner (shown on INVALID_INPUT or DUPLICATE_DISH_ID)
                state.errorMessage?.let { error ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Column {
                                    Text(
                                        text = "VALIDATION FAILED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Results and counts cleared. Please fix the error or tap Reset.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Focused Search Bar
                item {
                    SearchBarComponent(
                        query = state.searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged
                    )
                }

                // 4. Resident Group Section
                item {
                    ResidentSection(residents = state.residents)
                }

                // 5. Dish Table Section
                item {
                    DishSection(
                        dishes = state.dishes,
                        onEditDish = viewModel::onSelectDishForEdit
                    )
                }

                // 6. Results Section (Only rendered when calculated and no validation error)
                if (state.isCalculated && state.errorMessage == null) {
                    item {
                        ResultsSection(
                            totalCount = state.totalCompatibleCount,
                            compatibleDishes = state.displayedCompatibleDishes,
                            excludedDishes = state.excludedDishes
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Focused Row Edit Dialog
            state.selectedDishForEdit?.let { dish ->
                EditDishDialog(
                    dish = dish,
                    onDismiss = { viewModel.onSelectDishForEdit(null) },
                    onSave = viewModel::onUpdateDish
                )
            }
        }
    }
}
