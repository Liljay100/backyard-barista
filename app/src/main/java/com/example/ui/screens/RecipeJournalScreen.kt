package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RecipeEntity
import com.example.ui.components.BrewMethodBadge
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StarRatingBar
import com.example.ui.theme.CaramelAmber
import com.example.ui.theme.CoffeeDarkRoast
import com.example.ui.theme.CoffeeMocha
import com.example.ui.theme.HoneyBronze
import com.example.ui.theme.LightCaramel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeJournalScreen(
    journalRecipes: List<RecipeEntity>,
    favoriteRecipes: List<RecipeEntity>,
    onRecipeClick: (RecipeEntity) -> Unit,
    onFavoriteToggle: (RecipeEntity) -> Unit,
    onUpdateRatingAndNotes: (Long, Int, String) -> Unit,
    onDeleteRecipe: (Long) -> Unit,
    onLogCustomBrew: (String, String, String, String, Int, String) -> Unit,
    onStartBrewGuide: (String) -> Unit,
    onNavigateToGenerator: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All Journal Entries") }
    val filters = listOf("All Journal Entries", "Favorites", "Crafted by Me")

    var showLogBrewDialog by remember { mutableStateOf(false) }

    // Dialog state
    var customName by remember { mutableStateOf("") }
    var customMethod by remember { mutableStateOf("Pour-Over") }
    var customRoast by remember { mutableStateOf("Medium") }
    var customMilk by remember { mutableStateOf("Oat Milk") }
    var customRating by remember { mutableIntStateOf(5) }
    var customNotes by remember { mutableStateOf("") }

    val displayedRecipes = when (selectedFilter) {
        "Favorites" -> favoriteRecipes
        "Crafted by Me" -> journalRecipes.filter { it.isUserCreated }
        else -> journalRecipes
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("recipe_journal_screen")
    ) {
        // Journal Banner Header
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Personal Recipe Journal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = { showLogBrewDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("log_custom_brew_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Brew")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rate your extractions, record tasting notes, and build your personalized specialty coffee library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Filter Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("journal_filter_${filter.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // Header Title
        item {
            SectionHeader(
                title = selectedFilter,
                subtitle = "${displayedRecipes.size} saved coffee recipes in database"
            )
        }

        if (displayedRecipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No saved brews found in this filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate a custom recipe or tap Log Brew to record your first tasting entry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToGenerator,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Generate a Recipe")
                        }
                    }
                }
            }
        } else {
            items(displayedRecipes, key = { it.id }) { recipe ->
                JournalEntryCard(
                    recipe = recipe,
                    onRecipeClick = { onRecipeClick(recipe) },
                    onFavoriteToggle = { onFavoriteToggle(recipe) },
                    onRatingChanged = { rating ->
                        onUpdateRatingAndNotes(recipe.id, rating, recipe.userNotes)
                    },
                    onSaveNotes = { notes ->
                        onUpdateRatingAndNotes(recipe.id, recipe.userRating, notes)
                    },
                    onDelete = { onDeleteRecipe(recipe.id) },
                    onStartBrew = { onStartBrewGuide(recipe.brewMethod) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialog for Logging a Custom Brew into Room Database
    if (showLogBrewDialog) {
        AlertDialog(
            onDismissRequest = { showLogBrewDialog = false },
            title = {
                Text(
                    text = "Log a Brew to Journal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Recipe / Brew Name") },
                        placeholder = { Text("e.g. Morning Ethiopian V60") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_brew_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customMethod,
                            onValueChange = { customMethod = it },
                            label = { Text("Brew Method") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customRoast,
                            onValueChange = { customRoast = it },
                            label = { Text("Roast") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your Rating:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    StarRatingBar(
                        rating = customRating,
                        onRatingChanged = { customRating = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = { Text("Personal Tasting Notes") },
                        placeholder = { Text("e.g. Crisp berry sweetness, velvety body.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            onLogCustomBrew(
                                customName,
                                customMethod,
                                customRoast,
                                customMilk,
                                customRating,
                                customNotes
                            )
                            customName = ""
                            customNotes = ""
                            showLogBrewDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_log_brew_btn")
                ) {
                    Text("Save to Journal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogBrewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun JournalEntryCard(
    recipe: RecipeEntity,
    onRecipeClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    onSaveNotes: (String) -> Unit,
    onDelete: () -> Unit,
    onStartBrew: () -> Unit
) {
    var isEditingNotes by remember { mutableStateOf(false) }
    var editedNotes by remember(recipe.userNotes) { mutableStateOf(recipe.userNotes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("journal_card_${recipe.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Method badge + Favorite + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrewMethodBadge(method = recipe.brewMethod)
                    Spacer(modifier = Modifier.width(6.dp))
                    DifficultyBadge(difficulty = recipe.difficulty)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (recipe.isFavorite) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Tagline
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onRecipeClick() }
            )

            Text(
                text = recipe.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            // Rating Bar (Directly editable on the card!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rating:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StarRatingBar(
                        rating = recipe.userRating,
                        onRatingChanged = onRatingChanged
                    )
                }

                Text(
                    text = "${recipe.roastType} Roast • ${recipe.ratioText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(8.dp))

            // Personal Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Tasting Notes",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { isEditingNotes = !isEditingNotes },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isEditingNotes) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Edit notes",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isEditingNotes) {
                OutlinedTextField(
                    value = editedNotes,
                    onValueChange = { editedNotes = it },
                    placeholder = { Text("Log your flavor impressions, grind tweaks, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Button(
                    onClick = {
                        onSaveNotes(editedNotes)
                        isEditingNotes = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Notes")
                }
            } else {
                Text(
                    text = if (recipe.userNotes.isNotBlank()) recipe.userNotes else "No personal tasting notes yet. Tap the pencil icon to log your notes!",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (recipe.userNotes.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Full Details & Start Brew Guide)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecipeClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recipe Specs")
                }

                Button(
                    onClick = onStartBrew,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Brew Guide")
                }
            }
        }
    }
}
