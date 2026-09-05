package com.pratyush.hostelfood_compatibility_board.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pratyush.hostelfood_compatibility_board.viewmodel.BoardScreenState

/**
 * Header card containing budget controls, action buttons, and the prominent compatible count badge.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryHeaderCard(
    state: BoardScreenState,
    onLoadBuiltIn: () -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onBudgetChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var budgetInput by remember(state.budget) { mutableStateOf(state.budget.toString()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // App Title & Tagline
            Text(
                text = "Hostel Food Compatibility Board",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Group food decision-support system",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Budget Input and Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { newValue ->
                        budgetInput = newValue
                        val parsed = newValue.toIntOrNull()
                        if (parsed != null) {
                            onBudgetChanged(parsed)
                        } else if (newValue.isEmpty()) {
                            onBudgetChanged(0) // Will trigger INVALID_INPUT as per contract
                        }
                    },
                    label = { Text("Budget / Person") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(160.dp)
                )

                // Quick boundary test button for Acceptance Test 4 (₹130)
                OutlinedButton(
                    onClick = {
                        val newBudget = if (state.budget == 150) 130 else 150
                        budgetInput = newBudget.toString()
                        onBudgetChanged(newBudget)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (state.budget == 150) "Set ₹130" else "Set ₹150")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compatible Count Summary Badge
            if (state.isCalculated && state.errorMessage == null) {
                Surface(
                    color = Color(0xFF2E7D32), // Vibrant Dark Green
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "COMPATIBLE SUMMARY",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.totalCompatibleCount} DISHES FOUND",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else if (!state.isCalculated && state.errorMessage == null) {
                Surface(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ready to calculate compatibility",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row (Load Built-in, Calculate, Reset)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Acceptance Criterion 1: Single action built-in load & calculate
                Button(
                    onClick = onLoadBuiltIn,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Load Built-in")
                }

                FilledTonalButton(
                    onClick = onCalculate,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Calculate")
                }

                // Acceptance Criterion 6: Reset
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
