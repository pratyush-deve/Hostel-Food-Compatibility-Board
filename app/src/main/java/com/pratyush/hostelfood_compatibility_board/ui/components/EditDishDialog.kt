package com.pratyush.hostelfood_compatibility_board.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pratyush.hostelfood_compatibility_board.model.Dish

/**
 * Focused row edit dialog to modify a dish's price, cafe, or ingredients.
 * Used to test boundary conditions (e.g. Setting D01 price to 0 for INVALID_INPUT).
 */
@Composable
fun EditDishDialog(
    dish: Dish,
    onDismiss: () -> Unit,
    onSave: (Dish) -> Unit
) {
    var name by remember { mutableStateOf(dish.name) }
    var cafe by remember { mutableStateOf(dish.cafe) }
    var priceText by remember { mutableStateOf(dish.price.toString()) }
    var ingredientsText by remember { mutableStateOf(dish.ingredientTags.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Dish ${dish.id}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cafe,
                    onValueChange = { cafe = it },
                    label = { Text("Cafe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (₹)") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick boundary helper buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { priceText = "0" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set ₹0 (Test Error)")
                    }
                    OutlinedButton(
                        onClick = { priceText = "150" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set ₹150")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    label = { Text("Ingredients (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceText.toIntOrNull() ?: 0
                    val parsedIngredients = ingredientsText
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val updatedDish = dish.copy(
                        name = name,
                        cafe = cafe,
                        price = parsedPrice,
                        ingredientTags = parsedIngredients
                    )
                    onSave(updatedDish)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
