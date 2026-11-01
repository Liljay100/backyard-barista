package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.models.BaristaRecommendationInput
import com.example.data.models.BrewMethodGuide
import com.example.data.models.BrewStep
import com.example.data.models.CoffeeQA
import com.example.data.models.RecipeEntity
import com.example.data.models.ShoppingItemEntity
import com.example.data.repository.CoffeeRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BrewTimerState(
    val isRunning: Boolean = false,
    val currentStepIndex: Int = 0,
    val secondsRemaining: Int = 0,
    val totalStepSeconds: Int = 0,
    val isCompleted: Boolean = false
)

data class BrewVisualSpec(
    val glassware: String,
    val liquidAppearance: String,
    val foamAndTexture: String,
    val garnishAndPresentation: String
)

data class BrewVisualizationState(
    val isVisualizing: Boolean = false,
    val recipeId: Long? = null,
    val recipeName: String = "",
    val generatedBitmap: Bitmap? = null,
    val visualSpec: BrewVisualSpec? = null,
    val visualPrompt: String = "",
    val errorMessage: String? = null
)

class BaristaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = CoffeeRepository(database.recipeDao(), database.shoppingDao())

    val allRecipes: StateFlow<List<RecipeEntity>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalRecipes: StateFlow<List<RecipeEntity>> = repository.journalRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRecipes: StateFlow<List<RecipeEntity>> = repository.favoriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brewGuides: List<BrewMethodGuide> = repository.getBrewMethodGuides()
    val coffeeQAs: List<CoffeeQA> = repository.getCoffeeQAs()

    // Navigation & Modal Detail State
    private val _selectedRecipeForDetail = MutableStateFlow<RecipeEntity?>(null)
    val selectedRecipeForDetail = _selectedRecipeForDetail.asStateFlow()

    private val _activeBrewGuide = MutableStateFlow<BrewMethodGuide>(brewGuides.first())
    val activeBrewGuide = _activeBrewGuide.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    // Recipe Generator State
    private val _selectedFlavors = MutableStateFlow<Set<String>>(setOf("Caramel", "Chocolaty"))
    val selectedFlavors = _selectedFlavors.asStateFlow()

    private val _selectedRoast = MutableStateFlow("Medium")
    val selectedRoast = _selectedRoast.asStateFlow()

    private val _selectedMilk = MutableStateFlow("Oat Milk")
    val selectedMilk = _selectedMilk.asStateFlow()

    private val _selectedBrewMethod = MutableStateFlow("Pour-Over")
    val selectedBrewMethod = _selectedBrewMethod.asStateFlow()

    private val _selectedStrength = MutableStateFlow("Balanced")
    val selectedStrength = _selectedStrength.asStateFlow()

    private val _generatedRecipe = MutableStateFlow<RecipeEntity?>(null)
    val generatedRecipe = _generatedRecipe.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    // Barista Recommender / Pantry Assistant State
    private val _userPantry = MutableStateFlow<Set<String>>(
        setOf("Espresso Beans", "Oat Milk", "Brown Sugar", "Cinnamon")
    )
    val userPantry = _userPantry.asStateFlow()

    private val _currentMood = MutableStateFlow("Cozy Morning")
    val currentMood = _currentMood.asStateFlow()

    private val _preferredTemp = MutableStateFlow("Hot")
    val preferredTemp = _preferredTemp.asStateFlow()

    // Brew Timer State (Guide Screen)
    private val _timerState = MutableStateFlow(BrewTimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null

    // Recipe-Specific Active Brew Timer Dialog
    private val _activeTimerRecipe = MutableStateFlow<RecipeEntity?>(null)
    val activeTimerRecipe = _activeTimerRecipe.asStateFlow()

    private val _activeTimerStages = MutableStateFlow<List<BrewStep>>(emptyList())
    val activeTimerStages = _activeTimerStages.asStateFlow()

    private val _isRecipeTimerDialogVisible = MutableStateFlow(false)
    val isRecipeTimerDialogVisible = _isRecipeTimerDialogVisible.asStateFlow()

    private var recipeTimerJob: Job? = null

    // AI Brew Visualization State
    private val _visualizationState = MutableStateFlow(BrewVisualizationState())
    val visualizationState = _visualizationState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun showRecipeDetail(recipe: RecipeEntity?) {
        _selectedRecipeForDetail.value = recipe
    }

    fun selectBrewGuide(guide: BrewMethodGuide) {
        _activeBrewGuide.value = guide
        resetTimer()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Generator Controls
    fun toggleFlavor(flavor: String) {
        val current = _selectedFlavors.value.toMutableSet()
        if (current.contains(flavor)) {
            if (current.size > 1) current.remove(flavor)
        } else {
            current.add(flavor)
        }
        _selectedFlavors.value = current
    }

    fun setRoast(roast: String) { _selectedRoast.value = roast }
    fun setMilk(milk: String) { _selectedMilk.value = milk }
    fun setBrewMethod(method: String) { _selectedBrewMethod.value = method }
    fun setStrength(strength: String) { _selectedStrength.value = strength }

    fun generateRecipe() {
        viewModelScope.launch {
            _isGenerating.value = true
            delay(500) // realistic smooth calculation feedback
            val input = BaristaRecommendationInput(
                selectedFlavors = _selectedFlavors.value.toList(),
                roastType = _selectedRoast.value,
                milkPreference = _selectedMilk.value,
                brewMethod = _selectedBrewMethod.value,
                strength = _selectedStrength.value
            )
            val recipe = repository.generateCustomRecipe(input)
            _generatedRecipe.value = recipe
            _isGenerating.value = false
            showSnackbar("Crafted: ${recipe.name}")
        }
    }

    // Pantry & Recommender Controls
    fun togglePantryItem(item: String) {
        val current = _userPantry.value.toMutableSet()
        if (current.contains(item)) current.remove(item) else current.add(item)
        _userPantry.value = current
    }

    fun setMood(mood: String) { _currentMood.value = mood }
    fun setTemperature(temp: String) { _preferredTemp.value = temp }

    fun generateFromPantry() {
        viewModelScope.launch {
            _isGenerating.value = true
            delay(400)
            val pantry = _userPantry.value
            val chosenMethod = when {
                pantry.any { it.contains("Espresso") } -> "Espresso"
                pantry.any { it.contains("French") } -> "French Press"
                pantry.any { it.contains("Pour") || it.contains("V60") } -> "Pour-Over"
                pantry.any { it.contains("Aero") } -> "AeroPress"
                pantry.any { it.contains("Cold") } -> "Cold Brew"
                else -> "Pour-Over"
            }
            val chosenMilk = when {
                pantry.any { it.contains("Oat") } -> "Oat Milk"
                pantry.any { it.contains("Almond") } -> "Almond Milk"
                pantry.any { it.contains("Whole") } -> "Whole Milk"
                pantry.any { it.contains("Condensed") } -> "Sweet Condensed Milk"
                else -> "None (Black)"
            }
            val chosenFlavors = mutableListOf<String>()
            if (pantry.any { it.contains("Sugar") || it.contains("Caramel") }) chosenFlavors.add("Caramel")
            if (pantry.any { it.contains("Cocoa") || it.contains("Chocolate") }) chosenFlavors.add("Chocolaty")
            if (pantry.any { it.contains("Cinnamon") || it.contains("Nut") }) chosenFlavors.add("Nutty")
            if (chosenFlavors.isEmpty()) chosenFlavors.addAll(listOf("Caramel", "Bold"))

            val input = BaristaRecommendationInput(
                selectedFlavors = chosenFlavors,
                roastType = if (_currentMood.value.contains("Cozy")) "Medium-Dark" else "Medium",
                milkPreference = chosenMilk,
                brewMethod = chosenMethod,
                strength = if (_currentMood.value.contains("Boost")) "Intense" else "Balanced",
                temperature = _preferredTemp.value,
                availableIngredients = pantry.toList()
            )
            val recipe = repository.generateCustomRecipe(input)
            _selectedRecipeForDetail.value = recipe
            _isGenerating.value = false
        }
    }

    // Recipe & Journal Actions
    fun saveRecipeToJournal(recipe: RecipeEntity, rating: Int = 0, notes: String = "") {
        viewModelScope.launch {
            val toSave = recipe.copy(
                isFavorite = true,
                isUserCreated = true,
                userRating = if (rating > 0) rating else recipe.userRating,
                userNotes = if (notes.isNotBlank()) notes else recipe.userNotes
            )
            val id = repository.saveRecipe(toSave)
            _generatedRecipe.value = toSave.copy(id = id)
            showSnackbar("Saved \"${recipe.name}\" to your Recipe Journal!")
        }
    }

    fun logCustomJournalEntry(
        name: String,
        method: String,
        roast: String,
        milk: String,
        rating: Int,
        notes: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val input = BaristaRecommendationInput(
                selectedFlavors = listOf("Caramel", "Balanced"),
                roastType = roast,
                milkPreference = milk,
                brewMethod = method,
                strength = "Balanced"
            )
            val template = repository.generateCustomRecipe(input)
            val customEntry = template.copy(
                name = name.trim(),
                tagline = "Hand-crafted custom brew logged in Backyard Barista.",
                userRating = rating,
                userNotes = notes.trim(),
                isFavorite = true,
                isUserCreated = true,
                createdAt = System.currentTimeMillis()
            )
            repository.saveRecipe(customEntry)
            showSnackbar("Logged \"${name.trim()}\" in your Recipe Journal!")
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            val newFav = !recipe.isFavorite
            repository.toggleFavorite(recipe.id, newFav)
            val msg = if (newFav) "Added to favorites" else "Removed from favorites"
            showSnackbar(msg)
            if (_selectedRecipeForDetail.value?.id == recipe.id) {
                _selectedRecipeForDetail.value = recipe.copy(isFavorite = newFav)
            }
        }
    }

    fun updateJournalNotesAndRating(recipeId: Long, rating: Int, notes: String) {
        viewModelScope.launch {
            repository.updateJournalEntry(recipeId, rating, notes)
            showSnackbar("Journal updated (${rating}★)!")
            if (_selectedRecipeForDetail.value?.id == recipeId) {
                _selectedRecipeForDetail.value = _selectedRecipeForDetail.value?.copy(
                    userRating = rating,
                    userNotes = notes
                )
            }
        }
    }

    fun deleteRecipeFromJournal(recipeId: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
            if (_selectedRecipeForDetail.value?.id == recipeId) {
                _selectedRecipeForDetail.value = null
            }
            showSnackbar("Recipe removed from journal")
        }
    }

    // Shopping List Actions
    fun addRecipeIngredientsToShoppingList(recipe: RecipeEntity) {
        viewModelScope.launch {
            val items = mutableListOf<ShoppingItemEntity>()

            // 1. Specialty Beans
            items.add(
                ShoppingItemEntity(
                    name = "${recipe.roastType} Roast Specialty Coffee Beans",
                    quantity = "${recipe.coffeeGrams.toInt()}g",
                    category = "Beans & Grounds",
                    sourceRecipe = recipe.name
                )
            )

            // 2. Filtered Water
            items.add(
                ShoppingItemEntity(
                    name = "Soft Filtered Brewing Water (75-125 ppm TDS)",
                    quantity = "${recipe.waterGrams.toInt()}g",
                    category = "Beans & Grounds",
                    sourceRecipe = recipe.name
                )
            )

            // 3. Milk & Dairy
            if (recipe.milkPreference != "None (Black)") {
                items.add(
                    ShoppingItemEntity(
                        name = recipe.milkPreference,
                        quantity = if (recipe.brewMethod == "Espresso") "150ml" else "1 Carton",
                        category = "Milk & Dairy",
                        sourceRecipe = recipe.name
                    )
                )
            }

            // 4. Flavor Accents & Syrups
            if (recipe.flavorProfiles.contains("Caramel")) {
                items.add(
                    ShoppingItemEntity(
                        name = "Artisanal Caramel Syrup / Raw Panela",
                        quantity = "1 Bottle",
                        category = "Syrups & Flavors",
                        sourceRecipe = recipe.name
                    )
                )
            }
            if (recipe.flavorProfiles.contains("Chocolaty")) {
                items.add(
                    ShoppingItemEntity(
                        name = "Single-Origin Dutch Cocoa / 70% Dark Chocolate",
                        quantity = "1 Tin / Bar",
                        category = "Syrups & Flavors",
                        sourceRecipe = recipe.name
                    )
                )
            }
            if (recipe.flavorProfiles.contains("Nutty")) {
                items.add(
                    ShoppingItemEntity(
                        name = "Natural Toasted Hazelnut or Almond Essence",
                        quantity = "1 Bottle",
                        category = "Syrups & Flavors",
                        sourceRecipe = recipe.name
                    )
                )
            }
            if (recipe.flavorProfiles.contains("Sweet")) {
                items.add(
                    ShoppingItemEntity(
                        name = "Wild Raw Blossom Honey / Demerara Sugar",
                        quantity = "1 Jar",
                        category = "Syrups & Flavors",
                        sourceRecipe = recipe.name
                    )
                )
            }

            // 5. Required Brewing Equipment & Filters
            val equipmentItem = when (recipe.brewMethod) {
                "Pour-Over" -> ShoppingItemEntity(
                    name = "V60 / Chemex Bleached Paper Filters (Size 02)",
                    quantity = "1 Pack (100ct)",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
                "Espresso" -> ShoppingItemEntity(
                    name = "Precision Espresso Tamper & WDT Needle Tool",
                    quantity = "1 Set",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
                "French Press" -> ShoppingItemEntity(
                    name = "French Press Heat-Proof Glass Carafe",
                    quantity = "1 Unit",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
                "AeroPress" -> ShoppingItemEntity(
                    name = "AeroPress Micro-Paper Filters",
                    quantity = "1 Pack (350ct)",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
                "Cold Brew" -> ShoppingItemEntity(
                    name = "Cold Brew Mesh Filtration Pouch or Glass Pitcher",
                    quantity = "1 Unit",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
                "Moka Pot" -> ShoppingItemEntity(
                    name = "Moka Pot Replacement Funnel & Silicone Gasket",
                    quantity = "1 Set",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
                else -> ShoppingItemEntity(
                    name = "Specialty Coffee Paper Filters",
                    quantity = "1 Pack",
                    category = "Equipment & Gear",
                    sourceRecipe = recipe.name
                )
            }
            items.add(equipmentItem)

            repository.addShoppingItems(items)
            showSnackbar("Extracted ${items.size} ingredients & gear into your Shopping List!")
        }
    }

    fun addCustomShoppingItem(name: String, quantity: String, category: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addShoppingItem(
                ShoppingItemEntity(
                    name = name.trim(),
                    quantity = if (quantity.isBlank()) "1 unit" else quantity.trim(),
                    category = category
                )
            )
            showSnackbar("Added \"$name\" to shopping list")
        }
    }

    fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.toggleShoppingItemPurchased(item.id, !item.isPurchased)
        }
    }

    fun deleteShoppingItem(id: Long) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun clearCompletedShoppingItems() {
        viewModelScope.launch {
            repository.clearCompletedShoppingItems()
            showSnackbar("Cleared completed items")
        }
    }

    // Interactive Brew Timer Logic
    fun startTimer() {
        val guide = _activeBrewGuide.value
        if (guide.steps.isEmpty()) return

        val state = _timerState.value
        val step = guide.steps[state.currentStepIndex]
        val seconds = if (state.secondsRemaining > 0) state.secondsRemaining else step.durationSeconds

        _timerState.value = state.copy(
            isRunning = true,
            secondsRemaining = seconds,
            totalStepSeconds = step.durationSeconds,
            isCompleted = false
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning && _timerState.value.secondsRemaining > 0) {
                delay(1000)
                val newSec = _timerState.value.secondsRemaining - 1
                _timerState.value = _timerState.value.copy(secondsRemaining = newSec)
                if (newSec <= 0) {
                    advanceTimerStep()
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val guide = _activeBrewGuide.value
        val initialSec = guide.steps.firstOrNull()?.durationSeconds ?: 0
        _timerState.value = BrewTimerState(
            isRunning = false,
            currentStepIndex = 0,
            secondsRemaining = initialSec,
            totalStepSeconds = initialSec,
            isCompleted = false
        )
    }

    fun advanceTimerStep() {
        val guide = _activeBrewGuide.value
        val nextIdx = _timerState.value.currentStepIndex + 1
        if (nextIdx < guide.steps.size) {
            val nextStep = guide.steps[nextIdx]
            _timerState.value = _timerState.value.copy(
                currentStepIndex = nextIdx,
                secondsRemaining = nextStep.durationSeconds,
                totalStepSeconds = nextStep.durationSeconds,
                isRunning = true
            )
        } else {
            // Completed!
            timerJob?.cancel()
            _timerState.value = _timerState.value.copy(
                isRunning = false,
                secondsRemaining = 0,
                isCompleted = true
            )
            showSnackbar("Brewing completed! Enjoy your perfect cup.")
        }
    }

    fun jumpToTimerStep(index: Int) {
        val guide = _activeBrewGuide.value
        if (index in guide.steps.indices) {
            timerJob?.cancel()
            val step = guide.steps[index]
            _timerState.value = _timerState.value.copy(
                isRunning = false,
                currentStepIndex = index,
                secondsRemaining = step.durationSeconds,
                totalStepSeconds = step.durationSeconds,
                isCompleted = false
            )
        }
    }

    // Dedicated Recipe-Specific Brew Timer Logic
    fun startTimerForRecipe(recipe: RecipeEntity) {
        _activeTimerRecipe.value = recipe
        val stages = generateBrewStagesForRecipe(recipe)
        _activeTimerStages.value = stages

        val initialSeconds = stages.firstOrNull()?.durationSeconds ?: 30
        _timerState.value = BrewTimerState(
            isRunning = true,
            currentStepIndex = 0,
            secondsRemaining = initialSeconds,
            totalStepSeconds = initialSeconds,
            isCompleted = false
        )
        _isRecipeTimerDialogVisible.value = true

        runRecipeTimerCountdown()
    }

    fun pauseRecipeTimer() {
        recipeTimerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resumeRecipeTimer() {
        val state = _timerState.value
        if (state.secondsRemaining <= 0) {
            val stages = _activeTimerStages.value
            val step = stages.getOrNull(state.currentStepIndex)
            val initialSeconds = step?.durationSeconds ?: 30
            _timerState.value = state.copy(
                isRunning = true,
                secondsRemaining = initialSeconds,
                totalStepSeconds = initialSeconds,
                isCompleted = false
            )
        } else {
            _timerState.value = state.copy(isRunning = true, isCompleted = false)
        }
        runRecipeTimerCountdown()
    }

    fun resetRecipeTimer() {
        recipeTimerJob?.cancel()
        val stages = _activeTimerStages.value
        val initialSeconds = stages.firstOrNull()?.durationSeconds ?: 30
        _timerState.value = BrewTimerState(
            isRunning = false,
            currentStepIndex = 0,
            secondsRemaining = initialSeconds,
            totalStepSeconds = initialSeconds,
            isCompleted = false
        )
    }

    fun advanceRecipeTimerStage() {
        val stages = _activeTimerStages.value
        val nextIdx = _timerState.value.currentStepIndex + 1
        if (nextIdx < stages.size) {
            val nextStep = stages[nextIdx]
            _timerState.value = _timerState.value.copy(
                currentStepIndex = nextIdx,
                secondsRemaining = nextStep.durationSeconds,
                totalStepSeconds = nextStep.durationSeconds,
                isRunning = true,
                isCompleted = false
            )
            runRecipeTimerCountdown()
        } else {
            recipeTimerJob?.cancel()
            _timerState.value = _timerState.value.copy(
                isRunning = false,
                secondsRemaining = 0,
                isCompleted = true
            )
            showSnackbar("Brewing completed! Enjoy your ${activeTimerRecipe.value?.name ?: "coffee"}.")
        }
    }

    fun dismissRecipeTimerDialog() {
        recipeTimerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
        _isRecipeTimerDialogVisible.value = false
    }

    private fun runRecipeTimerCountdown() {
        recipeTimerJob?.cancel()
        recipeTimerJob = viewModelScope.launch {
            while (_timerState.value.isRunning && _timerState.value.secondsRemaining > 0) {
                delay(1000)
                val newSec = _timerState.value.secondsRemaining - 1
                _timerState.value = _timerState.value.copy(secondsRemaining = newSec)
                if (newSec <= 0) {
                    advanceRecipeTimerStage()
                }
            }
        }
    }

    private fun generateBrewStagesForRecipe(recipe: RecipeEntity): List<BrewStep> {
        val c = recipe.coffeeGrams.toInt()
        val w = recipe.waterGrams.toInt()
        return when (recipe.brewMethod) {
            "Pour-Over" -> listOf(
                BrewStep(1, "Stage 1: The Bloom (0:00–0:45)", 45, "Pour ${(c * 2.5).toInt()}g water at ${recipe.waterTemp} in a slow spiral. Swirl cone twice; wait 45s for degassing.", isKeyPhase = true),
                BrewStep(2, "Stage 2: Main Extraction Pulse (0:45–1:30)", 45, "Pour in steady concentric circles until scale reaches ${(w * 0.6).toInt()}g.", isKeyPhase = true),
                BrewStep(3, "Stage 3: Final Pour & Drawdown (1:30–3:15)", 105, "Pour remaining water up to ${w}g. Swirl server gently; allow complete flat bed drawdown.", isKeyPhase = true)
            )
            "French Press" -> listOf(
                BrewStep(1, "Stage 1: Saturation Pour (0:00–0:30)", 30, "Pour all ${w}g water at ${recipe.waterTemp} briskly, wetting all coarse grounds.", isKeyPhase = true),
                BrewStep(2, "Stage 2: Undisturbed Steep (0:30–4:00)", 210, "Rest lid on carafe without pressing. Allow full immersion extraction for 3.5 minutes.", isKeyPhase = true),
                BrewStep(3, "Stage 3: Crust Break & Slow Plunge (4:00–4:30)", 30, "Break crust with spoon, skim floating fines, then gently lower plunger to surface. Decant immediately.", isKeyPhase = true)
            )
            "Espresso" -> listOf(
                BrewStep(1, "Stage 1: Puck Preparation (10s)", 10, "Dose ${c}g evenly into dry basket. Declump grounds with WDT needle tool.", isKeyPhase = false),
                BrewStep(2, "Stage 2: Level Tamp & Group Flush (5s)", 5, "Tamp firmly level with 25–30 lbs pressure. Flush grouphead 2s; lock in portafilter.", isKeyPhase = false),
                BrewStep(3, "Stage 3: Extraction Pull (28s)", 28, "Engage pump immediately. Extract ${w}g rich, tiger-striped crema in 25–30s.", isKeyPhase = true)
            )
            "AeroPress" -> listOf(
                BrewStep(1, "Stage 1: Inverted Bloom & Agitate (0:00–0:30)", 30, "Pour ${(c * 3).toInt()}g water into inverted AeroPress. Stir 5 times to saturate grounds.", isKeyPhase = true),
                BrewStep(2, "Stage 2: Chamber Fill & Steep (0:30–1:30)", 60, "Fill with remaining water to ${w}g. Screw on rinsed filter cap; steep undisturbed.", isKeyPhase = true),
                BrewStep(3, "Stage 3: Invert & Gentle Press (1:30–2:00)", 30, "Smoothly flip onto cup; press plunger with gentle steady weight for 30s until first hiss.", isKeyPhase = true)
            )
            "Moka Pot" -> listOf(
                BrewStep(1, "Stage 1: Boiler & Basket Setup (60s)", 60, "Fill base with boiling water to valve line; fill funnel with ${c}g coffee level.", isKeyPhase = false),
                BrewStep(2, "Stage 2: Low Flame Extraction (180s)", 180, "Place on low flame with lid cracked. Watch for rich amber stream flowing up column.", isKeyPhase = true),
                BrewStep(3, "Stage 3: Halt Extraction (15s)", 15, "Remove pot from heat immediately when stream turns blonde. Run base under cool tap.", isKeyPhase = true)
            )
            "Cold Brew" -> listOf(
                BrewStep(1, "Stage 1: Cold Saturation (60s)", 60, "Pour ${w}g cool filtered water over ${c}g coarse coffee. Stir to submerge all grounds.", isKeyPhase = true),
                BrewStep(2, "Stage 2: Refrigerated Steep (120s Demo / 16 hrs)", 120, "Cover vessel and chill in refrigerator for 16 to 18 hours.", isKeyPhase = true),
                BrewStep(3, "Stage 3: Dual Paper Filtration (60s)", 60, "Strain concentrate through fine mesh, followed by paper filter for radiant clarity.", isKeyPhase = true)
            )
            else -> listOf(
                BrewStep(1, "Stage 1: Bloom (45s)", 45, "Pour initial water to saturate grounds.", isKeyPhase = true),
                BrewStep(2, "Stage 2: Extraction (90s)", 90, "Pour remaining water and brew to completion.", isKeyPhase = true)
            )
        }
    }

    // AI Brew Visualization Logic
    fun visualizeRecipe(recipe: RecipeEntity) {
        val spec = generateVisualSpecForRecipe(recipe)
        val prompt = generateVisualPrompt(recipe, spec)

        _visualizationState.value = BrewVisualizationState(
            isVisualizing = true,
            recipeId = recipe.id,
            recipeName = recipe.name,
            visualSpec = spec,
            visualPrompt = prompt,
            errorMessage = null
        )

        viewModelScope.launch {
            val hasFirebase = try {
                FirebaseApp.getApps(getApplication()).isNotEmpty()
            } catch (e: Throwable) {
                false
            }

            if (!hasFirebase) {
                // Transparent, helpful message complying with "show a helpful message instead of pretending an image was generated"
                _visualizationState.value = _visualizationState.value.copy(
                    isVisualizing = false,
                    errorMessage = "AI Image Generation is currently unavailable in this environment because Firebase AI Logic has not been provisioned. To enable real-time coffee rendering, configure the Gemini API or provision Firebase AI Logic in AI Studio. (No mock image was substituted)."
                )
                showSnackbar("Image generation unavailable: AI Logic unprovisioned")
                return@launch
            }

            try {
                val config = generationConfig {
                    responseModalities = listOf(ResponseModality.IMAGE)
                }
                val app = FirebaseApp.getInstance()
                val ai = FirebaseAI.getInstance(app)
                val model = ai.generativeModel(
                    modelName = "gemini-nano-banana-2.1",
                    generationConfig = config
                )
                val response = kotlinx.coroutines.withTimeout(45_000L) {
                    model.generateContent(prompt)
                }
                val imagePart = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()
                var bitmap: Bitmap? = imagePart?.image
                if (bitmap == null) {
                    val inlinePart = response.inlineDataParts.firstOrNull()
                    if (inlinePart != null) {
                        val bytes = inlinePart.inlineData
                        bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }

                if (bitmap != null) {
                    _visualizationState.value = _visualizationState.value.copy(
                        isVisualizing = false,
                        generatedBitmap = bitmap,
                        errorMessage = null
                    )
                    showSnackbar("Visual render created for ${recipe.name}! ☕")
                } else {
                    _visualizationState.value = _visualizationState.value.copy(
                        isVisualizing = false,
                        errorMessage = "The AI model did not return image data for this recipe. Ensure your Firebase project has Gemini image generation quotas enabled."
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _visualizationState.value = _visualizationState.value.copy(
                    isVisualizing = false,
                    errorMessage = "Image generation failed: ${e.localizedMessage ?: "Connection error"}. Ensure Firebase AI Logic is provisioned with Blaze billing."
                )
                showSnackbar("Image generation failed")
            }
        }
    }

    fun dismissVisualizationMessage() {
        _visualizationState.value = _visualizationState.value.copy(errorMessage = null)
    }

    fun clearVisualization() {
        _visualizationState.value = BrewVisualizationState()
    }

    // Formatted Recipe Summary with Coffee Emojis for Clipboard Export
    fun formatRecipeForClipboard(recipe: RecipeEntity): String {
        val ingredientsList = recipe.ingredientsText.lines().filter { it.isNotBlank() }
            .joinToString("\n") { "  • $it" }
        val instructionsList = recipe.instructionsText.lines().filter { it.isNotBlank() }
            .mapIndexed { index, step -> "  ${index + 1}. $step" }
            .joinToString("\n")

        return """
☕ The Backyard Barista – Specialty Coffee Recipe ☕
══════════════════════════════════════════════
✨ ${recipe.name} ✨
"${recipe.tagline}"

⏱️ Preparation Time: ${recipe.prepTime}
🎚️ Difficulty Level: ${recipe.difficulty}
🔥 Roast Profile: ${recipe.roastType} Roast
⚖️ Golden Ratio: ${recipe.ratioText} (${recipe.coffeeGrams.toInt()}g coffee / ${recipe.waterGrams.toInt()}g water)
⚙️ Brewing Method: ${recipe.brewMethod}
🌡️ Ideal Water Temperature: ${recipe.waterTemp}
🪨 Recommended Grind Size: ${recipe.grindSize}
🥛 Milk Preference: ${recipe.milkPreference}
🎨 Flavor Notes: ${recipe.flavorProfiles}

📋 Ingredients & Measurements:
$ingredientsList

📖 Step-by-Step Brewing Instructions:
$instructionsList

💡 Barista Master Recommendation:
${recipe.baristaTip}

══════════════════════════════════════════════
Crafted with The Backyard Barista – Your Personal Specialty Coffee Companion ☕
""".trimIndent()
    }

    fun generateVisualSpecForRecipe(recipe: RecipeEntity): BrewVisualSpec {
        val glassware = when (recipe.brewMethod) {
            "Espresso" -> "White ceramic demitasse cup with matching saucer and small polished spoon"
            "Pour-Over" -> "Clear heat-resistant borosilicate glass carafe with pour spout and ceramic tasting tumbler"
            "French Press" -> "Polished stainless steel and glass French press beaker, poured into a stoneware mug"
            "AeroPress" -> "Modern double-walled clear glass tumbler revealing transparent amber clarity"
            "Cold Brew" -> "Tall frosted crystal highball glass filled with crystal-clear square ice cubes"
            "Moka Pot" -> "Traditional faceted Italian aluminum moka pot paired with a small rustic porcelain cup"
            else -> "Artisanal handcrafted stoneware ceramic coffee mug"
        }

        val liquid = when {
            recipe.milkPreference == "None (Black)" && recipe.roastType == "Light" ->
                "Brilliant translucent amber and copper clarity with warm honey refractions along the rim"
            recipe.milkPreference == "None (Black)" ->
                "Deep mahogany and dark roasted hazelnut liquid body, clear meniscus with subtle golden sheen"
            recipe.milkPreference.contains("Condensed") ->
                "Dual-layered specialty drink with rich golden condensed milk base transitioning into a dark espresso swirl"
            else ->
                "Velvety warm café au lait hue, rich creamy caramel gradient merging into specialty roasted espresso"
        }

        val foam = when {
            recipe.brewMethod == "Espresso" ->
                "Thick hazelnut-colored crema with dark tiger stripes and micro-bubbles"
            recipe.milkPreference != "None (Black)" ->
                "Glossy, velvety micro-foam with a crisp rosetta or tulip latte art pour, zero large bubbles"
            recipe.brewMethod == "Pour-Over" || recipe.brewMethod == "Cold Brew" ->
                "Silky still surface with gentle light reflections and delicate aromatic steam"
            else ->
                "Gentle frothy crema ring around the edge of the cup"
        }

        val garnish = buildString {
            if (recipe.flavorProfiles.contains("Chocolaty")) append("Delicate dusting of raw Dutch cacao powder across the micro-foam. ")
            if (recipe.flavorProfiles.contains("Nutty")) append("Lightly crushed toasted hazelnuts resting on the wooden saucer. ")
            if (recipe.flavorProfiles.contains("Caramel")) append("Fine golden caramel drizzle pattern. ")
            if (recipe.flavorProfiles.contains("Sweet")) append("Gently drizzled organic blossom honey. ")
            append("Resting on an artisanal rustic dark walnut barista counter with scattered roasted coffee beans in the background.")
        }

        return BrewVisualSpec(glassware, liquid, foam, garnish)
    }

    fun generateVisualPrompt(recipe: RecipeEntity, spec: BrewVisualSpec): String {
        return """
A hyper-realistic, award-winning specialty coffee photograph of "${recipe.name}".
Presentation & Glassware: ${spec.glassware}.
Drink Liquid & Color: ${spec.liquidAppearance}.
Texture & Crema/Micro-foam: ${spec.foamAndTexture}.
Garnish & Atmosphere: ${spec.garnishAndPresentation}.
Lighting: Soft directional morning window sunlight, cozy boutique cafe ambiance, warm earthy aesthetic, 8k resolution, highly detailed commercial beverage photography, shallow depth of field.
""".trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        recipeTimerJob?.cancel()
    }
}
