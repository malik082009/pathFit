package com.grittech.pathfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.CalorieMath
import com.grittech.pathfit.domain.ProfileRules
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.model.CommonCard
import com.grittech.pathfit.model.HomeTab
import com.grittech.pathfit.model.PickerMode
import com.grittech.pathfit.model.PlateItem
import com.grittech.pathfit.model.UsualMeal
import com.grittech.pathfit.ui.components.PrimaryButton
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CardRest
import com.grittech.pathfit.ui.theme.Cyan
import com.grittech.pathfit.ui.theme.Ink
import com.grittech.pathfit.ui.theme.InkMuted
import com.grittech.pathfit.ui.theme.Line
import com.grittech.pathfit.ui.theme.Mint
import com.grittech.pathfit.ui.theme.OnMint
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun QuickLogScreen(onBack: () -> Unit, onBuildMeal: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val slot = state.selectedSlot
    val slotLabel = ProfileRules.slotTitle(slot)
    val usuals = remember(state.usuals, slot) {
        state.usuals
            .filter { it.status == "active" && it.mealSlot == slot }
            .sortedWith(compareByDescending<UsualMeal> { it.pinned }.thenByDescending { it.lastUsedAt })
    }
    val commons = state.staples[slot].orEmpty()
    val dishHits = state.dishes
    var category by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        vm.ensureSlotSuggestions()
        vm.searchDishes("")
    }

    val filteredDishes = remember(dishHits, category, state.dishQuery) {
        val q = state.dishQuery.trim()
        dishHits.filter { dish ->
            val tagOk = category == "All" || dish.tags.any { it.contains(category, ignoreCase = true) } ||
                dish.nameEn.contains(category, ignoreCase = true) ||
                dish.detail.contains(category, ignoreCase = true)
            val queryOk = q.isBlank() ||
                dish.nameEn.contains(q, ignoreCase = true) ||
                dish.searchAliases.any { it.contains(q, ignoreCase = true) } ||
                dish.slug.contains(q, ignoreCase = true)
            tagOk && queryOk
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Ink,
                modifier = Modifier.clickable(onClick = onBack).padding(8.dp),
            )
            Text(
                "Quick Log",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            Icon(Icons.Filled.Info, contentDescription = null, tint = InkMuted, modifier = Modifier.padding(6.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Mint)
                    .clickable {
                        vm.selectTab(HomeTab.Profile)
                        onBack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Profile", tint = OnMint, modifier = Modifier.size(18.dp))
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "breakfast" to "Breakfast (Nashta)",
                    "lunch" to "Lunch (Dopahar)",
                    "dinner" to "Dinner (Raat)",
                    "snack" to "Snack (Shaam)",
                ).forEach { (key, label) ->
                    val selected = slot == key
                    Text(
                        label,
                        color = if (selected) OnMint else InkMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) Mint else CardRest)
                            .border(1.dp, if (selected) Mint else Line, CircleShape)
                            .clickable {
                                vm.selectSlot(key)
                                vm.ensureSlotSuggestions()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }

            QuickSearchField(
                value = state.dishQuery,
                hint = "Search catalog (e.g. anda, paratha, daal, chai)",
                onChange = { vm.searchDishes(it) },
            )
            Text(
                "Roman-Urdu smart index active…",
                color = InkMuted,
                fontSize = 11.sp,
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Mint.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .background(CardRest)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    Text(
                        "  Speed Combination",
                        color = Mint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    "Eating your regular staple? Pair and save your personalized everyday macro blueprint.",
                    color = InkMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
                PrimaryButton(
                    "+ Build My Usual",
                    onClick = {
                        vm.startCustom(slot)
                        onBuildMeal()
                    },
                    showArrow = false,
                    enabled = !state.busy,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Your Usuals ($slotLabel)",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                if (usuals.isNotEmpty()) {
                    Text(
                        "${usuals.size} Saved",
                        color = Mint,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    )
                }
            }
            if (usuals.isEmpty()) {
                Text(
                    "No saved usuals for this slot yet. Build My Usual or pin a Common meal.",
                    color = InkMuted,
                    fontSize = 12.sp,
                )
            } else {
                usuals.forEach { usual ->
                    UsualQuickCard(
                        usual = usual,
                        busy = state.busy,
                        onLog = { vm.logUsual(usual.id) },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Suggested Common Meals ($slotLabel)",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                Text(
                    " ${state.draft.countryCode.ifBlank { "PK" }} Curated",
                    color = Mint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                )
            }
            if (commons.isEmpty()) {
                Text("Loading curated suggestions…", color = InkMuted, fontSize = 12.sp)
            } else {
                commons.forEach { card ->
                    CommonQuickCard(
                        card = card,
                        onEditSave = {
                            vm.startCustom(slot)
                            vm.editCustom { draft ->
                                draft.copy(
                                    name = card.nameEn,
                                    items = card.items.mapIndexed { index, item ->
                                        PlateItem(
                                            key = "${item.dishId}-$index",
                                            dishId = item.dishId,
                                            name = item.nameEn,
                                            portion = item.defaultPortion.coerceAtLeast(0.5),
                                            unit = item.unit,
                                            kcalPerServing = if (item.defaultPortion == 0.0) {
                                                item.estimateKcal.toDouble()
                                            } else {
                                                item.estimateKcal / item.defaultPortion
                                            },
                                            proteinPer = item.proteinG,
                                            carbsPer = item.carbsG,
                                            fatPer = item.fatG,
                                            addedFatKcal = item.addedFatKcal,
                                            oilLevel = if (item.addedFatKcal > 0) "normal" else null,
                                        )
                                    },
                                )
                            }
                            onBuildMeal()
                        },
                        onPin = { vm.pinCommon(card) },
                        onLogOnce = { vm.logCommonOnce(card) },
                    )
                }
            }

            Text(
                "Single Dishes Catalog",
                color = Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "All",
                    "Eggs",
                    "Roti",
                    "Coffee",
                    "Salan",
                    "Chai",
                ).forEach { tag ->
                    val selected = category == tag
                    Text(
                        if (tag == "Eggs") "Eggs & Protein"
                        else if (tag == "Roti") "Rotis & Breads"
                        else if (tag == "Coffee") "Hot Drinks / Coffee"
                        else if (tag == "Salan") "Salans"
                        else tag,
                        color = if (selected) OnMint else InkMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) Mint else CardRest)
                            .border(1.dp, if (selected) Mint else Line, CircleShape)
                            .clickable { category = tag }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
            filteredDishes.take(24).forEach { dish ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardRest)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).clickable { vm.selectDish(dish.dishId) }) {
                        Text(dish.nameEn, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            "+${dish.kcal.roundToInt()} kcal · ${dish.proteinG.roundToInt()}g Protein",
                            color = InkMuted,
                            fontSize = 11.sp,
                        )
                    }
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.15f))
                            .clickable { vm.openDishPicker(PickerMode.AddToPlate) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Mint, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                "Or assemble multiple dishes together · Auto-macros",
                color = InkMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(Canvas)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            PrimaryButton(
                "+ Build Custom Combination",
                onClick = {
                    vm.startCustom(slot)
                    onBuildMeal()
                },
                showArrow = false,
                enabled = !state.busy,
            )
        }
    }
}

@Composable
private fun UsualQuickCard(usual: UsualMeal, busy: Boolean, onLog: () -> Unit) {
    val totals = CalorieMath.usualTotals(usual.items)
    val detail = usual.items.joinToString(" · ") { item ->
        val count = if (item.portion == item.portion.toInt().toDouble()) {
            item.portion.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", item.portion)
        }
        "$count ${item.unit} ${item.name}".trim()
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
            Text("  Saved Usual", color = Mint, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            if (usual.pinned) {
                Text("  · Pinned", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text(usual.name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        if (detail.isNotBlank()) {
            Text(detail, color = InkMuted, fontSize = 12.sp)
        }
        Text(
            "~${formatCount(totals.kcal)} kcal · ${totals.proteinG.roundToInt()}g Protein · ${totals.carbsG.roundToInt()}g Carbs",
            color = InkMuted,
            fontSize = 12.sp,
        )
        Text(
            "Low Variability",
            color = Mint,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            modifier = Modifier
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
        Text(
            "1-Tap Log +",
            color = OnMint,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (busy) Mint.copy(alpha = 0.5f) else Mint)
                .clickable(enabled = !busy, onClick = onLog)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun CommonQuickCard(
    card: CommonCard,
    onEditSave: () -> Unit,
    onPin: () -> Unit,
    onLogOnce: () -> Unit,
) {
    val kcal = card.items.sumOf { it.estimateKcal }
    val protein = card.items.sumOf { it.proteinG }.roundToInt()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(card.nameEn, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(
            "~${formatCount(kcal)} kcal · ${protein}g Protein",
            color = InkMuted,
            fontSize = 11.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Edit & Save",
                color = Mint,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onEditSave),
            )
            Text(
                "Add Usual",
                color = Cyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onPin),
            )
            Text(
                "Log Once +",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onLogOnce),
            )
        }
    }
}

@Composable
private fun QuickSearchField(value: String, hint: String, onChange: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = InkMuted)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = Ink, fontSize = 15.sp),
            modifier = Modifier.weight(1f).padding(12.dp),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty()) Text(hint, color = InkMuted, fontSize = 14.sp)
                inner()
            },
        )
        if (value.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Clear",
                tint = InkMuted,
                modifier = Modifier.clickable { onChange("") }.size(18.dp),
            )
        }
    }
}
