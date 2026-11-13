package com.grittech.pathfit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.CalorieMath
import com.grittech.pathfit.domain.DayClock
import com.grittech.pathfit.domain.ProfileRules
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.model.HomeTab
import com.grittech.pathfit.model.MealLog
import com.grittech.pathfit.model.PickerMode
import com.grittech.pathfit.ui.components.PrimaryButton
import com.grittech.pathfit.ui.components.PulseMark
import com.grittech.pathfit.ui.components.SecondaryButton
import com.grittech.pathfit.ui.components.findActivity
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Amber
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CanvasDeep
import com.grittech.pathfit.ui.theme.CardRest
import com.grittech.pathfit.ui.theme.Cyan
import com.grittech.pathfit.ui.theme.Ink
import com.grittech.pathfit.ui.theme.InkMuted
import com.grittech.pathfit.ui.theme.Line
import com.grittech.pathfit.ui.theme.Mint
import com.grittech.pathfit.ui.theme.OnMint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(onOpenStaples: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            when (state.homeTab) {
                HomeTab.Today -> TodayBody(onOpenStaples)
                HomeTab.Diary -> DiaryBody()
                HomeTab.Usuals -> UsualsBody(onOpenStaples)
                HomeTab.Profile -> ProfileBody(
                    onReview = { vm.openOnboarding() },
                    onUseDashboard = { vm.useDashboard() },
                    onSignOut = { vm.signOut() },
                    onDelete = { confirmDelete = true },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        if (state.snackbar != null) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(14.dp)).background(CardRest).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(state.snackbar ?: "", color = Ink, modifier = Modifier.weight(1f), fontSize = 13.sp)
                if (state.undoIds.isNotEmpty()) {
                    Text("Undo", color = Mint, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { vm.undoLast() })
                } else {
                    Text("OK", color = Mint, modifier = Modifier.clickable { vm.clearError() })
                }
            }
        }
        BottomNav(state.homeTab) { vm.selectTab(it) }
    }
    if (state.showAdjust) {
        AlertDialog(
            onDismissRequest = { vm.closeAdjust() },
            confirmButton = { TextButton(onClick = { vm.logHero(true) }) { Text("Log adjusted") } },
            dismissButton = { TextButton(onClick = { vm.closeAdjust() }) { Text("Cancel") } },
            title = { Text("Adjust portion") },
            text = {
                Column {
                    state.adjustItems.forEachIndexed { index, item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(item.name.ifBlank { "Item" }, color = Ink, modifier = Modifier.weight(1f))
                            Text("−", color = Mint, modifier = Modifier.clickable { vm.updateAdjustPortion(index, item.portion - 0.5) }.padding(8.dp))
                            Text(String.format(Locale.US, "%.1f", item.portion), color = Ink)
                            Text("+", color = Mint, modifier = Modifier.clickable { vm.updateAdjustPortion(index, item.portion + 0.5) }.padding(8.dp))
                        }
                    }
                }
            },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAccount(activity)
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
            title = { Text("Delete account?") },
            text = { Text("This signs you in again to confirm, then removes the FitPath account. A later sign-in with the same Google account can start a new profile.") },
        )
    }
}

@Composable
private fun TodayBody(onOpenStaples: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val eatenLogs = state.logs.filter { !it.deleted && it.status in setOf("eaten", "partial") }
    val eaten = eatenLogs.sumOf { it.kcal }
    val protein = eatenLogs.sumOf { it.proteinG }
    val carbs = eatenLogs.sumOf { it.carbsG }
    val fat = eatenLogs.sumOf { it.fatG }
    val target = state.targets?.takeIf { it.status == "active" }
    val calorieTarget = target?.calorieTarget
    val remaining = calorieTarget?.let { (it - eaten).coerceAtLeast(0) }
    val pct = if (calorieTarget != null && calorieTarget > 0) ((eaten * 100.0) / calorieTarget).roundToInt().coerceIn(0, 100) else 0
    val hour = DayClock.hour(state.draft.timezone)
    val pill = ProfileRules.rangeStatus(eaten, calorieTarget, hour)
    val usual = vm.heroUsual()
    val slotLogged = state.logs.any { !it.deleted && it.mealSlot == state.selectedSlot && it.status in setOf("eaten", "partial", "skipped") }
    val firstName = ProfileRules.firstName(state.draft.displayName)
    LaunchedEffect(state.selectedSlot, usual?.id) {
        if (usual == null) vm.ensureSlotSuggestions()
    }
    val dayKey = DayClock.resolveKey(state.dayKey, state.draft.timezone)

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PulseMark()
        Text("  FitPath", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Row(
            Modifier
                .clip(CircleShape)
                .background(CardRest)
                .border(1.dp, Line, CircleShape)
                .clickable { vm.shiftDay(1) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Mint, modifier = Modifier.size(15.dp))
            Text("  ${DayClock.headerLabel(dayKey, state.draft.timezone)}", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Mint)
                .clickable { vm.selectTab(HomeTab.Profile) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = OnMint)
        }
    }
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text("Assalam-o-Alaikum, $firstName", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Mint))
                Text("  ${pill.second}", color = InkMuted, fontSize = 12.sp)
            }
        }
        Text("✨", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, end = 6.dp))
        Row(
            Modifier
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f))
                .border(1.dp, Mint.copy(alpha = 0.25f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
            Text("  ${pill.first}", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(Modifier.height(16.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(22.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text("TARGET ENERGY BALANCE", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            if (calorieTarget != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${formatCount(remaining ?: 0)} kcal remaining",
                        color = Mint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(CircleShape).background(Mint.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Text("$pct% of daily goal", color = InkMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        if (calorieTarget == null) {
            Spacer(Modifier.height(8.dp))
            Text(state.targets?.blockReasons?.joinToString().ifNullOrBlank { "Fuel targets are not active yet." }, color = Ink)
        } else {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatCount(eaten), color = Ink, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text(" / ${formatCount(calorieTarget)} kcal", color = InkMuted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(CanvasDeep)) {
                Box(
                    Modifier
                        .fillMaxWidth(pct / 100f)
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Mint, Color(0xFF00E599)))),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MacroCard("Protein", Icons.Filled.FitnessCenter, protein, target.proteinG, Cyan, Modifier.weight(1f))
                MacroCard("Carbs", Icons.Filled.Grain, carbs, target.carbsG, Amber, Modifier.weight(1f))
                MacroCard("Fats", Icons.Filled.WaterDrop, fat, target.fatG, Mint, Modifier.weight(1f))
            }
        }
    }

    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("MEAL PHASE SELECTOR", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
            Text("  ${DayClock.clockLabel(state.draft.timezone)} Window", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Triple("breakfast", "Breakfast", Icons.Filled.CheckCircle),
            Triple("lunch", "Lunch", null),
            Triple("dinner", "Dinner", Icons.Filled.Nightlight),
            Triple("snack", "Snack", Icons.Filled.BakeryDining),
        ).forEach { (slot, label, icon) ->
            val selected = state.selectedSlot == slot
            val done = state.logs.any { !it.deleted && it.mealSlot == slot }
            val window = if (selected) " · ${DayClock.slotWindow(slot)}" else ""
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(if (selected) Color(0xFF12352A) else CardRest)
                    .border(1.dp, if (selected) Mint else Line, CircleShape)
                    .clickable { vm.selectSlot(slot) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    done && !selected -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(15.dp))
                    selected -> Box(Modifier.size(8.dp).clip(CircleShape).background(Mint))
                    icon != null -> Icon(icon, contentDescription = null, tint = InkMuted, modifier = Modifier.size(15.dp))
                }
                Text(" $label$window", color = if (selected) Mint else Ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }

    Spacer(Modifier.height(14.dp))
    if (slotLogged && usual != null) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(16.dp)) {
            Text("Logged · ${usual.name}", color = Ink, fontWeight = FontWeight.SemiBold)
            Text("This slot already has an entry.", color = InkMuted, fontSize = 12.sp)
            val slotIds = state.logs.filter { !it.deleted && it.mealSlot == state.selectedSlot }.map { it.id }
            Text("Undo", color = Mint, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp).clickable { vm.undoLogIds(slotIds) })
        }
    } else if (usual != null) {
        var usualExpanded by remember(usual.id, state.selectedSlot) { mutableStateOf(true) }
        val totals = CalorieMath.usualTotals(usual.items)
        val low = (totals.kcal * 0.9).roundToInt()
        val high = (totals.kcal * 1.1).roundToInt()
        val slotTitle = state.selectedSlot.replaceFirstChar { it.titlecase(Locale.US) }
        val itemLine = usual.items.joinToString(" · ") { item ->
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
                .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = Mint.copy(alpha = 0.12f), spotColor = Mint.copy(alpha = 0.18f))
                .clip(RoundedCornerShape(22.dp))
                .background(CardRest)
                .border(1.dp, Mint.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { usualExpanded = !usualExpanded },
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                Text(" YOUR USUAL", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("  · $slotTitle", color = InkMuted, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.TrendingFlat, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                Text(" Low Spike", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    if (usualExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (usualExpanded) "Collapse" else "Expand",
                    tint = InkMuted,
                    modifier = Modifier.size(20.dp).padding(start = 4.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                usual.name,
                color = Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { usualExpanded = !usualExpanded },
            )
            Text(
                "~${formatCount(low)}–${formatCount(high)} kcal",
                color = Mint,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (!usualExpanded) {
                Text(
                    "Tap to view",
                    color = InkMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp).clickable { usualExpanded = true },
                )
            }
            AnimatedVisibility(
                visible = usualExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Text(
                        "${totals.proteinG.roundToInt()}g Protein · ${totals.carbsG.roundToInt()}g Carbs",
                        color = InkMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    if (itemLine.isNotBlank()) {
                        Text(
                            itemLine,
                            color = InkMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(14.dp, CircleShape, ambientColor = Mint.copy(alpha = 0.35f), spotColor = Mint.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(Mint)
                            .clickable { vm.logHero(false) },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = OnMint)
                        Text("  Log this meal", color = OnMint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniAction(Icons.Filled.Search, "Something else", Modifier.weight(1f)) { vm.openDishPicker(PickerMode.LogOnce) }
                        MiniAction(Icons.Filled.Tune, "Adjust portion", Modifier.weight(1f)) { vm.openAdjust() }
                    }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(16.dp)) {
            Text("Common for this slot", color = Ink, fontWeight = FontWeight.SemiBold)
            Text("No usual yet. These are suggestions, not Your Usual.", color = InkMuted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            state.staples[state.selectedSlot].orEmpty().take(4).forEach { card ->
                val kcal = card.items.sumOf { it.estimateKcal }
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(card.nameEn, color = Ink, fontWeight = FontWeight.Medium)
                    Text("~${formatCount(kcal)} kcal", color = InkMuted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add Usual", color = Mint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { vm.pinCommon(card) })
                        Text("Log once", color = Ink, fontSize = 13.sp, modifier = Modifier.clickable { vm.logCommonOnce(card) })
                    }
                }
            }
            Text("Build my usual", color = Mint, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp).clickable { vm.startCustom(state.selectedSlot) })
        }
    }

    Spacer(Modifier.height(14.dp))
    val glasses = (state.waterMl / 250).coerceAtMost(8)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = Cyan)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text("Daily Hydration", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("$glasses / 8 glasses · ${formatCount(state.waterMl)} ml", color = InkMuted, fontSize = 12.sp)
            }
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Cyan.copy(alpha = 0.14f))
                    .clickable { vm.addGlass() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                Text(" +1 Glass", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            repeat(8) { index ->
                val filled = index < glasses
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (filled) Cyan.copy(alpha = 0.22f) else CanvasDeep)
                            .border(1.dp, if (filled) Cyan.copy(alpha = 0.45f) else Line, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.LocalDrink,
                            contentDescription = null,
                            tint = if (filled) Cyan else InkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text("${index + 1}", color = if (filled) Cyan else InkMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun DiaryBody() {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    Text("Diary / Log", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Text(DayClock.headerLabel(DayClock.resolveKey(state.dayKey, state.draft.timezone), state.draft.timezone), color = InkMuted)
    Spacer(Modifier.height(12.dp))
    val rows = state.logs.filter { !it.deleted }.sortedByDescending { it.loggedAt }
    if (rows.isEmpty()) Text("No meals for this day.", color = InkMuted)
    rows.forEach { log ->
        LogRow(log)
    }
}

@Composable
private fun UsualsBody(onOpenStaples: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    Text("Usuals & Common", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Text("Pinned staples ready for one-tap logging.", color = InkMuted)
    Spacer(Modifier.height(12.dp))
    val meals = state.usuals.filter { it.status == "active" }
    if (meals.isEmpty()) Text("No usuals yet.", color = InkMuted)
    meals.forEach { meal ->
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp)).background(CardRest).padding(14.dp)) {
            Text(meal.name, color = Ink, fontWeight = FontWeight.SemiBold)
            Text(ProfileRules.slotSubtitle(meal.mealSlot), color = InkMuted, fontSize = 12.sp)
            Text("Log", color = Mint, modifier = Modifier.padding(top = 6.dp).clickable { vm.logUsual(meal.id) })
        }
    }
    Spacer(Modifier.height(12.dp))
    PrimaryButton("Pick everyday staples", onClick = onOpenStaples, showArrow = false)
}

@Composable
private fun ProfileBody(
    onReview: () -> Unit,
    onUseDashboard: () -> Unit,
    onSignOut: () -> Unit,
    onDelete: () -> Unit,
) {
    val state = rememberFitViewModel().state.collectAsStateWithLifecycle().value
    Text("Profile & Vitals", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardRest).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(state.draft.displayName.ifBlank { "Friend" }, color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(state.draft.locationLabel, color = InkMuted)
        Text("${state.draft.heightCm} cm · ${state.draft.weightKg} kg → ${state.draft.targetWeightKg} kg", color = Ink)
        Text(ProfileRules.goalLabel(state.draft.goal), color = Mint)
        Text("Activity ${state.draft.activityLevel}", color = InkMuted, fontSize = 13.sp)
        Text("Plan ${state.entitlement}", color = InkMuted, fontSize = 13.sp)
        Text(
            if (state.onboardingStatus == "complete") "Server onboarding: complete" else "Server onboarding: pending",
            color = InkMuted,
            fontSize = 13.sp,
        )
        state.targets?.calorieTarget?.let { Text("Fuel target ${formatCount(it)} kcal", color = Ink) }
    }
    Spacer(Modifier.height(16.dp))
    PrimaryButton("Review onboarding", onClick = onReview, showArrow = false)
    Spacer(Modifier.height(8.dp))
    SecondaryButton("Open Today dashboard", onClick = onUseDashboard)
    Spacer(Modifier.height(8.dp))
    SecondaryButton("Sign out", onSignOut)
    Spacer(Modifier.height(8.dp))
    Text("Delete account", color = Color(0xFFFFB4AB), modifier = Modifier.fillMaxWidth().clickable(onClick = onDelete).padding(vertical = 10.dp), textAlign = TextAlign.Center)
}

@Composable
private fun BottomNav(tab: HomeTab, onSelect: (HomeTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CanvasDeep)
            .border(1.dp, Line)
            .navigationBarsPadding()
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        NavItem(Icons.AutoMirrored.Filled.EventNote, "Today", tab == HomeTab.Today) { onSelect(HomeTab.Today) }
        NavItem(Icons.AutoMirrored.Filled.MenuBook, "Diary / Log", tab == HomeTab.Diary) { onSelect(HomeTab.Diary) }
        NavItem(Icons.Filled.Restaurant, "Usuals & Common", tab == HomeTab.Usuals) { onSelect(HomeTab.Usuals) }
        NavItem(Icons.Filled.HealthAndSafety, "Profile & Vitals", tab == HomeTab.Profile) { onSelect(HomeTab.Profile) }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Mint.copy(alpha = 0.14f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = if (selected) Mint else InkMuted, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (selected) Mint else InkMuted,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp,
        )
    }
}

@Composable
private fun MacroCard(label: String, icon: ImageVector, eaten: Double, target: Int?, tint: Color, modifier: Modifier) {
    val pct = if (target != null && target > 0) ((eaten / target) * 100.0).roundToInt().coerceIn(0, 150) else 0
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(CanvasDeep).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Text("  $label", color = InkMuted, fontSize = 11.sp)
        }
        Text("${eaten.roundToInt()}/${target ?: "—"}g", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Line)) {
            Box(Modifier.fillMaxWidth((pct / 100f).coerceAtMost(1f)).height(4.dp).background(tint))
        }
    }
}

@Composable
private fun MiniAction(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(CircleShape)
            .background(CanvasDeep)
            .border(1.dp, Line, CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
        Text("  $label", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LogRow(log: MealLog) {
    val time = if (log.loggedAt > 0) SimpleDateFormat("h:mm a", Locale.US).format(Date(log.loggedAt)) else ""
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(log.title, color = Ink)
            Text("${log.mealSlot} · $time", color = InkMuted, fontSize = 12.sp)
        }
        Text("~${formatCount(log.kcal)}", color = Ink)
    }
}

private fun String?.ifNullOrBlank(fallback: () -> String): String = if (this.isNullOrBlank()) fallback() else this
