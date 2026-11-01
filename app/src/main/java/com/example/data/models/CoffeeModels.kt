package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val tagline: String,
    val brewMethod: String,
    val roastType: String,
    val milkPreference: String,
    val strength: String,
    val flavorProfiles: String, // comma-separated (e.g., "Chocolaty, Caramel")
    val prepTime: String,
    val difficulty: String,
    val coffeeGrams: Double,
    val waterGrams: Double,
    val ratioText: String,
    val grindSize: String,
    val waterTemp: String,
    val ingredientsText: String,
    val instructionsText: String,
    val flavorDescription: String,
    val baristaTip: String,
    val userRating: Int = 0,
    val userNotes: String = "",
    val isFavorite: Boolean = false,
    val isUserCreated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: String,
    val category: String, // "Beans & Grounds", "Milk & Dairy", "Syrups & Flavors", "Equipment & Gear"
    val isPurchased: Boolean = false,
    val sourceRecipe: String = ""
)

data class BrewStep(
    val stepNumber: Int,
    val title: String,
    val durationSeconds: Int,
    val description: String,
    val isKeyPhase: Boolean = false
)

data class TroubleshootTip(
    val problem: String,
    val cause: String,
    val solution: String
)

data class BrewMethodGuide(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconType: String, // "espresso", "french_press", "pour_over", "aeropress", "cold_brew", "moka_pot"
    val difficulty: String,
    val idealGrind: String,
    val defaultRatio: String, // e.g., "1:16"
    val defaultCoffeeGrams: Double,
    val defaultWaterGrams: Double,
    val idealTemp: String,
    val totalTimeText: String,
    val totalDurationSeconds: Int,
    val summary: String,
    val steps: List<BrewStep>,
    val proTips: List<String>,
    val troubleshooting: List<TroubleshootTip>
)

data class BaristaRecommendationInput(
    val selectedFlavors: List<String>,
    val roastType: String,
    val milkPreference: String,
    val brewMethod: String,
    val strength: String,
    val temperature: String = "Hot",
    val availableIngredients: List<String> = emptyList()
)

data class CoffeeQA(
    val id: Int,
    val question: String,
    val answer: String,
    val topic: String
)
