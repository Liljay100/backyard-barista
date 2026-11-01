package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ActiveBrewTimerDialog
import com.example.ui.screens.BaristaRecommenderScreen
import com.example.ui.screens.BrewingGuideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecipeDetailSheet
import com.example.ui.screens.RecipeGeneratorScreen
import com.example.ui.screens.RecipeJournalScreen
import com.example.ui.screens.ShoppingListScreen
import com.example.ui.theme.CaramelAmber
import com.example.ui.theme.CoffeeDarkRoast
import com.example.ui.theme.CoffeeMocha
import com.example.ui.theme.HoneyBronze
import com.example.ui.theme.LightCaramel
import com.example.ui.viewmodel.BaristaViewModel

enum class BaristaScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    GENERATOR("Create", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_create"),
    GUIDES("Brew Guide", Icons.Filled.Coffee, Icons.Outlined.Coffee, "nav_guides"),
    RECOMMENDER("Ask Barista", Icons.Filled.LocalCafe, Icons.Outlined.LocalCafe, "nav_recommender"),
    JOURNAL("Journal", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_journal"),
    SHOPPING("Shopping", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_shopping")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaristaMainApp(
    viewModel: BaristaViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(BaristaScreen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Collect Reactive State
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val journalRecipes by viewModel.journalRecipes.collectAsStateWithLifecycle()
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val selectedRecipeForDetail by viewModel.selectedRecipeForDetail.collectAsStateWithLifecycle()
    val activeBrewGuide by viewModel.activeBrewGuide.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val activeTimerRecipe by viewModel.activeTimerRecipe.collectAsStateWithLifecycle()
    val activeTimerStages by viewModel.activeTimerStages.collectAsStateWithLifecycle()
    val isRecipeTimerDialogVisible by viewModel.isRecipeTimerDialogVisible.collectAsStateWithLifecycle()
    val visualizationState by viewModel.visualizationState.collectAsStateWithLifecycle()

    val selectedFlavors by viewModel.selectedFlavors.collectAsStateWithLifecycle()
    val selectedRoast by viewModel.selectedRoast.collectAsStateWithLifecycle()
    val selectedMilk by viewModel.selectedMilk.collectAsStateWithLifecycle()
    val selectedBrewMethod by viewModel.selectedBrewMethod.collectAsStateWithLifecycle()
    val selectedStrength by viewModel.selectedStrength.collectAsStateWithLifecycle()
    val generatedRecipe by viewModel.generatedRecipe.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    val userPantry by viewModel.userPantry.collectAsStateWithLifecycle()
    val currentMood by viewModel.currentMood.collectAsStateWithLifecycle()
    val preferredTemp by viewModel.preferredTemp.collectAsStateWithLifecycle()

    // Handle Snackbars
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Android Hardware / Gesture Back Navigation
    if (currentScreen != BaristaScreen.HOME) {
        BackHandler {
            currentScreen = BaristaScreen.HOME
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 720.dp

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("app_bar_title")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "The Backyard Barista",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        IconButton(
                            onClick = { currentScreen = BaristaScreen.RECOMMENDER },
                            modifier = Modifier.testTag("top_bar_ask_barista_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalCafe,
                                contentDescription = "Ask Barista",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("bottom_nav_bar"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        BaristaScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    if (screen == BaristaScreen.SHOPPING && shoppingItems.any { !it.isPurchased }) {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.secondary,
                                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                                ) {
                                                    Text("${shoppingItems.count { !it.isPurchased }}")
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(screen.testTag)
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Wide Screen Navigation Rail for Foldables & Tablets / Desktops
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("wide_nav_rail")
                    ) {
                        BaristaScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("${screen.testTag}_rail")
                            )
                        }
                    }
                }

                // Main Content View
                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        BaristaScreen.HOME -> HomeScreen(
                            recipes = allRecipes,
                            onRecipeClick = { viewModel.showRecipeDetail(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onNavigateToGenerator = { currentScreen = BaristaScreen.GENERATOR },
                            onNavigateToGuides = { currentScreen = BaristaScreen.GUIDES },
                            onNavigateToRecommender = { currentScreen = BaristaScreen.RECOMMENDER }
                        )

                        BaristaScreen.GENERATOR -> RecipeGeneratorScreen(
                            selectedFlavors = selectedFlavors,
                            selectedRoast = selectedRoast,
                            selectedMilk = selectedMilk,
                            selectedBrewMethod = selectedBrewMethod,
                            selectedStrength = selectedStrength,
                            isGenerating = isGenerating,
                            generatedRecipe = generatedRecipe,
                            visualizationState = visualizationState,
                            onToggleFlavor = { viewModel.toggleFlavor(it) },
                            onSelectRoast = { viewModel.setRoast(it) },
                            onSelectMilk = { viewModel.setMilk(it) },
                            onSelectBrewMethod = { viewModel.setBrewMethod(it) },
                            onSelectStrength = { viewModel.setStrength(it) },
                            onGenerateClick = { viewModel.generateRecipe() },
                            onRecipeClick = { viewModel.showRecipeDetail(it) },
                            onSaveToJournal = { recipe, rating, notes ->
                                viewModel.saveRecipeToJournal(recipe, rating, notes)
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onAddToShoppingList = { viewModel.addRecipeIngredientsToShoppingList(it) },
                            onStartBrewTimer = { viewModel.startTimerForRecipe(it) },
                            onVisualizeBrew = { viewModel.visualizeRecipe(it) },
                            onDismissVisualizationNotice = { viewModel.dismissVisualizationMessage() },
                            onCopyRecipe = { recipe ->
                                val text = viewModel.formatRecipeForClipboard(recipe)
                                clipboardManager.setText(AnnotatedString(text))
                                viewModel.showSnackbar("Recipe & instructions copied to clipboard! ☕")
                            },
                            onStartBrewGuide = { method ->
                                val matchingGuide = viewModel.brewGuides.firstOrNull { it.title.contains(method, ignoreCase = true) }
                                    ?: viewModel.brewGuides.first()
                                viewModel.selectBrewGuide(matchingGuide)
                                currentScreen = BaristaScreen.GUIDES
                            }
                        )

                        BaristaScreen.GUIDES -> BrewingGuideScreen(
                            guides = viewModel.brewGuides,
                            activeGuide = activeBrewGuide,
                            timerState = timerState,
                            onSelectGuide = { viewModel.selectBrewGuide(it) },
                            onStartTimer = { viewModel.startTimer() },
                            onPauseTimer = { viewModel.pauseTimer() },
                            onResetTimer = { viewModel.resetTimer() },
                            onAdvanceTimer = { viewModel.advanceTimerStep() },
                            onJumpToStep = { viewModel.jumpToTimerStep(it) }
                        )

                        BaristaScreen.RECOMMENDER -> BaristaRecommenderScreen(
                            userPantry = userPantry,
                            currentMood = currentMood,
                            preferredTemp = preferredTemp,
                            isGenerating = isGenerating,
                            coffeeQAs = viewModel.coffeeQAs,
                            onTogglePantryItem = { viewModel.togglePantryItem(it) },
                            onSetMood = { viewModel.setMood(it) },
                            onSetTemperature = { viewModel.setTemperature(it) },
                            onRecommendFromPantry = { viewModel.generateFromPantry() }
                        )

                        BaristaScreen.JOURNAL -> RecipeJournalScreen(
                            journalRecipes = journalRecipes,
                            favoriteRecipes = favoriteRecipes,
                            onRecipeClick = { viewModel.showRecipeDetail(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onUpdateRatingAndNotes = { id, rating, notes ->
                                viewModel.updateJournalNotesAndRating(id, rating, notes)
                            },
                            onDeleteRecipe = { id ->
                                viewModel.deleteRecipeFromJournal(id)
                            },
                            onLogCustomBrew = { name, method, roast, milk, rating, notes ->
                                viewModel.logCustomJournalEntry(name, method, roast, milk, rating, notes)
                            },
                            onStartBrewGuide = { method ->
                                val matchingGuide = viewModel.brewGuides.firstOrNull { it.title.contains(method, ignoreCase = true) }
                                    ?: viewModel.brewGuides.first()
                                viewModel.selectBrewGuide(matchingGuide)
                                currentScreen = BaristaScreen.GUIDES
                            },
                            onNavigateToGenerator = { currentScreen = BaristaScreen.GENERATOR }
                        )

                        BaristaScreen.SHOPPING -> ShoppingListScreen(
                            items = shoppingItems,
                            onToggleItem = { viewModel.toggleShoppingItem(it) },
                            onDeleteItem = { viewModel.deleteShoppingItem(it) },
                            onClearCompleted = { viewModel.clearCompletedShoppingItems() },
                            onAddItem = { name, qty, cat -> viewModel.addCustomShoppingItem(name, qty, cat) },
                            onShowMessage = { viewModel.showSnackbar(it) }
                        )
                    }
                }
            }
        }

        // Detailed Modal Bottom Sheet
        selectedRecipeForDetail?.let { recipe ->
            RecipeDetailSheet(
                recipe = recipe,
                visualizationState = visualizationState,
                onDismiss = { viewModel.showRecipeDetail(null) },
                onSaveToJournal = { viewModel.saveRecipeToJournal(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onAddToShoppingList = { viewModel.addRecipeIngredientsToShoppingList(it) },
                onStartBrewTimer = { viewModel.startTimerForRecipe(it) },
                onVisualizeBrew = { viewModel.visualizeRecipe(it) },
                onDismissVisualizationNotice = { viewModel.dismissVisualizationMessage() },
                onCopyRecipe = { r ->
                    val text = viewModel.formatRecipeForClipboard(r)
                    clipboardManager.setText(AnnotatedString(text))
                    viewModel.showSnackbar("Recipe & instructions copied to clipboard! ☕")
                },
                onStartBrewGuide = { brewMethod ->
                    val matchingGuide = viewModel.brewGuides.firstOrNull { it.title.contains(brewMethod, ignoreCase = true) }
                        ?: viewModel.brewGuides.first()
                    viewModel.selectBrewGuide(matchingGuide)
                    currentScreen = BaristaScreen.GUIDES
                },
                onUpdateJournalNotes = { id, rating, notes ->
                    viewModel.updateJournalNotesAndRating(id, rating, notes)
                },
                onDeleteRecipe = { id ->
                    viewModel.deleteRecipeFromJournal(id)
                }
            )
        }

        // Real-Time Step-by-Step Brew Timer Dialog
        if (isRecipeTimerDialogVisible && activeTimerRecipe != null) {
            ActiveBrewTimerDialog(
                recipe = activeTimerRecipe!!,
                stages = activeTimerStages,
                timerState = timerState,
                onStart = { viewModel.resumeRecipeTimer() },
                onPause = { viewModel.pauseRecipeTimer() },
                onReset = { viewModel.resetRecipeTimer() },
                onNextStage = { viewModel.advanceRecipeTimerStage() },
                onDismiss = { viewModel.dismissRecipeTimerDialog() }
            )
        }
    }
}
