package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RecipeEntity
import com.example.ui.components.BrewMethodBadge
import com.example.ui.components.BrewVisualCard
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.FlavourFilterChip
import com.example.ui.components.SectionHeader
import com.example.ui.components.StarRatingBar
import com.example.ui.theme.CaramelAmber
import com.example.ui.theme.CaramelWarm
import com.example.ui.theme.CoffeeDarkRoast
import com.example.ui.theme.CoffeeMocha
import com.example.ui.theme.HoneyBronze
import com.example.ui.theme.LightCaramel
import com.example.ui.viewmodel.BrewVisualizationState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeGeneratorScreen(
    selectedFlavors: Set<String>,
    selectedRoast: String,
    selectedMilk: String,
    selectedBrewMethod: String,
    selectedStrength: String,
    isGenerating: Boolean,
    generatedRecipe: RecipeEntity?,
    visualizationState: BrewVisualizationState,
    onToggleFlavor: (String) -> Unit,
    onSelectRoast: (String) -> Unit,
    onSelectMilk: (String) -> Unit,
    onSelectBrewMethod: (String) -> Unit,
    onSelectStrength: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onRecipeClick: (RecipeEntity) -> Unit,
    onSaveToJournal: (RecipeEntity, Int, String) -> Unit,
    onFavoriteToggle: (RecipeEntity) -> Unit,
    onAddToShoppingList: (RecipeEntity) -> Unit,
    onStartBrewTimer: (RecipeEntity) -> Unit,
    onVisualizeBrew: (RecipeEntity) -> Unit,
    onDismissVisualizationNotice: () -> Unit,
    onCopyRecipe: (RecipeEntity) -> Unit,
    onStartBrewGuide: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val flavorOptions = listOf("Chocolaty", "Nutty", "Caramel", "Sweet", "Bold", "Fruity", "Floral")
    val roastOptions = listOf("Light", "Medium", "Medium-Dark", "Dark")
    val milkOptions = listOf(
        "None (Black)",
        "Whole Milk",
        "Oat Milk",
        "Almond Milk",
        "Soy Milk",
        "Cream / Half-and-Half",
        "Sweet Condensed Milk"
    )
    val brewMethods = listOf("Pour-Over", "Espresso", "French Press", "AeroPress", "Cold Brew", "Moka Pot")
    val strengthOptions = listOf("Mild", "Balanced", "Rich", "Intense")

    // In-result note and rating editor state
    var localRating by remember(generatedRecipe?.name) { mutableIntStateOf(5) }
    var localNotes by remember(generatedRecipe?.name) { mutableStateOf("") }
    var isSavedToJournal by remember(generatedRecipe?.name) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("recipe_generator_screen")
    ) {
        // Title Banner
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Specialty Coffee Recipe Generator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Select your desired flavor profiles, roast level, milk type, and strength. The intelligent barista engine computes dynamic brewing ratios, bean origin recommendations, and step-by-step instructions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Flavour Profiles
        SectionHeader(
            title = "1. Flavour Profiles",
            subtitle = "Select one or more tasting notes"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            flavorOptions.forEach { flavor ->
                FlavourFilterChip(
                    label = flavor,
                    isSelected = selectedFlavors.contains(flavor),
                    onClick = { onToggleFlavor(flavor) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Roast Type
        SectionHeader(
            title = "2. Roast Level",
            subtitle = "Shapes body, origin brightness, and extraction temperature"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            roastOptions.forEach { roast ->
                val isSelected = selectedRoast == roast
                SelectableOptionPill(
                    label = roast,
                    isSelected = isSelected,
                    onClick = { onSelectRoast(roast) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Brewing Method
        SectionHeader(
            title = "3. Brewing Method",
            subtitle = "Determines equipment, grind size, and extraction style"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            brewMethods.forEach { method ->
                val isSelected = selectedBrewMethod == method
                SelectableOptionChip(
                    label = method,
                    icon = Icons.Default.Coffee,
                    isSelected = isSelected,
                    onClick = { onSelectBrewMethod(method) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Milk Preference
        SectionHeader(
            title = "4. Milk & Dairy Choice",
            subtitle = "Pairing for microfoam, cortado, or pure black coffee"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            milkOptions.forEach { milk ->
                val isSelected = selectedMilk == milk
                SelectableOptionChip(
                    label = milk,
                    icon = Icons.Default.Opacity,
                    isSelected = isSelected,
                    onClick = { onSelectMilk(milk) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Coffee Strength
        SectionHeader(
            title = "5. Desired Coffee Strength",
            subtitle = "Fine-tunes the golden water-to-coffee ratio"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            strengthOptions.forEach { strength ->
                val isSelected = selectedStrength == strength
                SelectableOptionPill(
                    label = strength,
                    isSelected = isSelected,
                    onClick = { onSelectStrength(strength) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Generate Action Button
        Button(
            onClick = {
                isSavedToJournal = false
                onGenerateClick()
            },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("generate_recipe_btn")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Barista is crafting your recipe...")
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Craft My Specialty Recipe",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // DYNAMIC ON-PAGE RESULT DISPLAY
        if (generatedRecipe != null) {
            Spacer(modifier = Modifier.height(28.dp))

            SectionHeader(
                title = "Your Personalised Recipe",
                subtitle = "Formulated dynamically based on your preferences"
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generated_recipe_result_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BrewMethodBadge(method = generatedRecipe.brewMethod)
                            Spacer(modifier = Modifier.width(6.dp))
                            DifficultyBadge(difficulty = generatedRecipe.difficulty)
                        }

                        Text(
                            text = "${generatedRecipe.roastType} Roast",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title & Tagline
                    Text(
                        text = generatedRecipe.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = generatedRecipe.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Flavor Profile Chips
                    val flavors = generatedRecipe.flavorProfiles.split(",").map { it.trim() }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        flavors.forEach { f ->
                            Text(
                                text = "• $f",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Brew Visualizer (Image Render or Transparent Capability Notice)
                    BrewVisualCard(
                        visualizationState = visualizationState,
                        onDismissMessage = onDismissVisualizationNotice
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Golden Ratio & Extraction Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecBlock(label = "Ratio", value = generatedRecipe.ratioText, icon = Icons.Default.WaterDrop, modifier = Modifier.weight(1f))
                        SpecBlock(label = "Coffee Dose", value = "${generatedRecipe.coffeeGrams.toInt()}g", icon = Icons.Default.Scale, modifier = Modifier.weight(1f))
                        SpecBlock(label = "Water Yield", value = "${generatedRecipe.waterGrams.toInt()}g", icon = Icons.Default.AccessTime, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecBlock(label = "Ideal Temp", value = generatedRecipe.waterTemp, icon = Icons.Default.Thermostat, modifier = Modifier.weight(1f))
                        SpecBlock(label = "Grind Size", value = generatedRecipe.grindSize, icon = Icons.Default.Scale, modifier = Modifier.weight(1.4f))
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Ingredients & Accurate Measurements
                    Text(
                        text = "Ingredients & Accurate Measurements",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            generatedRecipe.ingredientsText.split("\n").filter { it.isNotBlank() }.forEach { ingredient ->
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step-by-Step Preparation Instructions
                    Text(
                        text = "Preparation Instructions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    generatedRecipe.instructionsText.split("\n").filter { it.isNotBlank() }.forEach { step ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Barista Master Tip
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Barista Recommendation",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = generatedRecipe.baristaTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // In-Result Rating & Notes Box for the Recipe Journal
                    Text(
                        text = "Save & Rate in Recipe Journal",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Add your initial rating and personal tasting notes to record this brew in your journal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rate:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StarRatingBar(
                            rating = localRating,
                            onRatingChanged = { localRating = it }
                        )
                    }

                    OutlinedTextField(
                        value = localNotes,
                        onValueChange = { localNotes = it },
                        label = { Text("Personal Notes & Tasting Impressions") },
                        placeholder = { Text("e.g. Delicious sweet body, perfectly balanced honey finish!") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("generator_notes_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Primary AI & Clipboard Actions: Copy Recipe & Visualize Brew
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onCopyRecipe(generatedRecipe) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_recipe_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Recipe Summary",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Recipe")
                        }

                        Button(
                            onClick = { onVisualizeBrew(generatedRecipe) },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyBronze),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("visualize_brew_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Visualize Brew with AI",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Visualize Brew")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons (Save to Journal, Add to Shopping List, Brew Guide)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveToJournal(generatedRecipe, localRating, localNotes)
                                isSavedToJournal = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSavedToJournal) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_to_journal_btn")
                        ) {
                            Icon(
                                imageVector = if (isSavedToJournal) Icons.Default.Check else Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSavedToJournal) "Saved in Journal!" else "Save to Journal")
                        }

                        Button(
                            onClick = { onAddToShoppingList(generatedRecipe) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generator_add_cart_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to List")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onStartBrewTimer(generatedRecipe) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_brew_timer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Brew Timer (${generatedRecipe.brewMethod})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { onStartBrewGuide(generatedRecipe.brewMethod) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generator_view_guide_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Coffee, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View ${generatedRecipe.brewMethod} Brewing Guide")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SpecBlock(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SelectableOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("opt_${label.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SelectableOptionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("opt_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
