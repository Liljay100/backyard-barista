package com.example.data.repository

import com.example.data.local.RecipeDao
import com.example.data.local.ShoppingDao
import com.example.data.models.BaristaRecommendationInput
import com.example.data.models.BrewMethodGuide
import com.example.data.models.BrewStep
import com.example.data.models.CoffeeQA
import com.example.data.models.RecipeEntity
import com.example.data.models.ShoppingItemEntity
import com.example.data.models.TroubleshootTip
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class CoffeeRepository(
    private val recipeDao: RecipeDao,
    private val shoppingDao: ShoppingDao
) {
    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()
    val favoriteRecipes: Flow<List<RecipeEntity>> = recipeDao.getFavoriteRecipes()
    val journalRecipes: Flow<List<RecipeEntity>> = recipeDao.getJournalRecipes()
    val shoppingItems: Flow<List<ShoppingItemEntity>> = shoppingDao.getAllItems()

    suspend fun checkAndSeedInitialData() {
        if (recipeDao.getCount() == 0) {
            recipeDao.insertAll(initialSpecialtyRecipes)
        }
        if (shoppingDao.getCount() == 0) {
            shoppingDao.insertAll(initialPantryShoppingItems)
        }
    }

    suspend fun saveRecipe(recipe: RecipeEntity): Long = recipeDao.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: RecipeEntity) = recipeDao.updateRecipe(recipe)
    suspend fun deleteRecipe(id: Long) = recipeDao.deleteRecipeById(id)
    suspend fun toggleFavorite(id: Long, isFav: Boolean) = recipeDao.updateFavorite(id, isFav)
    suspend fun updateJournalEntry(id: Long, rating: Int, notes: String) =
        recipeDao.updateRatingAndNotes(id, rating, notes)

    suspend fun addShoppingItem(item: ShoppingItemEntity): Long = shoppingDao.insertItem(item)
    suspend fun addShoppingItems(items: List<ShoppingItemEntity>) = shoppingDao.insertAll(items)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = shoppingDao.updateItem(item)
    suspend fun toggleShoppingItemPurchased(id: Long, isPurchased: Boolean) =
        shoppingDao.updatePurchasedState(id, isPurchased)
    suspend fun deleteShoppingItem(id: Long) = shoppingDao.deleteItemById(id)
    suspend fun clearCompletedShoppingItems() = shoppingDao.clearCompleted()

    fun getBrewMethodGuides(): List<BrewMethodGuide> = brewGuidesList
    fun getCoffeeQAs(): List<CoffeeQA> = baristaQAList

    /**
     * Highly dynamic specialty coffee recipe generator.
     * Computes distinct ratios, bean origins, measurements, and instructions
     * for every combination of flavours, roast, milk, method, and strength.
     */
    fun generateCustomRecipe(input: BaristaRecommendationInput): RecipeEntity {
        val method = input.brewMethod.ifEmpty { "Pour-Over" }
        val roast = input.roastType.ifEmpty { "Medium" }
        val milk = input.milkPreference.ifEmpty { "None (Black)" }
        val strength = input.strength.ifEmpty { "Balanced" }
        val flavors = if (input.selectedFlavors.isEmpty()) listOf("Caramel", "Chocolaty") else input.selectedFlavors

        // 1. Calculate precise bean dose & water volume tailored by Method + Strength
        val (coffeeGrams, waterGrams, ratioStr) = calculateDoseAndWater(method, strength)

        // 2. Select ideal water temperature adjusted by Roast and Method
        val waterTemp = calculateIdealTemp(method, roast)

        // 3. Define accurate grind size
        val grindSize = calculateGrindSize(method, strength)

        // 4. Calculate preparation time and difficulty level
        val (prepTime, difficulty) = calculateTimeAndDifficulty(method, strength)

        // 5. Select recommended bean origin based on roast & flavor notes
        val beanOrigin = selectBeanOrigin(roast, flavors)

        // 6. Generate a distinctive, dynamic artisanal recipe title & tagline
        val recipeName = generateArtisanalName(flavors, roast, milk, method, strength)
        val tagline = buildDynamicTagline(roast, milk, strength, flavors, beanOrigin)

        // 7. Compose tailored ingredients with exact quantities
        val ingredientsList = buildDynamicIngredients(
            beanOrigin = beanOrigin,
            roast = roast,
            coffeeGrams = coffeeGrams,
            waterGrams = waterGrams,
            milk = milk,
            flavors = flavors,
            method = method
        )

        // 8. Generate accurate, numbered step-by-step instructions with timestamps
        val instructionsList = buildDynamicInstructions(
            method = method,
            coffeeGrams = coffeeGrams,
            waterGrams = waterGrams,
            waterTemp = waterTemp,
            grindSize = grindSize,
            milk = milk,
            strength = strength
        )

        // 9. Craft descriptive flavor and mouthfeel description
        val flavorDesc = buildFlavorDescription(flavors, roast, milk, strength, beanOrigin)

        // 10. Generate professional Barista Master tip
        val baristaTip = buildBaristaMasterTip(method, roast, milk, strength)

        return RecipeEntity(
            name = recipeName,
            tagline = tagline,
            brewMethod = method,
            roastType = roast,
            milkPreference = milk,
            strength = strength,
            flavorProfiles = flavors.joinToString(", "),
            prepTime = prepTime,
            difficulty = difficulty,
            coffeeGrams = coffeeGrams,
            waterGrams = waterGrams,
            ratioText = ratioStr,
            grindSize = grindSize,
            waterTemp = waterTemp,
            ingredientsText = ingredientsList.joinToString("\n"),
            instructionsText = instructionsList.joinToString("\n"),
            flavorDescription = flavorDesc,
            baristaTip = baristaTip,
            userRating = 0,
            userNotes = "",
            isFavorite = false,
            isUserCreated = true,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun calculateDoseAndWater(method: String, strength: String): Triple<Double, Double, String> {
        return when (method) {
            "Pour-Over" -> when (strength) {
                "Mild" -> Triple(15.0, 260.0, "1:17.3")
                "Balanced" -> Triple(18.0, 288.0, "1:16.0")
                "Rich" -> Triple(20.0, 300.0, "1:15.0")
                "Intense" -> Triple(22.0, 310.0, "1:14.1")
                else -> Triple(18.0, 288.0, "1:16.0")
            }
            "Espresso" -> when (strength) {
                "Mild" -> Triple(17.0, 42.0, "1:2.5 (Lungo)")
                "Balanced" -> Triple(18.0, 36.0, "1:2.0 (Normale)")
                "Rich" -> Triple(19.0, 34.0, "1:1.8")
                "Intense" -> Triple(20.0, 30.0, "1:1.5 (Ristretto)")
                else -> Triple(18.0, 36.0, "1:2.0")
            }
            "French Press" -> when (strength) {
                "Mild" -> Triple(26.0, 430.0, "1:16.5")
                "Balanced" -> Triple(30.0, 450.0, "1:15.0")
                "Rich" -> Triple(33.0, 460.0, "1:13.9")
                "Intense" -> Triple(36.0, 470.0, "1:13.0")
                else -> Triple(30.0, 450.0, "1:15.0")
            }
            "AeroPress" -> when (strength) {
                "Mild" -> Triple(15.0, 225.0, "1:15.0")
                "Balanced" -> Triple(17.0, 220.0, "1:12.9")
                "Rich" -> Triple(19.0, 210.0, "1:11.0")
                "Intense" -> Triple(22.0, 180.0, "1:8.2 (Bypass)")
                else -> Triple(17.0, 220.0, "1:13.0")
            }
            "Cold Brew" -> when (strength) {
                "Mild" -> Triple(40.0, 480.0, "1:12.0 (Ready-to-drink)")
                "Balanced" -> Triple(55.0, 440.0, "1:8.0 (Concentrate)")
                "Rich" -> Triple(65.0, 455.0, "1:7.0 (Rich Concentrate)")
                "Intense" -> Triple(80.0, 400.0, "1:5.0 (Heavy Concentrate)")
                else -> Triple(55.0, 440.0, "1:8.0")
            }
            "Moka Pot" -> when (strength) {
                "Mild" -> Triple(18.0, 190.0, "1:10.5")
                "Balanced" -> Triple(20.0, 180.0, "1:9.0")
                "Rich" -> Triple(22.0, 175.0, "1:8.0")
                "Intense" -> Triple(24.0, 168.0, "1:7.0")
                else -> Triple(20.0, 180.0, "1:9.0")
            }
            else -> Triple(18.0, 288.0, "1:16.0")
        }
    }

    private fun calculateIdealTemp(method: String, roast: String): String {
        if (method == "Cold Brew") return "Chilled / Room Temp (18°C–20°C)"
        return when (roast) {
            "Light" -> "95°C–96°C (203°F–205°F)"
            "Medium" -> "92°C–94°C (198°F–201°F)"
            "Medium-Dark" -> "90°C–92°C (194°F–198°F)"
            "Dark" -> "87°C–89°C (188°F–192°F)"
            else -> "93°C (200°F)"
        }
    }

    private fun calculateGrindSize(method: String, strength: String): String {
        return when (method) {
            "Espresso" -> if (strength == "Intense") "Very Fine (Powder sugar with micro-grit)" else "Fine (Table sugar texture)"
            "Moka Pot" -> "Medium-Fine (Fine table salt)"
            "AeroPress" -> if (strength == "Intense") "Fine-Medium" else "Medium-Fine (Sand)"
            "Pour-Over" -> if (strength == "Intense") "Medium-Fine (Sea salt)" else "Medium (Kosher salt)"
            "French Press" -> "Coarse (Rock salt / breadcrumbs)"
            "Cold Brew" -> "Extra Coarse (Cracked peppercorns)"
            else -> "Medium"
        }
    }

    private fun calculateTimeAndDifficulty(method: String, strength: String): Pair<String, String> {
        val prepTime = when (method) {
            "Cold Brew" -> if (strength == "Intense") "20 hrs cold steep" else "16 hrs cold steep"
            "French Press" -> if (strength == "Intense") "5 mins" else "4.5 mins"
            "Pour-Over" -> if (strength == "Intense") "3.5 mins" else "3 mins"
            "AeroPress" -> "2 mins"
            "Espresso" -> "2.5 mins"
            "Moka Pot" -> "6 mins"
            else -> "4 mins"
        }
        val difficulty = when (method) {
            "Espresso" -> "Advanced"
            "Pour-Over" -> "Intermediate"
            "AeroPress" -> "Intermediate"
            "Moka Pot" -> "Beginner"
            "French Press" -> "Beginner"
            "Cold Brew" -> "Beginner"
            else -> "Beginner"
        }
        return prepTime to difficulty
    }

    private fun selectBeanOrigin(roast: String, flavors: List<String>): String {
        val hasFloralOrFruity = flavors.any { it == "Floral" || it == "Fruity" }
        val hasChocOrNutty = flavors.any { it == "Chocolaty" || it == "Nutty" }
        val hasCaramel = flavors.contains("Caramel")

        return when {
            roast == "Light" && hasFloralOrFruity -> "Ethiopian Yirgacheffe G1 Washed"
            roast == "Light" -> "Kenya Nyeri Peaberry SL28"
            roast == "Medium" && hasCaramel -> "Colombian Huila Supremo (Honey Process)"
            roast == "Medium" && hasFloralOrFruity -> "Costa Rican Tarrazú Geisha"
            roast == "Medium" -> "Guatemalan Huehuetenango Highland"
            roast == "Medium-Dark" && hasChocOrNutty -> "Brazilian Cerrado Sul de Minas (Natural)"
            roast == "Medium-Dark" -> "Papua New Guinea Sigri Estate"
            roast == "Dark" && hasChocOrNutty -> "Sumatran Mandheling Grade 1 (Wet-Hulled)"
            else -> "Artisan Continental Dark Blend"
        }
    }

    private fun generateArtisanalName(
        flavors: List<String>,
        roast: String,
        milk: String,
        method: String,
        strength: String
    ): String {
        val flavor1 = flavors.firstOrNull() ?: "Caramel"
        val flavor2 = flavors.getOrNull(1)

        val prefix = when (flavor1) {
            "Chocolaty" -> if (flavor2 == "Nutty") "Toasted Cocoa" else "Velvet Ganache"
            "Nutty" -> if (flavor2 == "Caramel") "Praline Pecan" else "Toasted Almond"
            "Caramel" -> if (flavor2 == "Sweet") "Golden Honeycomb" else "Artisanal Caramel"
            "Sweet" -> if (flavor2 == "Floral") "Wildflower Honey" else "Dulce de Néctar"
            "Bold" -> if (roast == "Dark") "Midnight Obsidian" else "Highland Reserve"
            "Fruity" -> if (flavor2 == "Floral") "Bergamot Blossom" else "Sun-Drenched Berry"
            "Floral" -> "Jasmine Orchid"
            else -> "Craft Reserve"
        }

        val suffix = if (milk != "None (Black)") {
            when {
                milk.contains("Oat") -> "Oat Miel"
                milk.contains("Almond") -> "Almond Cortado"
                milk.contains("Condensed") -> "Café Bombón"
                milk.contains("Cream") -> "Sweet Cream Breve"
                milk.contains("Soy") -> "Silk Cloud Latte"
                method == "Espresso" -> if (strength == "Intense") "Macchiato Ristretto" else "Velvet Flat White"
                else -> "Artisan Café au Lait"
            }
        } else {
            when (method) {
                "Pour-Over" -> if (strength == "Intense") "Single-Origin Extraction" else "Reserve V60 Brew"
                "French Press" -> "Immersion Plunge"
                "Cold Brew" -> if (strength == "Intense") "Cold Draft Concentrate" else "Amber Cold Brew"
                "AeroPress" -> "Inverted Crema Press"
                "Moka Pot" -> "Dolce Stovetop Crema"
                else -> "Specialty Drip"
            }
        }

        return "$prefix $suffix"
    }

    private fun buildDynamicTagline(
        roast: String,
        milk: String,
        strength: String,
        flavors: List<String>,
        origin: String
    ): String {
        val milkDesc = if (milk != "None (Black)") "paired with silky $milk" else "served clean and black"
        return "A ${strength.lowercase()} $roast roast craft creation featuring $origin, $milkDesc highlighting ${flavors.joinToString(" & ").lowercase()} nuances."
    }

    private fun buildDynamicIngredients(
        beanOrigin: String,
        roast: String,
        coffeeGrams: Double,
        waterGrams: Double,
        milk: String,
        flavors: List<String>,
        method: String
    ): List<String> {
        val list = mutableListOf<String>()
        list.add("• ${coffeeGrams.toInt()}g freshly ground $beanOrigin ($roast Roast)")
        list.add("• ${waterGrams.toInt()}g pure filtered water (chlorine-free, 75–125 ppm TDS)")

        if (milk != "None (Black)") {
            val milkVolume = when (method) {
                "Espresso" -> if (milk.contains("Condensed")) "30ml" else "150ml"
                "AeroPress" -> "120ml"
                "Cold Brew" -> "100ml"
                else -> "140ml"
            }
            list.add("• $milkVolume fresh $milk (chilled prior to steaming or frothing)")
        }

        // Add targeted flavor accents based on selections
        if (flavors.contains("Caramel")) {
            list.add("• 1 tsp artisanal salted caramel syrup or grated raw panela")
        }
        if (flavors.contains("Chocolaty")) {
            list.add("• 1 tsp shavings of 70% single-origin dark chocolate or Dutch cacao powder")
        }
        if (flavors.contains("Nutty")) {
            list.add("• 1 drop pure natural toasted hazelnut or almond essence")
        }
        if (flavors.contains("Sweet")) {
            list.add("• 1 tsp wild raw blossom honey or organic turbinado sugar crystals")
        }
        if (flavors.contains("Fruity") && method == "Cold Brew") {
            list.add("• Optional: Expressed fresh orange peel twist across the glass rim")
        }
        if (flavors.contains("Floral")) {
            list.add("• 1 drop organic culinary orange blossom or rosewater mist (optional)")
        }

        return list
    }

    private fun buildDynamicInstructions(
        method: String,
        coffeeGrams: Double,
        waterGrams: Double,
        waterTemp: String,
        grindSize: String,
        milk: String,
        strength: String
    ): List<String> {
        val steps = mutableListOf<String>()
        val c = coffeeGrams.toInt()
        val w = waterGrams.toInt()

        when (method) {
            "Pour-Over" -> {
                val bloomWater = (c * 2.5).toInt()
                val secondPourTarget = (w * 0.6).toInt()
                steps.add("1. Rinsing: Place paper filter in cone, rinse thoroughly with hot water to strip paper flavor, and discard rinse water.")
                steps.add("2. Dose & Bed: Add ${c}g ground coffee ($grindSize). Create a small central indentation in the coffee bed with your thumb.")
                steps.add("3. The Bloom (0:00–0:45): Pour ${bloomWater}g water at $waterTemp spiraling outwards. Gently swirl cone twice to saturate all grounds and release trapped CO2.")
                steps.add("4. Main Pulse (0:45–1:30): Pour continuously in slow concentric circles until the digital scale reads ${secondPourTarget}g.")
                steps.add("5. Final Drawdown (1:30–3:15): Pour remaining water up to ${w}g. Give the dripper one gentle swirl for a flat coffee bed, allow full drawdown, then decant.")
            }
            "Espresso" -> {
                steps.add("1. Portafilter Prep: Dose exactly ${c}g of fine grounds evenly into a dry, pre-heated portafilter basket.")
                steps.add("2. WDT & Tamping: Use a WDT acupuncture tool to declump grounds, then tamp firmly with 25–30 lbs level pressure.")
                steps.add("3. Group Flush: Flush group head for 2 seconds to stabilize temperature, then lock portafilter firmly.")
                steps.add("4. Pull Shot: Extract ${w}g in 25–30 seconds. Watch for a dense hazelnut tiger-striped crema stream.")
                if (milk != "None (Black)") {
                    steps.add("5. Milk Texturing: Steam $milk in a stainless pitcher creating glossy wet paint microfoam at 60°C–65°C. Pour velvety latte art.")
                }
            }
            "French Press" -> {
                steps.add("1. Preheat: Warm glass carafe with hot water for 30 seconds, then dump out.")
                steps.add("2. Immersion Dose: Add ${c}g of coarse ground coffee ($grindSize).")
                steps.add("3. Rapid Pour: Pour all ${w}g of water at $waterTemp vigorously, ensuring every ground is fully submerged.")
                steps.add("4. Undisturbed Steep: Place lid on carafe without depressing the plunger. Steep undisturbed for 4 minutes.")
                steps.add("5. Crust Break & Skim: At 4:00, gently break the floating crust with two spoons and skim away white foam and floating fines.")
                steps.add("6. Press & Decant: Lower plunger to the liquid surface (do not crush bed). Pour immediately into cups to stop extraction.")
            }
            "AeroPress" -> {
                val bloomAmt = (c * 3).toInt()
                steps.add("1. Setup: Insert plunger into AeroPress inverted position. Rinse paper filter in cap with hot water.")
                steps.add("2. Dose & Bloom: Add ${c}g coffee ($grindSize). Pour ${bloomAmt}g water at $waterTemp and stir 5 times. Wait 30 seconds.")
                steps.add("3. Fill Chamber: Pour remaining water up to ${w}g. Stir gently 3 times.")
                steps.add("4. Seal & Steep: Screw on filter cap securely. Let steep until the timer reaches 1:30.")
                steps.add("5. Flip & Plunge: Smoothly invert AeroPress onto mug. Press down with steady gentle pressure for 30s. Halt when you hear the first hiss.")
            }
            "Cold Brew" -> {
                steps.add("1. Grind Setup: Grind ${c}g fresh whole beans on the coarsest setting ($grindSize).")
                steps.add("2. Cold Saturation: Place coffee in brewing jar, pour ${w}g cool filtered water over grounds.")
                steps.add("3. Stir & Seal: Stir gently until no dry pockets remain. Cover tightly with lid.")
                steps.add("4. Chill Steep: Place in the refrigerator or cool pantry for 16 to 18 hours.")
                steps.add("5. Dual Filtration: Pour through coarse mesh, then through a paper V60 filter for crystal clarity. Serve over clear ice.")
            }
            "Moka Pot" -> {
                steps.add("1. Pre-boil: Fill bottom boiler with pre-heated boiling water just below the safety valve.")
                steps.add("2. Basket Fill: Fill funnel basket with ${c}g coffee ($grindSize); level gently without compressing.")
                steps.add("3. Chamber Lock: Using a kitchen towel for heat protection, screw top chamber firmly onto base.")
                steps.add("4. Gentle Heat: Place on low flame with lid open. Watch for honey-toned stream.")
                steps.add("5. Stop Extraction: Remove pot from heat immediately when the stream turns blonde and sputters. Run base under cool water.")
            }
            else -> {
                steps.add("1. Grind ${c}g fresh coffee to $grindSize.")
                steps.add("2. Heat water to $waterTemp.")
                steps.add("3. Brew coffee and water with steady extraction.")
                steps.add("4. Decant and enjoy freshly brewed.")
            }
        }
        return steps
    }

    private fun buildFlavorDescription(
        flavors: List<String>,
        roast: String,
        milk: String,
        strength: String,
        origin: String
    ): String {
        val flavorList = flavors.joinToString(", ")
        val milkProfile = if (milk != "None (Black)") "rounded by velvety $milk sweetness" else "with pure cup transparency"
        return "Expresses articulate notes of $flavorList, $milkProfile. The $roast roast profile of $origin gives this cup a $strength strength profile with balanced malic brightness and a lingering sweet finish."
    }

    private fun buildBaristaMasterTip(
        method: String,
        roast: String,
        milk: String,
        strength: String
    ): String {
        return when (method) {
            "Pour-Over" -> if (roast == "Light") {
                "Light roast density requires high heat (95°C) to dissolve origin fruit sugars. Pour low and slow to avoid breaking the coffee bed."
            } else {
                "For $roast roast, keep kettle temperature around 91°C–92°C to prevent pulling astringent dry tannins in late drawdown."
            }
            "Espresso" -> if (strength == "Intense") {
                "A ristretto 1:1.5 ratio accentuates rich viscous body and dark chocolate notes. Grind slightly finer and stop extraction at 25 seconds."
            } else {
                "Always purge 2 seconds of hot water through your group head before locking in the portafilter to eliminate stale coffee oils."
            }
            "French Press" -> "James Hoffmann tip: Breaking the crust at 4 minutes and skimming floating froth removes bitter silt, yielding pour-over clarity with French press body."
            "AeroPress" -> "The moment you hear the hiss of air passing through the grounds, stop pressing immediately! The final foam contains harsh bitter compounds."
            "Cold Brew" -> "For best iced clarity, double-filter your concentrate through a wet paper V60 filter. It eliminates silt while preserving rich chocolate oils."
            "Moka Pot" -> "Always use boiling water in the bottom reservoir so your coffee grounds aren't baked on the stove while waiting for water to heat."
            else -> "Use fresh whole beans ground within 15 minutes of brewing. Volatile aromatics begin dissipating rapidly once cracked open."
        }
    }

    companion object {
        val initialPantryShoppingItems = listOf(
            ShoppingItemEntity(name = "Ethiopian Yirgacheffe Whole Beans", quantity = "250g", category = "Beans & Grounds"),
            ShoppingItemEntity(name = "Columbian Supremo Medium Roast", quantity = "250g", category = "Beans & Grounds"),
            ShoppingItemEntity(name = "Barista Edition Oat Milk", quantity = "1 Liter", category = "Milk & Dairy"),
            ShoppingItemEntity(name = "Demerara Brown Sugar", quantity = "500g", category = "Syrups & Flavors"),
            ShoppingItemEntity(name = "V60 Bleached Paper Filters (Size 02)", quantity = "1 Pack (100ct)", category = "Equipment & Gear"),
            ShoppingItemEntity(name = "Organic Ceylon Cinnamon Sticks", quantity = "50g", category = "Syrups & Flavors")
        )

        val initialSpecialtyRecipes = listOf(
            RecipeEntity(
                name = "Velveteen Brown Sugar Oat Latte",
                tagline = "Silky textured oat microfoam layered over caramelized espresso.",
                brewMethod = "Espresso",
                roastType = "Medium-Dark",
                milkPreference = "Oat Milk",
                strength = "Rich",
                flavorProfiles = "Caramel, Chocolaty, Sweet",
                prepTime = "4 mins",
                difficulty = "Intermediate",
                coffeeGrams = 18.0,
                waterGrams = 36.0,
                ratioText = "1:2",
                grindSize = "Fine",
                waterTemp = "93°C (200°F)",
                ingredientsText = "• 18g specialty espresso blend (Medium-Dark)\n• 36g double shot espresso extraction\n• 160ml Barista Oat Milk\n• 1 tsp brown sugar & pinch of cinnamon",
                instructionsText = "1. Extract 36g double espresso over 1 tsp raw brown sugar.\n2. Swirl cup to dissolve crystals into thick crema.\n3. Steam oat milk to 60°C creating creamy, glossy microfoam.\n4. Pour latte art from a low height for velvety mouthfeel. Garnish with dusted cinnamon.",
                flavorDescription = "Deep butterscotch sweetness, toasted oats, and comforting milk chocolate finish.",
                baristaTip = "Oat milk burns easily above 65°C—keep it silky and sweet by stopping steaming right when the pitcher feels comfortably warm to touch.",
                userRating = 5,
                userNotes = "My daily morning favorite! Super velvety texture.",
                isFavorite = true,
                isUserCreated = false
            ),
            RecipeEntity(
                name = "Golden Sunrise Ethiopian Pour-Over",
                tagline = "Vibrant clarity with notes of bergamot, jasmine, and ripe peach.",
                brewMethod = "Pour-Over",
                roastType = "Light",
                milkPreference = "None (Black)",
                strength = "Balanced",
                flavorProfiles = "Floral, Fruity, Sweet",
                prepTime = "3.5 mins",
                difficulty = "Intermediate",
                coffeeGrams = 18.0,
                waterGrams = 288.0,
                ratioText = "1:16",
                grindSize = "Medium (Kosher Salt)",
                waterTemp = "95°C (203°F)",
                ingredientsText = "• 18g Ethiopian Yirgacheffe Washed beans (Light roast)\n• 288g soft filtered water at 95°C\n• 1 unbleached V60 paper filter (rinsed)",
                instructionsText = "1. Rinse paper filter thoroughly with hot water.\n2. Add 18g grounds; level bed and make small thumb depression.\n3. Bloom with 50g water for 45s. Swirl gently twice.\n4. Second pour: pour in slow concentric rings to 160g by 1:15.\n5. Third pour: pour gently to 288g by 2:00. Drawdown complete at 3:10.",
                flavorDescription = "Intense jasmine tea aroma, bright candied lemon acidity, and succulent peach finish.",
                baristaTip = "Use high-temperature water (95°C) for light roast washed coffees to dissolve the delicate floral origin compounds.",
                userRating = 5,
                userNotes = "Tastes like floral black tea with peach nectar. Extraordinary.",
                isFavorite = true,
                isUserCreated = false
            ),
            RecipeEntity(
                name = "Midnight Mocha French Press",
                tagline = "Heavy-bodied comfort brew infused with dark cocoa and roasted walnuts.",
                brewMethod = "French Press",
                roastType = "Dark",
                milkPreference = "None (Black)",
                strength = "Intense",
                flavorProfiles = "Chocolaty, Nutty, Bold",
                prepTime = "5 mins",
                difficulty = "Beginner",
                coffeeGrams = 30.0,
                waterGrams = 450.0,
                ratioText = "1:15",
                grindSize = "Coarse (Rock Salt)",
                waterTemp = "94°C (201°F)",
                ingredientsText = "• 30g dark roasted Sumatran or Colombian beans\n• 450g hot filtered water\n• Optional: 1 square 85% dark chocolate melted in mug",
                instructionsText = "1. Add 30g coarse grounds to French press.\n2. Pour 450g water rapidly, wetting every ground thoroughly.\n3. Place plunger on top without pressing down. Wait 4 minutes.\n4. Gently stir the crust at 4:00 with a wooden paddle. Skim floating fines.\n5. Press plunger down slowly over 20 seconds. Pour immediately into mugs.",
                flavorDescription = "Smoky dark chocolate, toasted walnut, molasses sweetness, and velvety thick crema body.",
                baristaTip = "Always decant the entire brew immediately after plunging so the grounds don't stew and turn bitter in the carafe.",
                userRating = 4,
                userNotes = "Incredible on rainy afternoons. Full-bodied perfection.",
                isFavorite = false,
                isUserCreated = false
            ),
            RecipeEntity(
                name = "Bourbon Vanilla Cold Foam Brew",
                tagline = "Slow 18-hour cold steeped concentrate crowned with whipped vanilla sweet cream.",
                brewMethod = "Cold Brew",
                roastType = "Medium-Dark",
                milkPreference = "Cream / Half-and-Half",
                strength = "Rich",
                flavorProfiles = "Caramel, Sweet, Nutty",
                prepTime = "18 hrs steep",
                difficulty = "Beginner",
                coffeeGrams = 60.0,
                waterGrams = 480.0,
                ratioText = "1:8 Concentrate",
                grindSize = "Extra Coarse",
                waterTemp = "Chilled Filtered Water",
                ingredientsText = "• 60g coarsely crushed beans\n• 480g cold filtered water\n• 50ml heavy cream + 25ml milk + 1 tsp Madagascar vanilla syrup (for foam)\n• Crystal clear ice cubes",
                instructionsText = "1. Steep coffee and water in a mason jar or cold brewer for 18 hours in the fridge.\n2. Double filter through paper to produce ultra-clean concentrate.\n3. Froth cream, milk, and vanilla syrup with an electric hand whisk for 25 seconds until silky foam.\n4. Fill glass with ice, add 150ml cold brew concentrate, top with cascading vanilla foam.",
                flavorDescription = "Silky contrast of icy dark chocolate cold brew and fluffy, sweet bourbon vanilla cloud.",
                baristaTip = "Cold frothing works best when both the cream and milk are ice-cold straight from the back of the fridge.",
                userRating = 5,
                userNotes = "Better than any cafe cold brew!",
                isFavorite = true,
                isUserCreated = false
            ),
            RecipeEntity(
                name = "AeroPress Modern Crema Shot",
                tagline = "Inverted AeroPress method extracting syrupy mouthfeel and bright citrus clarity.",
                brewMethod = "AeroPress",
                roastType = "Medium",
                milkPreference = "None (Black)",
                strength = "Rich",
                flavorProfiles = "Caramel, Fruity, Bold",
                prepTime = "2 mins",
                difficulty = "Intermediate",
                coffeeGrams = 17.0,
                waterGrams = 200.0,
                ratioText = "1:12",
                grindSize = "Medium-Fine",
                waterTemp = "88°C (190°F)",
                ingredientsText = "• 17g freshly ground medium roast\n• 200g water at 88°C\n• 1 AeroPress paper filter (rinsed)",
                instructionsText = "1. Set AeroPress in inverted position (plunger on table).\n2. Add grounds and pour 50g water; stir 10 seconds and let bloom for 30s.\n3. Add remaining 150g water; stir gently 3 times.\n4. Attach filter cap, flip firmly onto mug at 1:30.\n5. Press gently for 30 seconds; stop right when the hiss begins.",
                flavorDescription = "Honeyed caramel, sweet grapefruit zest, and a smooth, syrupy chocolate body.",
                baristaTip = "Stopping the press as soon as air hisses through avoids channeling bitter fines into your cup.",
                userRating = 4,
                userNotes = "Crisp, fast, and ultra repeatable.",
                isFavorite = false,
                isUserCreated = false
            ),
            RecipeEntity(
                name = "Stovetop Dolce Moka Crema",
                tagline = "Traditional Italian stovetop coffee with whipped sugar crema foam.",
                brewMethod = "Moka Pot",
                roastType = "Dark",
                milkPreference = "None (Black)",
                strength = "Intense",
                flavorProfiles = "Bold, Caramel, Chocolaty",
                prepTime = "6 mins",
                difficulty = "Beginner",
                coffeeGrams = 20.0,
                waterGrams = 180.0,
                ratioText = "1:9",
                grindSize = "Medium-Fine",
                waterTemp = "Pre-heated Water in Boiler",
                ingredientsText = "• 20g dark espresso roast coffee\n• Preheated water to safety valve\n• 1 tbsp brown sugar (for whipped crema)",
                instructionsText = "1. Add boiling water into the lower boiler up to the safety valve.\n2. Fill filter basket with grounds; level flat without tamping.\n3. Assemble tightly and brew over low flame with lid cracked open.\n4. Catch the first 1 tsp of dark coffee extract; whip aggressively with 1 tbsp sugar in a cup until pale foam.\n5. Pour remaining hot moka coffee into the foam and stir to create spontaneous Italian crema.",
                flavorDescription = "Syrupy espresso richness, caramelized demerara sugar, and baker's dark chocolate.",
                baristaTip = "Always preheat the water in the boiler so coffee grounds aren't baked on the stove before extraction.",
                userRating = 5,
                userNotes = "The whipped sugar crema technique is pure magic.",
                isFavorite = false,
                isUserCreated = false
            )
        )

        val brewGuidesList = listOf(
            BrewMethodGuide(
                id = "espresso",
                title = "Espresso Extraction",
                subtitle = "The foundation of specialty coffee drinks: 9 bars of pressure, concentrated liquid gold.",
                iconType = "espresso",
                difficulty = "Advanced",
                idealGrind = "Fine (Powdered sugar with slight grit)",
                defaultRatio = "1:2",
                defaultCoffeeGrams = 18.0,
                defaultWaterGrams = 36.0,
                idealTemp = "93°C (200°F)",
                totalTimeText = "25–30 seconds",
                totalDurationSeconds = 30,
                summary = "Extracting a double shot under high pressure delivers unmatched body, concentrated aroma, and rich hazelnut crema.",
                steps = listOf(
                    BrewStep(1, "Dose & Distribution", 10, "Dose 18g evenly into dry portafilter basket. Use a WDT needle tool to declump.", isKeyPhase = true),
                    BrewStep(2, "Level & Tamp", 5, "Tamp firmly and level with flat pressure (25-30 lbs). Wipe rim clean.", isKeyPhase = false),
                    BrewStep(3, "Flush & Lock", 5, "Flush 2 seconds of water through grouphead. Lock portafilter firmly.", isKeyPhase = false),
                    BrewStep(4, "Extraction Pull", 30, "Engage pump immediately. Watch golden streams coalesce into tiger-striped crema reaching 36g at 28s.", isKeyPhase = true)
                ),
                proTips = listOf(
                    "Sour taste? Your shot flowed too fast (under-extracted). Grind finer.",
                    "Bitter, dry astringency? Extraction took over 35 seconds (over-extracted). Grind coarser.",
                    "Channeling? Distribute grounds carefully with a WDT tool before tamping."
                ),
                troubleshooting = listOf(
                    TroubleshootTip("Shot runs under 20s with pale crema", "Grind is too coarse or dose is too low", "Grind 2 notches finer and verify dose with a scale."),
                    TroubleshootTip("Machine chokes or drips agonizingly", "Grind is too fine or tamped with excessive force", "Coarsen grind slightly; ensure gentle level tamp.")
                )
            ),
            BrewMethodGuide(
                id = "pourover",
                title = "Pour-Over (V60 / Chemex)",
                subtitle = "Crisp clarity, tea-like lightness, and distinct origin flavor nuances.",
                iconType = "pour_over",
                difficulty = "Intermediate",
                idealGrind = "Medium (Table/Kosher salt)",
                defaultRatio = "1:16",
                defaultCoffeeGrams = 18.0,
                defaultWaterGrams = 288.0,
                idealTemp = "92°C–96°C",
                totalTimeText = "3 mins 15 secs",
                totalDurationSeconds = 195,
                summary = "Pour-over filters remove oils and silt, allowing single-origin fruity and floral notes to shine with crystal clarity.",
                steps = listOf(
                    BrewStep(1, "Filter Rinse", 15, "Rinse paper filter with hot water to wash away paper taste; discard water.", isKeyPhase = false),
                    BrewStep(2, "The Bloom", 45, "Pour 50g water evenly in spiral. Swirl gently twice. Watch grounds expand and degas CO2.", isKeyPhase = true),
                    BrewStep(3, "Second Pour (Structure)", 45, "Pour in slow concentric circles from inside out until scale reaches 160g by 1:30.", isKeyPhase = true),
                    BrewStep(4, "Final Pour & Drawdown", 90, "Pour remaining water up to 288g by 2:15. Let bed draw down evenly flat by 3:15.", isKeyPhase = true)
                ),
                proTips = listOf(
                    "Keep your pouring kettle spout low (approx. 2 inches above bed) to minimize unwanted agitation.",
                    "A flat coffee bed at completion confirms even water flow without channeling.",
                    "Light roasts thrive at 94°C–96°C; dark roasts brew sweeter at 90°C–92°C."
                ),
                troubleshooting = listOf(
                    TroubleshootTip("Water stalls and takes over 4.5 minutes", "Grind has too many fine particles clogging the filter", "Coarsen your grind by 2 notches."),
                    TroubleshootTip("Thin, sour, or hollow cup", "Water flowed through too fast without extracting soluble solids", "Grind finer and pour in slower controlled pulses.")
                )
            ),
            BrewMethodGuide(
                id = "frenchpress",
                title = "French Press (Cafetière)",
                subtitle = "Immersion brewing for deep, velvety body, rich oils, and comforting warmth.",
                iconType = "french_press",
                difficulty = "Beginner",
                idealGrind = "Coarse (Coarse sea salt / breadcrumbs)",
                defaultRatio = "1:15",
                defaultCoffeeGrams = 30.0,
                defaultWaterGrams = 450.0,
                idealTemp = "94°C (201°F)",
                totalTimeText = "4 mins 30 secs",
                totalDurationSeconds = 270,
                summary = "Full immersion allows coffee oils and micro-particles to remain, creating an unmatched rich mouthfeel.",
                steps = listOf(
                    BrewStep(1, "Preheat & Dose", 20, "Warm glass with hot water. Add 30g coarse ground coffee.", isKeyPhase = false),
                    BrewStep(2, "Aggressive Saturation", 30, "Pour all 450g water briskly, ensuring all grounds are saturated.", isKeyPhase = true),
                    BrewStep(3, "Undisturbed Steep", 210, "Rest lid on carafe without plunging. Let steep undisturbed for 4 minutes.", isKeyPhase = true),
                    BrewStep(4, "Crust Break & Gentle Plunge", 30, "Break crust with spoon, skim foam, then press plunger gently down.", isKeyPhase = true)
                ),
                proTips = listOf(
                    "James Hoffmann technique: Skimming foam and crust at 4 minutes yields an extraordinarily clean cup without mud.",
                    "Never leave brewed coffee sitting in the French press after plunging; decant immediately into a serving carafe."
                ),
                troubleshooting = listOf(
                    TroubleshootTip("Excessive silt or sludge in cup", "Grind is too fine for the metal mesh filter", "Use a coarser grind with uniform particle distribution."),
                    TroubleshootTip("Hard to push plunger down", "Grind is packed or too fine", "Never force plunger; pull back slightly and press gently.")
                )
            ),
            BrewMethodGuide(
                id = "aeropress",
                title = "AeroPress (Inverted Method)",
                subtitle = "Ultra-versatile pneumatic brewer yielding syrupy espresso-like body or clean drip coffee.",
                iconType = "aeropress",
                difficulty = "Intermediate",
                idealGrind = "Medium-Fine (Fine sand)",
                defaultRatio = "1:13",
                defaultCoffeeGrams = 17.0,
                defaultWaterGrams = 220.0,
                idealTemp = "88°C–90°C",
                totalTimeText = "2 minutes",
                totalDurationSeconds = 120,
                summary = "Combines full immersion with gentle air pressure through a microfilter for rapid, grit-free extraction.",
                steps = listOf(
                    BrewStep(1, "Invert & Dose", 20, "Set plunger inside chamber and invert onto counter. Add 17g medium-fine coffee.", isKeyPhase = false),
                    BrewStep(2, "Bloom & Agitate", 30, "Add 60g water at 88°C. Stir gently for 10 seconds to saturate all grounds.", isKeyPhase = true),
                    BrewStep(3, "Fill & Cap", 40, "Fill remaining water to 220g. Screw rinsed filter cap on securely.", isKeyPhase = false),
                    BrewStep(4, "Flip & Plunge", 30, "At 1:30, invert smoothly onto your mug and press steadily for 30 seconds.", isKeyPhase = true)
                ),
                proTips = listOf(
                    "Stop pressing when you hear the first hiss of air escaping to keep out bitter late-extraction compounds.",
                    "The inverted method completely prevents premature bypass drip before steeping finishes."
                ),
                troubleshooting = listOf(
                    TroubleshootTip("Plunger slips or leaks during flip", "Plunger was placed too shallow into chamber", "Insert rubber seal at least 1 cm into chamber before adding grounds.")
                )
            ),
            BrewMethodGuide(
                id = "coldbrew",
                title = "Cold Brew Infusion",
                subtitle = "Slow, 16–20 hour room temperature or refrigerated steep with 70% less acidity.",
                iconType = "cold_brew",
                difficulty = "Beginner",
                idealGrind = "Extra Coarse (Cracked peppercorns)",
                defaultRatio = "1:8 Concentrate",
                defaultCoffeeGrams = 60.0,
                defaultWaterGrams = 480.0,
                idealTemp = "Cold / Room Temp Filtered Water",
                totalTimeText = "16–18 hours",
                totalDurationSeconds = 300,
                summary = "Cold water extracts sweet, chocolaty solubles while leaving harsh acidic compounds behind in the grounds.",
                steps = listOf(
                    BrewStep(1, "Coarse Grind", 30, "Grind 60g beans on coarsest setting to prevent cloudiness.", isKeyPhase = false),
                    BrewStep(2, "Submerge & Stir", 60, "Pour 480g cold filtered water over grounds. Stir gently until completely wet.", isKeyPhase = true),
                    BrewStep(3, "Cold Steeping", 180, "Cover vessel and chill in refrigerator for 16 to 18 hours.", isKeyPhase = true),
                    BrewStep(4, "Double Filtration", 60, "Strain through fine mesh, followed by paper filter for radiant clarity.", isKeyPhase = true)
                ),
                proTips = listOf(
                    "Concentrate stays fresh in the fridge for up to 10 days in an airtight bottle.",
                    "Serve 1:1 with cold milk, sparkling tonic water, or cold filtered water over clear ice."
                ),
                troubleshooting = listOf(
                    TroubleshootTip("Cloudy, chalky cold brew", "Fine dust particles bypassed filter", "Double filter through a paper V60 cone or Chemex.")
                )
            ),
            BrewMethodGuide(
                id = "mokapot",
                title = "Moka Pot (Stovetop Espresso)",
                subtitle = "Classic Italian kitchen essential generating rich, velvety, steam-pressured coffee.",
                iconType = "moka_pot",
                difficulty = "Beginner",
                idealGrind = "Medium-Fine (Fine table salt)",
                defaultRatio = "1:9",
                defaultCoffeeGrams = 20.0,
                defaultWaterGrams = 180.0,
                idealTemp = "Boiling water in base",
                totalTimeText = "5–6 minutes",
                totalDurationSeconds = 300,
                summary = "Steam pressure in the lower chamber pushes hot water upwards through the coffee basket into the upper server.",
                steps = listOf(
                    BrewStep(1, "Pre-heat Water", 60, "Boil water separately and fill lower boiler up to safety valve line.", isKeyPhase = false),
                    BrewStep(2, "Fill Basket", 30, "Fill basket with grounds; level flat without tamping or compressing.", isKeyPhase = true),
                    BrewStep(3, "Assemble Safely", 30, "Use a cloth to hold the hot base and screw the top chamber on firmly.", isKeyPhase = false),
                    BrewStep(4, "Low Heat Brew", 180, "Place on low heat. Watch for rich amber liquid pouring into column; remove at sputter.", isKeyPhase = true)
                ),
                proTips = listOf(
                    "Pre-boiling water prevents coffee grounds from roasting on the stove while waiting for water to heat.",
                    "Immediately run cold water over the bottom chamber when it begins sputtering to halt extraction instantly."
                ),
                troubleshooting = listOf(
                    TroubleshootTip("Coffee tastes burnt or metallic", "Cold water was used in base, scorching grounds", "Always start with boiling water in the bottom reservoir.")
                )
            )
        )

        val baristaQAList = listOf(
            CoffeeQA(
                1,
                "Why does my coffee taste sour or sharp?",
                "Sourness usually signals under-extraction. The water extracted early acidic compounds but didn't have time to dissolve sweet sugars. Solutions: Grind finer, increase water temperature, or extend brew time slightly.",
                "Extraction"
            ),
            CoffeeQA(
                2,
                "Why does my coffee taste dry and bitter?",
                "Bitterness and mouth-drying astringency indicate over-extraction. The water dissolved harsh plant matter and heavy tannins. Solutions: Grind coarser, decrease water temperature, or shorten steep time.",
                "Extraction"
            ),
            CoffeeQA(
                3,
                "How do I texture milk for silky latte art?",
                "Keep milk cold. Submerge the steam tip just below the surface (1cm) to introduce small 'tsst-tsst' sips of air until pitcher reaches body temp (~37°C), then sink tip deeper to create a vigorous vortex that polishes microfoam up to 60°C–65°C.",
                "Milk & Steaming"
            ),
            CoffeeQA(
                4,
                "What is 'the Bloom' and why is it essential?",
                "Fresh roasted coffee traps carbon dioxide gas inside bean cells. Pouring a small amount of hot water (2–3x coffee weight) releases this CO2. Skipping the bloom causes gas bubbles to repel water, leading to uneven extraction channels.",
                "Brewing Science"
            ),
            CoffeeQA(
                5,
                "How should I store my roasted coffee beans?",
                "Keep beans in an opaque, airtight container with a one-way degassing valve at room temperature away from heat, moisture, and direct sunlight. Avoid the freezer unless vacuum-sealed for long-term storage.",
                "Bean Care"
            ),
            CoffeeQA(
                6,
                "What is the ideal coffee-to-water ratio?",
                "The golden standard for drip and pour-over is 1:15 to 1:17 (approx. 60g coffee per 1 Liter of water). For espresso, standard is 1:2 (18g coffee to 36g liquid). For French press, 1:15 offers rich, rounded body.",
                "Brew Ratios"
            ),
            CoffeeQA(
                7,
                "Does water quality actually make a noticeable difference?",
                "Coffee is 98.5% water! Chlorinated or overly hard tap water dulls sweetness and creates chalky flavors. Use filtered water with balanced mineral content (magnesium and calcium help extract pleasant fruit and chocolate compounds).",
                "Water & Chemistry"
            )
        )
    }
}
