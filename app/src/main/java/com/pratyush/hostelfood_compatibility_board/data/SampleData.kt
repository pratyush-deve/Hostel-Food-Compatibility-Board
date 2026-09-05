package com.pratyush.hostelfood_compatibility_board.data

import com.pratyush.hostelfood_compatibility_board.model.DietClass
import com.pratyush.hostelfood_compatibility_board.model.Dish
import com.pratyush.hostelfood_compatibility_board.model.Resident

/**
 * Built-in default dataset specified by the problem statement.
 */
object SampleData {
    const val DEFAULT_BUDGET = 150

    fun defaultResidents(): List<Resident> = listOf(
        Resident(
            name = "Asha",
            diet = DietClass.VEGAN,
            allergens = emptyList() // "none" parses as empty list
        ),
        Resident(
            name = "Dev",
            diet = DietClass.VEGETARIAN,
            allergens = listOf("PEANUT")
        ),
        Resident(
            name = "Mira",
            diet = DietClass.NO_RESTRICTION,
            allergens = listOf("MILK")
        )
    )

    fun defaultDishes(): List<Dish> = listOf(
        Dish(
            id = "D01",
            cafe = "Hostel Cafe",
            name = "Lentil Rice Bowl",
            diet = DietClass.VEGAN,
            ingredientTags = listOf("LENTIL", "RICE", "SPINACH"),
            price = 110
        ),
        Dish(
            id = "D02",
            cafe = "Library Cafe",
            name = "Tomato Pasta",
            diet = DietClass.VEGAN,
            ingredientTags = listOf("WHEAT", "TOMATO"),
            price = 150
        ),
        Dish(
            id = "D03",
            cafe = "Hostel Cafe",
            name = "Paneer Wrap",
            diet = DietClass.VEGETARIAN,
            ingredientTags = listOf("MILK", "WHEAT"),
            price = 140
        ),
        Dish(
            id = "D04",
            cafe = "East Cafe",
            name = "Peanut Noodles",
            diet = DietClass.VEGAN,
            ingredientTags = listOf("PEANUT", "WHEAT"),
            price = 130
        ),
        Dish(
            id = "D05",
            cafe = "Library Cafe",
            name = "Egg Sandwich",
            diet = DietClass.NON_VEGETARIAN,
            ingredientTags = listOf("EGG", "WHEAT"),
            price = 100
        )
    )
}
