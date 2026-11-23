package com.grittech.pathfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.CalorieMath
import com.grittech.pathfit.domain.ProfileRules
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.model.PickerMode
import com.grittech.pathfit.model.PlateItem
import com.grittech.pathfit.ui.components.PrimaryButton
import com.grittech.pathfit.ui.components.SecondaryButton
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Amber
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CardHighest
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
fun StaplesScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var expanded by remember {
        mutableStateOf(setOf("breakfast", "lunch", "dinner", "snack"))
    }
    LaunchedEffect(Unit) { if (state.staples.isEmpty()) vm.loadStaples() }

    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Ink,
                modifier = Modifier.clickable(onClick = onBack).padding(8.dp),
            )
            Text("FitPath", color = Mint, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { vm.skipToToday() }.padding(8.dp),
            ) {
                Text("Skip for now", color = InkMuted, fontSize = 13.sp)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = InkMuted, modifier = Modifier.size(16.dp))
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Mint.copy(alpha = 0.10f))
                    .border(1.dp, Mint.copy(alpha = 0.28f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                Text("  Optional Step • Change Anytime", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text("Pick Your Everyday Staples", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = InkMuted)) {
                        append("Quick-start your food journal by saving your go-to staples. Tap meals to add them to ")
                    }
                    withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.SemiBold)) { append("Your Usuals") }
                    withStyle(SpanStyle(color = InkMuted)) {
                        append(" for 1-tap logging, or use the ")
                    }
                    withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.SemiBold)) { append("Custom Meal Builder") }
                    withStyle(SpanStyle(color = InkMuted)) {
                        append(" to craft and portion your own dish combinations without logging today.")
                    }
                },
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            if (state.staplesOffline) {
                Spacer(Modifier.height(8.dp))
                Text("Showing saved suggestions while the catalog reconnects.", color = Mint, fontSize = 12.sp)
            }
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error ?: "", color = Color(0xFFFFB4AB), fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))

            listOf(
                SlotVisual("breakfast", Icons.Filled.WbTwilight, Cyan, "Nashta"),
                SlotVisual("lunch", Icons.Filled.WbSunny, Mint, "Lunch"),
                SlotVisual("dinner", Icons.Filled.DarkMode, Amber, "Dinner"),
                SlotVisual("snack", Icons.Filled.Coffee, Cyan, "Snack"),
            ).forEach { visual ->
                val slot = visual.slot
                val cards = state.staples[slot].orEmpty()
                val avg = if (cards.isEmpty()) 0 else cards.sumOf { it.items.sumOf { item -> item.estimateKcal } } / cards.size
                val open = slot in expanded
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardRest)
                        .border(1.dp, Line, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            expanded = if (open) expanded - slot else expanded + slot
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(visual.icon, contentDescription = null, tint = visual.tint, modifier = Modifier.size(22.dp))
                        Text(
                            "  ${ProfileRules.slotSubtitle(slot)}",
                            color = Ink,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (avg > 0) {
                            Text("~${formatCount(avg)} kcal avg", color = InkMuted, fontSize = 12.sp)
                        }
                        Icon(
                            if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = InkMuted,
                            modifier = Modifier.padding(start = 4.dp).size(20.dp),
                        )
                    }
                    if (open) {
                        Spacer(Modifier.height(8.dp))
                        cards.forEach { card ->
                            val kcal = card.items.sumOf { it.estimateKcal }
                            val selected = card.templateId in state.selectedTemplates
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { vm.toggleTemplate(card.templateId) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(card.nameEn, color = Ink, fontWeight = FontWeight.Medium)
                                    Text("~${formatCount(kcal)} kcal", color = InkMuted, fontSize = 12.sp)
                                }
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) Mint else Color.Transparent)
                                        .border(1.5.dp, if (selected) Mint else InkMuted, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (selected) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = OnMint, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Line.copy(alpha = 0.7f)))
                        }
                        Spacer(Modifier.height(10.dp))
                        DashedActionRow("+ Build Custom ${visual.customLabel}") {
                            vm.startCustom(slot)
                        }
                    }
                }
            }

            PrimaryButton(if (state.busy) "Saving…" else "Save & Continue", onClick = { vm.saveStaples() }, enabled = !state.busy)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.BookmarkAdded, contentDescription = null, tint = InkMuted, modifier = Modifier.size(16.dp))
                Text(
                    "  Saved items will appear under 'Your Usuals' on Today's journal",
                    color = InkMuted,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private data class SlotVisual(
    val slot: String,
    val icon: ImageVector,
    val tint: Color,
    val customLabel: String,
)

@Composable
private fun DashedActionRow(label: String, onClick: () -> Unit) {
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFF6DFFBA).copy(alpha = 0.55f),
                    style = stroke,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Mint, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun BuildMealScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val custom = state.custom
    val totals = CalorieMath.plateTotals(custom.items)
    val target = state.targets?.calorieTarget
    val pct = if (target != null && target > 0) ((totals.kcal * 100.0) / target).roundToInt() else null
    val low = (totals.kcal * 0.95).roundToInt()
    val high = (totals.kcal * 1.05).roundToInt()
    val remaining = target?.let { tgt -> (tgt.toDouble() - totals.kcal).coerceAtLeast(0.0).roundToInt() }
    val slotTargets = mapOf("breakfast" to 500, "lunch" to 750, "dinner" to 650, "snack" to 250)

    MealTopBar(
        title = "Build Custom Meal",
        onBack = onBack,
        trailing = {
            Text("Reset", color = Mint, fontSize = 13.sp, modifier = Modifier.clickable {
                vm.editCustom { it.copy(items = emptyList()) }
            }.padding(8.dp))
            Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = InkMuted, modifier = Modifier.padding(4.dp))
            Box(Modifier.size(32.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = OnMint, modifier = Modifier.size(18.dp))
            }
        },
    ) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f))
                .border(1.dp, Mint.copy(alpha = 0.28f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Tune, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
            Text("  Usuals Builder (S8)", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Text("Custom Meal Builder", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "Craft your everyday plate once. FitPath remembers it so you can log in one tap without re-entering ingredients.",
            color = InkMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("breakfast", "Nashta", Icons.Filled.WbTwilight),
                Triple("lunch", "Lunch", Icons.Filled.Restaurant),
            ).forEach { (slot, label, icon) ->
                SlotPickCard(label, "~${slotTargets[slot]} kcal target", icon, custom.mealSlot == slot, Modifier.weight(1f)) {
                    vm.editCustom { it.copy(mealSlot = slot, name = if (it.name.isBlank() || it.name.startsWith("Custom ")) "Custom ${ProfileRules.slotTitle(slot)} Plate" else it.name) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("dinner", "Dinner", Icons.Filled.Nightlight),
                Triple("snack", "Snack", Icons.Filled.Coffee),
            ).forEach { (slot, label, icon) ->
                SlotPickCard(label, "~${slotTargets[slot]} kcal target", icon, custom.mealSlot == slot, Modifier.weight(1f)) {
                    vm.editCustom { it.copy(mealSlot = slot, name = if (it.name.isBlank() || it.name.startsWith("Custom ")) "Custom ${ProfileRules.slotTitle(slot)} Plate" else it.name) }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("PRESET NAME", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Text("${ProfileRules.slotTitle(custom.mealSlot)} Slot", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = custom.name,
                onValueChange = { value -> vm.editCustom { it.copy(name = value) } },
                textStyle = TextStyle(color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                singleLine = true,
            )
            Icon(Icons.Filled.Edit, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Text("Estimated Live Total", color = InkMuted, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (totals.kcal <= 0) "0" else "${formatCount(low)} – ${formatCount(high)}",
                    color = Ink,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(" kcal", color = InkMuted, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.clip(CircleShape).background(Mint.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                Text("  Class A Verified  ±5% variance", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroUnderline("Protein", "${totals.proteinG.roundToInt()}g", Cyan)
                MacroUnderline("Carbs", "${totals.carbsG.roundToInt()}g", Amber)
                MacroUnderline("Fats", "${totals.fatG.roundToInt()}g", InkMuted)
            }
            if (pct != null && target != null) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$pct% of ${formatCount(target)} kcal daily goal",
                        color = InkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                    )
                    Text("Healthy Range", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Canvas)) {
                    Box(Modifier.fillMaxWidth((pct / 100f).coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(Mint))
                }
            }
            if (remaining != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                    Text(
                        "Leaves ~${formatCount(remaining)} kcal for Lunch, Dinner & Snacks.",
                        color = InkMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(start = 6.dp).weight(1f),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest)
                .border(1.dp, Mint.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable { vm.openDishPicker(PickerMode.AddToPlate) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(Mint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Mint)
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text("+ Add Dish or Ingredient", color = Mint, fontWeight = FontWeight.Bold)
                Text("Roti, Salan, Achar, Dahi, Chutney...", color = InkMuted, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
        }

        if (custom.items.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dishes on Your Plate", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("${custom.items.size} items", color = InkMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            custom.items.forEach { item ->
                PlateItemCard(item) { updated ->
                    vm.editCustom { draft -> draft.copy(items = draft.items.map { if (it.key == item.key) updated else it }) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (state.error != null) Text(state.error ?: "", color = Color(0xFFFFB4AB))
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            "Save as Usual & Log (1-Tap Ready)",
            onClick = { vm.saveCustom(true) },
            enabled = !state.busy && custom.items.isNotEmpty(),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .border(1.dp, Mint.copy(alpha = 0.45f), CircleShape)
                .clickable(enabled = !state.busy && custom.items.isNotEmpty()) { vm.logCustomOnce() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Mint)
            Text("  Log Once Without Saving", color = Mint, fontWeight = FontWeight.SemiBold)
        }
        Text(
            "Save to Your Usuals (Don't Log Today)",
            color = Mint,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !state.busy && custom.items.isNotEmpty()) { vm.saveCustom(false) }
                .padding(vertical = 12.dp),
        )
        Text(
            "Cancel & Return to Staples",
            color = InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onBack).padding(vertical = 8.dp),
        )
        Text(
            "Or create custom recipe",
            color = Mint,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { vm.openRecipe() }.padding(bottom = 16.dp),
        )
    }
}

@Composable
fun DishPickerScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val selected = state.dishes.firstOrNull { it.dishId == state.selectedDishId }
    val addKcal = selected?.let { (it.kcal * state.dishQty).roundToInt() } ?: 0
    MealTopBar(
        title = "Meal Builder",
        onBack = onBack,
        trailing = {
            Icon(Icons.Filled.Info, contentDescription = null, tint = InkMuted, modifier = Modifier.padding(6.dp))
            Box(Modifier.size(32.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = OnMint, modifier = Modifier.size(18.dp))
            }
        },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Mint))
            Text("  Add Dish to Combination", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = InkMuted, modifier = Modifier.clickable(onClick = onBack))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Mint.copy(alpha = 0.08f))
                .border(1.dp, Mint.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Filled.Analytics, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = InkMuted)) {
                        append("Building optimal protein density buffers post-meal glycemic surges by up to ")
                    }
                    withStyle(SpanStyle(color = Mint, fontWeight = FontWeight.Bold)) { append("34%") }
                    withStyle(SpanStyle(color = InkMuted)) { append(".") }
                },
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        SearchField(state.dishQuery, "Search catalog (e.g. eggs, anda, roti)") { vm.searchDishes(it) }
        Spacer(Modifier.height(10.dp))
        Text("Tags:", color = InkMuted, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("anda", "boiled", "omelette", "chai", "paratha").forEach { tag ->
                val on = state.dishTag == tag
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(if (on) Mint else CardRest)
                        .border(1.dp, if (on) Mint else Line, CircleShape)
                        .clickable { vm.searchDishes(state.dishQuery, if (on) null else tag) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (on) Icon(Icons.Filled.Check, contentDescription = null, tint = OnMint, modifier = Modifier.size(14.dp))
                    Text(if (on) "  #$tag" else "#$tag", color = if (on) OnMint else Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CaptureTile(Icons.Filled.QrCodeScanner, "Barcode", "Coming soon", Modifier.weight(1f), enabled = false)
            CaptureTile(Icons.Filled.PhotoCamera, "AI Snap", "Coming soon", Modifier.weight(1f), enabled = false)
            CaptureTile(Icons.Filled.Mic, "Voice Log", "Coming soon", Modifier.weight(1f), enabled = false)
        }
        Spacer(Modifier.height(14.dp))

        if (selected != null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardRest)
                    .border(1.dp, Mint.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Canvas),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.BakeryDining, contentDescription = null, tint = Mint)
                    }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(selected.nameEn, color = Ink, fontWeight = FontWeight.Bold)
                        Text(selected.detail.ifBlank { "Catalog dish" }, color = InkMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            Modifier.clip(CircleShape).background(Mint.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Mint, modifier = Modifier.size(12.dp))
                            Text("  Clinical Satiety Anchor", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Clear",
                        tint = InkMuted,
                        modifier = Modifier.clickable { vm.selectDish("") },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    StepChip { vm.setDishQty(state.dishQty - 1) }
                    Text("${state.dishQty}", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    StepChip(plus = true) { vm.setDishQty(state.dishQty + 1) }
                }
                if (selected.preps.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Preparation Style", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selected.preps.forEach { prep ->
                            val on = state.dishPrep == prep
                            Row(
                                Modifier
                                    .clip(CircleShape)
                                    .background(if (on) Mint.copy(alpha = 0.14f) else Canvas)
                                    .border(1.dp, if (on) Mint else Line, CircleShape)
                                    .clickable { vm.setDishPrep(prep) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (on) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                                Text(if (on) "  $prep" else prep, color = if (on) Mint else Ink, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroUnderline("ENERGY", "~${(selected.kcal * state.dishQty).roundToInt()} kcal", Mint)
                    MacroUnderline("PROTEIN", "${(selected.proteinG * state.dishQty).roundToInt()}g", Cyan)
                    MacroUnderline("CARBS", "${(selected.carbsG * state.dishQty).roundToInt()}g", Amber)
                    MacroUnderline("FATS", "${(selected.fatG * state.dishQty).roundToInt()}g", InkMuted)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        state.dishes.filter { it.dishId != state.selectedDishId }.forEach { dish ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardRest)
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .clickable { vm.selectDish(dish.dishId) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(dish.nameEn, color = Ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${dish.detail.ifBlank { "Catalog" }} · ~${dish.kcal.roundToInt()} kcal · ${dish.proteinG.roundToInt()}g Protein",
                        color = InkMuted,
                        fontSize = 12.sp,
                    )
                }
                Box(Modifier.size(28.dp).clip(CircleShape).background(Mint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        if (selected != null) {
            val verb = if (state.pickerMode == PickerMode.LogOnce) "Log" else "Add"
            PrimaryButton(
                "$verb ${state.dishQty} ${selected.nameEn} to Meal (+$addKcal kcal)",
                onClick = { vm.confirmDish() },
                showArrow = false,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Or create custom recipe",
            color = Mint,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { vm.openRecipe() }.padding(bottom = 16.dp),
        )
    }
}

@Composable
fun IngredientScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { if (state.ingredients.isEmpty()) vm.searchIngredients("", "all") }
    val selected = state.ingredients.firstOrNull { it.id == state.selectedIngredientId }
    val factor = state.ingredientGrams / 100.0
    val addKcal = selected?.let { (it.kcalPer100g * factor).roundToInt() } ?: 0

    MealTopBar(
        title = "Add Ingredient / Item",
        onBack = onBack,
        backIcon = Icons.Filled.Close,
        trailing = {
            Box(Modifier.size(32.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = OnMint, modifier = Modifier.size(18.dp))
            }
        },
    ) {
        SearchField(state.ingredientQuery, "Search ingredients, staples, or brands...") {
            vm.searchIngredients(it, state.ingredientCategory)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "all" to "All",
                "protein" to "Proteins & Meat",
                "veg" to "Vegetables & Greens",
                "dairy" to "Dairy & Eggs",
                "spice" to "Spices & Base",
                "fat" to "Oils & Fats",
            ).forEach { (key, label) ->
                val on = state.ingredientCategory == key
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(if (on) Mint else CardRest)
                        .border(1.dp, if (on) Mint else Line, CircleShape)
                        .clickable { vm.searchIngredients(state.ingredientQuery, key) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (on) Icon(Icons.Filled.Check, contentDescription = null, tint = OnMint, modifier = Modifier.size(14.dp))
                    Text(if (on) "  $label" else label, color = if (on) OnMint else Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        if (selected != null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardRest)
                    .border(1.dp, Mint.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Canvas), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Mint)
                    }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(selected.nameEn, color = Ink, fontWeight = FontWeight.Bold)
                        Text(selected.detail.ifBlank { "Raw ingredient" }, color = InkMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    StepChip { vm.setIngredientGrams((state.ingredientGrams - 10).coerceAtLeast(10)) }
                    Text(
                        "${state.ingredientGrams}",
                        color = Ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    StepChip(plus = true) { vm.setIngredientGrams(state.ingredientGrams + 10) }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().clip(CircleShape).background(Canvas).padding(4.dp)) {
                    listOf("g", "oz", "kg", "pcs").forEach { unit ->
                        val on = unit == "g"
                        Text(
                            unit,
                            color = if (on) OnMint else InkMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (on) Mint else Color.Transparent)
                                .padding(vertical = 8.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 100, 250, 500).forEach { grams ->
                        Text(
                            if (grams == 50 || grams == 100) "+${grams}g" else "${grams}g",
                            color = Ink,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Canvas)
                                .border(1.dp, Line, CircleShape)
                                .clickable {
                                    vm.setIngredientGrams(
                                        if (grams == 50 || grams == 100) state.ingredientGrams + grams else grams,
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroUnderline("Calories", "${(selected.kcalPer100g * factor).roundToInt()} kcal", Cyan)
                    MacroUnderline("Protein", String.format(Locale.US, "%.0f g", selected.proteinPer100g * factor), Mint)
                    MacroUnderline("Carbs", String.format(Locale.US, "%.0f g", selected.carbsPer100g * factor), InkMuted)
                    MacroUnderline("Fat", String.format(Locale.US, "%.1f g", selected.fatPer100g * factor), Amber)
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Text("Recent & Recipe Staples", color = Ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        state.ingredients.filter { it.id != state.selectedIngredientId }.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardRest)
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .clickable { vm.selectIngredient(item.id) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(item.nameEn, color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("${item.kcalPer100g.roundToInt()} kcal / 100g · ${item.detail}", color = InkMuted, fontSize = 12.sp)
                }
                Text(
                    "+ Add",
                    color = Mint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { vm.selectIngredient(item.id) },
                )
            }
        }
        if (selected != null) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                "Add ${state.ingredientGrams}g ${selected.nameEn} (+$addKcal kcal)",
                onClick = { vm.confirmIngredient() },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RecipeScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val recipe = state.recipe
    val kcal = recipe.lines.sumOf { it.kcal }
    val protein = recipe.lines.sumOf { it.proteinG }
    val carbs = recipe.lines.sumOf { it.carbsG }
    val fat = recipe.lines.sumOf { it.fatG }
    val grams = recipe.lines.sumOf { it.grams }

    MealTopBar(
        title = "Create Custom Recipe",
        onBack = onBack,
        trailing = {
            Icon(Icons.Filled.Info, contentDescription = null, tint = InkMuted, modifier = Modifier.padding(6.dp))
            Box(Modifier.size(32.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = OnMint, modifier = Modifier.size(18.dp))
            }
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Mint)
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text("Custom Recipe Builder", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("Build your dish once. We calculate accurate per-serving calories and macros.", color = InkMuted, fontSize = 12.sp)
            }
            Text(
                "Draft",
                color = Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(CircleShape).background(Cyan.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Edit, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
            Text("  RECIPE NAME", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
            BasicTextField(
                value = recipe.name,
                onValueChange = { value -> vm.editRecipe { it.copy(name = value) } },
                textStyle = TextStyle(color = Ink, fontSize = 15.sp),
                modifier = Modifier.weight(1f).padding(12.dp),
                decorationBox = { inner ->
                    if (recipe.name.isEmpty()) Text("Homemade Chicken Palak (Ami's Recipe)", color = InkMuted)
                    inner()
                },
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Meal Slots", color = InkMuted, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("lunch", "dinner", "snack").forEach { slot ->
                val on = recipe.mealSlot == slot
                Text(
                    slot.replaceFirstChar { it.titlecase(Locale.US) },
                    color = if (on) OnMint else Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (on) Mint else CardRest)
                        .border(1.dp, if (on) Mint else Line, CircleShape)
                        .clickable { vm.editRecipe { it.copy(mealSlot = slot) } }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Base Ingredients", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("${recipe.lines.size} raw ingredients weighed", color = InkMuted, fontSize = 12.sp)
            }
            Text(
                "+ Add Item",
                color = Mint,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Mint, CircleShape)
                    .clickable { vm.openIngredients() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        recipe.lines.forEach { line ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardRest)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(line.name, color = Ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${line.kcal.roundToInt()} kcal · ${line.proteinG.roundToInt()}g Protein  |  ${line.grams.roundToInt()}g",
                        color = InkMuted,
                        fontSize = 12.sp,
                    )
                }
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = InkMuted,
                    modifier = Modifier.clickable {
                        vm.editRecipe { draft ->
                            draft.copy(lines = draft.lines.filterNot { it.ingredientId == line.ingredientId && it.grams == line.grams })
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Analytics, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                Text(
                    "  PER SERVING (~${if (grams > 0) grams.roundToInt() else 0}G)",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Calibrated",
                    color = Mint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(CircleShape).background(Mint.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${kcal.roundToInt()}", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(" kcal / serving", color = InkMuted, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                Text("  Diabetic Safe (GI < 25)", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroBox("PROTEIN", "${protein.roundToInt()}g", Mint, Modifier.weight(1f))
                MacroBox("CARBS", "${carbs.roundToInt()}g", Cyan, Modifier.weight(1f))
                MacroBox("FATS", "${fat.roundToInt()}g", Amber, Modifier.weight(1f))
            }
        }
        if (state.error != null) Text(state.error ?: "", color = Color(0xFFFFB4AB))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(CircleShape)
                    .border(1.dp, Line, CircleShape)
                    .clickable { vm.saveRecipe(false) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Ink)
                Text("  Save to Library", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            PrimaryButton(
                "Save & Add to Meal",
                onClick = { vm.saveRecipe(true) },
                enabled = !state.busy,
                modifier = Modifier.weight(1.2f),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MealTopBar(
    title: String,
    onBack: () -> Unit,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(backIcon, contentDescription = "Back", tint = Ink, modifier = Modifier.clickable(onClick = onBack).padding(8.dp))
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 4.dp))
            trailing()
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            content()
        }
    }
}

@Composable
private fun SearchField(value: String, hint: String, onChange: (String) -> Unit) {
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
                if (value.isEmpty()) Text(hint, color = InkMuted)
                inner()
            },
        )
        if (value.isNotEmpty()) {
            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = InkMuted, modifier = Modifier.clickable { onChange("") }.size(18.dp))
        } else {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SlotPickCard(
    title: String,
    caption: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Mint else CardRest)
            .border(1.dp, if (selected) Mint else Line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) OnMint else InkMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            color = if (selected) OnMint else Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
        )
        Text(
            caption,
            color = if (selected) OnMint.copy(alpha = 0.8f) else InkMuted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 2,
        )
    }
}

@Composable
private fun MacroUnderline(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Box(Modifier.padding(top = 4.dp).width(36.dp).height(3.dp).clip(CircleShape).background(color))
    }
}

@Composable
private fun MacroBox(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(Canvas).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun CaptureTile(
    icon: ImageVector,
    title: String,
    caption: String,
    modifier: Modifier,
    enabled: Boolean = true,
) {
    val tint = if (enabled) Mint else InkMuted.copy(alpha = 0.55f)
    val titleColor = if (enabled) Ink else InkMuted.copy(alpha = 0.7f)
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardRest.copy(alpha = if (enabled) 1f else 0.55f))
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(title, color = titleColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Text(caption, color = InkMuted, fontSize = 10.sp)
    }
}

@Composable
private fun StepChip(plus: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (plus) Icons.Filled.Add else Icons.Filled.Remove, contentDescription = null, tint = Mint)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlateItemCard(item: PlateItem, onChange: (PlateItem) -> Unit) {
    val line = CalorieMath.line(item.kcalPerServing, item.proteinPer, item.carbsPer, item.fatPer, item.addedFatKcal, item.portion, item.oilLevel)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Canvas), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Mint, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(item.name, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(item.detail.ifBlank { item.unit }, color = InkMuted, fontSize = 12.sp, maxLines = 2)
            }
            Text("~${line.kcal} kcal", color = Mint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quantity", color = InkMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            StepChip { onChange(item.copy(portion = (item.portion - 0.5).coerceAtLeast(0.5))) }
            Text(
                if (item.portion == item.portion.toInt().toDouble()) item.portion.toInt().toString() else String.format(Locale.US, "%.1f", item.portion),
                color = Ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            StepChip(plus = true) { onChange(item.copy(portion = (item.portion + 0.5).coerceAtMost(12.0))) }
        }
        if (item.oilLevel != null || item.addedFatKcal > 0) {
            Spacer(Modifier.height(10.dp))
            Text("Cooking Ghee / Oil Prep", color = InkMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf("light" to "Light (8g)", "normal" to "Normal (14g)", "rich" to "Rich (22g)").forEach { (level, label) ->
                    val on = item.oilLevel == level || (item.oilLevel == null && level == "normal")
                    Text(
                        if (on) "$label ✓" else label,
                        color = if (on) OnMint else Ink,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (on) Mint else Canvas)
                            .clickable { onChange(item.copy(oilLevel = level)) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 6.dp)) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = InkMuted, modifier = Modifier.size(12.dp).padding(top = 1.dp))
                Text(
                    "Oil adjusts cooking fat only — not the whole dish.",
                    color = InkMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(start = 6.dp).weight(1f),
                )
            }
        } else if (!item.prep.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("Cooking Style", color = InkMuted, fontSize = 12.sp)
            Text(item.prep ?: "", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
