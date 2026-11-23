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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.grittech.pathfit.domain.CalorieMath
import com.grittech.pathfit.domain.DayClock
import com.grittech.pathfit.domain.LogEpisodes
import com.grittech.pathfit.domain.ProfileRules
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.model.CommonCard
import com.grittech.pathfit.model.CustomMealDraft
import com.grittech.pathfit.model.HomeTab
import com.grittech.pathfit.model.MealEpisode
import com.grittech.pathfit.model.PlateItem
import com.grittech.pathfit.model.UsualMeal
import com.grittech.pathfit.ui.components.PrimaryButton
import com.grittech.pathfit.ui.components.PulseMark
import com.grittech.pathfit.ui.components.SecondaryButton
import com.grittech.pathfit.ui.components.findActivity
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Amber
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CanvasDeep
import com.grittech.pathfit.ui.theme.CardHighest
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
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onOpenStaples: () -> Unit,
    onOpenQuickLog: (String) -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    onOpenBuildMeal: () -> Unit = {},
) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            when (state.homeTab) {
                HomeTab.Today -> TodayBody(onOpenQuickLog = onOpenQuickLog)
                HomeTab.Diary -> DiaryBody(onOpenQuickLog = onOpenQuickLog)
                HomeTab.Usuals -> UsualsBody(
                    onOpenStaples = onOpenStaples,
                    onOpenBuildMeal = onOpenBuildMeal,
                )
                HomeTab.Profile -> ProfileBody(
                    onReview = { vm.openOnboarding() },
                    onUseDashboard = { vm.useDashboard() },
                    onOpenStats = onOpenStats,
                    onOpenHabits = onOpenHabits,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayBody(onOpenQuickLog: (String) -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var showDayPicker by remember { mutableStateOf(false) }
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
                .clickable { showDayPicker = true }
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
        if (pill.first != "Ready") {
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
                        MiniAction(Icons.Filled.Search, "Something else", Modifier.weight(1f)) {
                            onOpenQuickLog(state.selectedSlot)
                        }
                        MiniAction(Icons.Filled.Tune, "Adjust portion", Modifier.weight(1f)) { vm.openAdjust() }
                    }
                }
            }
        }
    } else {
        var showMoreCommon by remember(state.selectedSlot) { mutableStateOf(false) }
        val commonCards = state.staples[state.selectedSlot].orEmpty()
        val visibleCommon = if (showMoreCommon) commonCards else commonCards.take(3)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(16.dp)) {
            Text("Common for this slot", color = Ink, fontWeight = FontWeight.SemiBold)
            Text("No usual yet. These are suggestions, not Your Usual.", color = InkMuted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            visibleCommon.forEach { card ->
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
            if (commonCards.size > 3) {
                Text(
                    if (showMoreCommon) "Show less" else "Show more",
                    color = Mint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { showMoreCommon = !showMoreCommon },
                )
            }
            Text("Build my usual", color = Mint, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp).clickable { vm.startCustom(state.selectedSlot) })
        }
    }

    Spacer(Modifier.height(14.dp))
    val glasses = (state.waterMl / 250).coerceIn(0, 8)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = Cyan)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text("Daily Hydration", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("$glasses / 8 glasses · ${formatCount(glasses * 250)} ml", color = InkMuted, fontSize = 12.sp)
            }
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Cyan.copy(alpha = 0.14f))
                    .clickable(enabled = glasses < 8) { vm.setWaterGlasses(glasses + 1) }
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
                val target = if (filled && index == glasses - 1) glasses - 1 else index + 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { vm.setWaterGlasses(target) },
                ) {
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
                            contentDescription = if (filled) "Remove glass ${index + 1}" else "Add glass ${index + 1}",
                            tint = if (filled) Cyan else InkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text("${index + 1}", color = if (filled) Cyan else InkMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }

    val episodes = remember(state.logs) { LogEpisodes.group(state.logs) }
    val preview = episodes.takeLast(2).reversed()
    Spacer(Modifier.height(14.dp))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Today's Log", color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                "View all (${episodes.size})",
                color = Mint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { vm.selectTab(HomeTab.Diary) },
            )
        }
        if (preview.isEmpty()) {
            Text("No meals logged yet.", color = InkMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        } else {
            preview.forEach { episode ->
                val time = if (episode.loggedAt > 0) {
                    SimpleDateFormat("h:mm a", Locale.US).format(Date(episode.loggedAt))
                } else {
                    ""
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(episode.title, color = Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            listOfNotNull(ProfileRules.slotTitle(episode.mealSlot), time.takeIf { it.isNotBlank() }).joinToString(" · "),
                            color = InkMuted,
                            fontSize = 12.sp,
                        )
                    }
                    Text("~${formatCount(episode.kcal)} kcal", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (!state.coachTip.isNullOrBlank()) {
        Spacer(Modifier.height(14.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Mint.copy(alpha = 0.10f))
                .border(1.dp, Mint.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Text("Coach tip", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(state.coachTip.orEmpty(), color = Ink, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }

    if (showDayPicker) {
        val initialMillis = remember(dayKey) {
            runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey)?.time
            }.getOrNull()
        }
        val picker = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDayPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { millis ->
                        val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        vm.setDayKey(key)
                    }
                    showDayPicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDayPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = picker) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryBody(onOpenQuickLog: (String) -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var protocolVisible by remember { mutableStateOf(true) }
    var showDayPicker by remember { mutableStateOf(false) }
    val dayKey = DayClock.resolveKey(state.dayKey, state.draft.timezone)
    val eatenLogs = state.logs.filter { !it.deleted && it.status in setOf("eaten", "partial") }
    val eaten = eatenLogs.sumOf { it.kcal }
    val protein = eatenLogs.sumOf { it.proteinG }
    val carbs = eatenLogs.sumOf { it.carbsG }
    val fat = eatenLogs.sumOf { it.fatG }
    val target = state.targets?.takeIf { it.status == "active" }
    val calorieTarget = target?.calorieTarget
    val remaining = calorieTarget?.let { (it - eaten).coerceAtLeast(0) }
    val pct = if (calorieTarget != null && calorieTarget > 0) {
        ((eaten * 100.0) / calorieTarget).roundToInt().coerceIn(0, 100)
    } else {
        0
    }
    val episodes = remember(state.logs) { LogEpisodes.group(state.logs) }
    val pending = LogEpisodes.pendingSlot(state.logs, state.draft.timezone)
    val glasses = (state.waterMl / 250).coerceIn(0, 8)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Mint))
        Text("  ACTIVE LOGGING", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Metabolic Integrity v2.3",
            color = Mint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
    Spacer(Modifier.height(10.dp))
    Text("Daily Diary & Log", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Text("Continuous glycemic & caloric balance", color = InkMuted, fontSize = 13.sp)
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "◀",
            color = InkMuted,
            fontSize = 16.sp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { vm.shiftDay(-1) }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
        Row(
            Modifier
                .weight(1f)
                .clip(CircleShape)
                .background(CardRest)
                .border(1.dp, Line, CircleShape)
                .clickable { showDayPicker = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Mint, modifier = Modifier.size(15.dp))
            Text(
                "  ${DayClock.headerLabel(dayKey, state.draft.timezone)}",
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(Icons.Filled.ExpandMore, contentDescription = "Pick date", tint = InkMuted, modifier = Modifier.size(18.dp))
        }
        Text(
            "▶",
            color = InkMuted,
            fontSize = 16.sp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { vm.shiftDay(1) }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }

    Spacer(Modifier.height(14.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Text("Metabolic Target Intake", color = InkMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        if (calorieTarget == null) {
            Spacer(Modifier.height(8.dp))
            Text(state.targets?.blockReasons?.joinToString().ifNullOrBlank { "Fuel targets are not active yet." }, color = Ink)
        } else {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(formatCount(eaten), color = Ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(" / ${formatCount(calorieTarget)} kcal", color = InkMuted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 4.dp).weight(1f))
                Text("~${formatCount(remaining ?: 0)} kcal left", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(CanvasDeep)) {
                Box(
                    Modifier
                        .fillMaxWidth(pct / 100f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Mint),
                )
            }
            if (eaten > 0) {
                val low = (eaten * 0.97).roundToInt()
                val high = (eaten * 1.07).roundToInt()
                Text(
                    "Estimate: ~${formatCount(low)}–${formatCount(high)} kcal",
                    color = InkMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MacroCard("Protein", Icons.Filled.FitnessCenter, protein, target.proteinG, Cyan, Modifier.weight(1f))
                MacroCard("Carbs", Icons.Filled.Grain, carbs, target.carbsG, Amber, Modifier.weight(1f))
                MacroCard("Fats", Icons.Filled.WaterDrop, fat, target.fatG, Mint, Modifier.weight(1f))
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Meal Episodes", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(
            "+ Quick Item",
            color = Mint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onOpenQuickLog(state.selectedSlot) },
        )
    }
    Spacer(Modifier.height(8.dp))
    if (episodes.isEmpty()) {
        Text("No meals for this day.", color = InkMuted, fontSize = 13.sp)
    }
    episodes.forEach { episode ->
        DiaryEpisodeCard(episode)
        Spacer(Modifier.height(8.dp))
    }

    if (pending != null) {
        val slotTitle = ProfileRules.slotTitle(pending)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("$slotTitle (Not logged yet)", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Log, pick a usual, or skip to keep the diary complete.", color = InkMuted, fontSize = 12.sp)
            PrimaryButton("+ Log $slotTitle", onClick = { onOpenQuickLog(pending) }, showArrow = false)
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(CanvasDeep)
                    .clickable { vm.selectTab(HomeTab.Usuals) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                Text("  Pick from Usuals", color = Ink, fontWeight = FontWeight.SemiBold)
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable { vm.skipPendingSlot(pending) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = InkMuted, modifier = Modifier.size(16.dp))
                Text("  Skip (Fasting)", color = InkMuted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardRest).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = Cyan)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text("Daily Hydration", color = Ink, fontWeight = FontWeight.SemiBold)
                Text("$glasses / 8 glasses · ${formatCount(state.waterMl)} / 2,000 ml", color = InkMuted, fontSize = 12.sp)
            }
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Cyan.copy(alpha = 0.14f))
                    .clickable(enabled = glasses < 8) { vm.setWaterGlasses(glasses + 1) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                Text(" +1 Glass", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(8) { index ->
                Icon(
                    Icons.Filled.LocalDrink,
                    contentDescription = null,
                    tint = if (index < glasses) Cyan else InkMuted.copy(alpha = 0.35f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }

    if (protocolVisible) {
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CanvasDeep)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
            Text(
                "Clinical Protocol Active — Logs are locked to capture timestamp for clinical metabolic integrity.",
                color = InkMuted,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            Icon(
                Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = InkMuted,
                modifier = Modifier.size(18.dp).clickable { protocolVisible = false },
            )
        }
    }

    if (showDayPicker) {
        val initialMillis = remember(dayKey) {
            runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey)?.time
            }.getOrNull()
        }
        val picker = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDayPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { millis ->
                        val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        vm.setDayKey(key)
                    }
                    showDayPicker = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDayPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = picker) }
    }
}

@Composable
private fun DiaryEpisodeCard(episode: MealEpisode) {
    var expanded by remember(episode.key) { mutableStateOf(false) }
    val time = if (episode.loggedAt > 0) {
        SimpleDateFormat("h:mm a", Locale.US).format(Date(episode.loggedAt))
    } else {
        ""
    }
    val tip = ProfileRules.softTip(episode.kcal, episode.proteinG, episode.carbsG)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    listOfNotNull(time.takeIf { it.isNotBlank() }, ProfileRules.slotTitle(episode.mealSlot)).joinToString("  "),
                    color = InkMuted,
                    fontSize = 11.sp,
                )
                Text(episode.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Text("~${formatCount(episode.kcal)} kcal", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = InkMuted,
            )
        }
        Text(
            tip,
            color = Mint,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 6.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                episode.lines.forEach { line ->
                    Text(
                        "${line.title} · ~${formatCount(line.kcal)} kcal",
                        color = InkMuted,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun UsualsBody(
    onOpenStaples: () -> Unit,
    onOpenBuildMeal: () -> Unit,
) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var slotFilter by remember { mutableStateOf<String?>(null) }
    val pills = listOf<Pair<String?, String>>(
        null to "All",
        "breakfast" to "Breakfast",
        "lunch" to "Lunch",
        "dinner" to "Dinner",
        "snack" to "Snacks",
    )
    val buildSlot = slotFilter ?: state.selectedSlot.ifBlank { "lunch" }
    val usuals = state.usuals
        .filter { it.status == "active" }
        .filter { slotFilter == null || it.mealSlot == slotFilter }
        .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
        .sortedWith(compareByDescending<UsualMeal> { it.pinned }.thenByDescending { it.lastUsedAt })
    val commonCards = remember(state.staples, slotFilter, query) {
        val filter = slotFilter
        val source = if (filter == null) {
            state.staples.values.flatten()
        } else {
            state.staples[filter].orEmpty()
        }
        source.filter { query.isBlank() || it.nameEn.contains(query.trim(), ignoreCase = true) }
    }

    LaunchedEffect(slotFilter, state.selectedSlot) {
        val filter = slotFilter
        if (filter == null) {
            if (state.staples.isEmpty()) vm.loadStaples()
        } else {
            vm.selectSlot(filter)
            vm.ensureSlotSuggestions()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Usuals & Common Hub", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.Info, contentDescription = null, tint = InkMuted)
    }
    Text("Your remembered staples & regional catalog.", color = InkMuted, fontSize = 13.sp)
    Spacer(Modifier.height(12.dp))

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(color = Ink, fontSize = 14.sp),
            cursorBrush = SolidColor(Mint),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            decorationBox = { inner ->
                if (query.isBlank()) {
                    Text("Search dishes & combinations (e.g. anda, paratha, daal)", color = InkMuted, fontSize = 13.sp)
                }
                inner()
            },
        )
    }

    Spacer(Modifier.height(12.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        pills.forEach { (slot, label) ->
            val selected = slotFilter == slot
            Text(
                label,
                color = if (selected) OnMint else InkMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) Mint else CardRest)
                    .border(1.dp, if (selected) Mint else Line, CircleShape)
                    .clickable { slotFilter = slot }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }

    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Your Usuals", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (usuals.isNotEmpty()) {
            Text(
                "${usuals.size} Configured",
                color = Mint,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Mint.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    if (usuals.isEmpty()) {
        Text("No usuals yet for this filter.", color = InkMuted, fontSize = 13.sp)
    }
    usuals.forEach { meal ->
        val totals = CalorieMath.usualTotals(meal.items)
        val low = (totals.kcal * 0.9).roundToInt()
        val high = (totals.kcal * 1.1).roundToInt()
        val tip = ProfileRules.softTip(totals.kcal, totals.proteinG, totals.carbsG)
        val portions = meal.items.joinToString(" · ") { item ->
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
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                MealThumb(meal.mealSlot, meal.name)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(meal.name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(ProfileRules.slotSubtitle(meal.mealSlot), color = InkMuted, fontSize = 12.sp)
                    Text(
                        tip,
                        color = Mint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                    Text(
                        "~${formatCount(low)}–${formatCount(high)} kcal · ${totals.carbsG.roundToInt()}g Carbs",
                        color = InkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Icon(
                    Icons.Filled.PushPin,
                    contentDescription = if (meal.pinned) "Unpin" else "Pin",
                    tint = if (meal.pinned) Mint else InkMuted,
                    modifier = Modifier.size(18.dp).clickable { vm.toggleUsualPin(meal.id) },
                )
            }
            if (portions.isNotBlank()) {
                Text(
                    portions,
                    color = InkMuted,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CanvasDeep)
                        .padding(10.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(Mint)
                        .clickable { vm.logUsual(meal.id) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = OnMint, modifier = Modifier.size(16.dp))
                    Text("  1-Tap Log", color = OnMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(CanvasDeep)
                        .clickable { vm.editUsualBlueprint(meal.id) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                    Text("  Edit Blueprint", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(CircleShape)
                .border(1.dp, Line, CircleShape)
                .clickable {
                    vm.startCustom(buildSlot)
                    onOpenBuildMeal()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.AddCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
            Text("  Build Custom", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(CircleShape)
                .border(1.dp, Line, CircleShape)
                .clickable {
                    if (slotFilter == null) vm.loadStaples() else vm.ensureSlotSuggestions()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
            Text("  Import Common", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }

    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Common Meals Explorer", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(
            "Pakistan / ${state.draft.countryCode.ifBlank { "PK" }}",
            color = Mint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
    Text(
        "Curated combinations · Suggestions only until added to Your Usuals.",
        color = InkMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
    )
    if (commonCards.isEmpty()) {
        Text("No common suggestions loaded yet.", color = InkMuted, fontSize = 13.sp)
    }
    commonCards.forEach { card ->
        CommonExplorerCard(
            card = card,
            onAddUsual = { vm.pinCommon(card) },
            onEditSave = {
                val slot = card.mealSlot.ifBlank { buildSlot }
                vm.selectSlot(slot)
                vm.editCustom {
                    CustomMealDraft(
                        mealSlot = slot,
                        name = card.nameEn,
                        items = card.items.mapIndexed { index, item ->
                            PlateItem(
                                key = "${item.dishId}-$index",
                                dishId = item.dishId,
                                name = item.nameEn,
                                portion = item.defaultPortion,
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
                onOpenBuildMeal()
            },
            onLogOnce = {
                if (card.mealSlot.isNotBlank()) vm.selectSlot(card.mealSlot)
                vm.logCommonOnce(card)
            },
        )
        Spacer(Modifier.height(8.dp))
    }

    Spacer(Modifier.height(8.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Text("Why use \"Usuals\"?", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            "Remembering your frequent household preparations (exact roti sizes, cooking fats, katori counts) allows 1-tap logging without repetitive weighing.",
            color = InkMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    Spacer(Modifier.height(12.dp))
    Text(
        "Pick everyday staples",
        color = Mint,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenStaples).padding(vertical = 8.dp),
    )
}

@Composable
private fun CommonExplorerCard(
    card: CommonCard,
    onAddUsual: () -> Unit,
    onEditSave: () -> Unit,
    onLogOnce: () -> Unit,
) {
    val kcal = card.items.sumOf { it.estimateKcal }
    val protein = card.items.sumOf { it.proteinG }
    val carbs = card.items.sumOf { it.carbsG }
    val tip = ProfileRules.softTip(kcal, protein, carbs)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CanvasDeep)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(card.nameEn, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(
            "~${formatCount(kcal)} kcal · ${ProfileRules.slotTitle(card.mealSlot)}",
            color = InkMuted,
            fontSize = 12.sp,
        )
        Text(
            tip,
            color = Mint,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Add Usual",
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Line, CircleShape)
                    .clickable(onClick = onAddUsual)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
            Text(
                "Edit & Save",
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Line, CircleShape)
                    .clickable(onClick = onEditSave)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
            Text(
                "Log Once",
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, Line, CircleShape)
                    .clickable(onClick = onLogOnce)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ProfileBody(
    onReview: () -> Unit,
    onUseDashboard: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenHabits: () -> Unit,
    onSignOut: () -> Unit,
    onDelete: () -> Unit,
) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val draft = state.draft
    val targets = state.targets
    val email = remember { FirebaseAuth.getInstance().currentUser?.email }.orEmpty()
    val name = draft.displayName.ifBlank { "Friend" }

    val eatenLogs = state.logs.filter { !it.deleted && it.status in setOf("eaten", "partial") }
    val loggedToday = eatenLogs.isNotEmpty()
    val eaten = eatenLogs.sumOf { it.kcal }
    val calorieTarget = targets?.takeIf { it.status == "active" }?.calorieTarget
    val waterMl = state.waterMl

    val weightDelta = draft.targetWeightKg - draft.weightKg
    val progressPct = ((1.0 - abs(weightDelta) / maxOf(abs(weightDelta), 15.0)) * 100.0).roundToInt().coerceIn(0, 100)
    val trajectoryLabel = when {
        weightDelta < -0.2 -> String.format(Locale.US, "%.1f kg deficit trajectory", weightDelta)
        weightDelta > 0.2 -> String.format(Locale.US, "+%.1f kg surplus trajectory", weightDelta)
        else -> "Stable weight target"
    }

    val proteinPct = targets?.proteinPct ?: 30
    val carbsPct = targets?.carbsPct ?: 40
    val fatPct = targets?.fatPct ?: 30
    val deficit = targets?.appliedGoalDelta?.roundToInt()
        ?: run {
            val tdee = targets?.tdee
            val cal = targets?.calorieTarget
            if (tdee != null && cal != null) (cal - tdee).roundToInt() else null
        }

    val stabilityText = when {
        !loggedToday && waterMl <= 0 -> "—"
        else -> {
            val score = (if (loggedToday) 50 else 0) + when {
                waterMl >= 2000 -> 50
                waterMl > 0 -> 25
                else -> 0
            }
            "$score/100"
        }
    }
    val adherenceText = when {
        calorieTarget == null -> "—"
        !loggedToday -> "Not logged"
        eaten <= (calorieTarget * 1.05).roundToInt() -> "Target Met"
        else -> "Over target"
    }

    val entitlement = state.entitlement.lowercase(Locale.US)
    val showTrialBanner = entitlement in setOf("trialing", "none", "")
    val planLabel = when (entitlement) {
        "trialing", "none", "" -> "Free Trial Active"
        "active", "premium", "annual" -> "Membership Active"
        else -> state.entitlement.replaceFirstChar { it.titlecase(Locale.US) }.ifBlank { "Plan" }
    }

    var mealReminders by remember { mutableStateOf(true) }
    var hydrationPrompts by remember { mutableStateOf(true) }
    var showDisclaimer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(18.dp)
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = OnMint,
        checkedTrackColor = Mint,
        uncheckedThumbColor = InkMuted,
        uncheckedTrackColor = CanvasDeep,
    )

    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Mint))
                    Text(
                        "  CLINICAL BIOMETRICS ONLINE",
                        color = Mint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp,
                    )
                }
                Text("Profile & Vitals", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardRest)
                        .clickable { showSettings = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = InkMuted, modifier = Modifier.size(20.dp))
                }
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardRest)
                        .clickable { showSettings = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = InkMuted, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 2. User profile card
        Column(
            Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(CardRest)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp)) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.18f))
                            .border(2.dp, Mint.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Mint, modifier = Modifier.size(32.dp))
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Mint),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = OnMint, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1)
                    if (email.isNotBlank()) {
                        Text(email, color = InkMuted, fontSize = 12.sp, maxLines = 1)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                        Text(
                            "  Metabolic Cohort: Active Monitoring",
                            color = InkMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardHighest)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                    Text("  $planLabel", color = Amber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(
                        state.entitlement.ifBlank { "none" }.replaceFirstChar { it.titlecase(Locale.US) },
                        color = Amber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CanvasDeep)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Text(
                    "Complete access to continuous metabolic predictions and South Asian pairing engines.",
                    color = InkMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
                if (showTrialBanner) {
                    PrimaryButton(
                        label = "Upgrade to Annual Membership",
                        onClick = { },
                        showArrow = true,
                    )
                }
            }
        }

        // 3. Habits & Consistency Ladder
        Row(
            Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(CardRest)
                .border(1.dp, Mint.copy(alpha = 0.2f), cardShape)
                .clickable(onClick = onOpenHabits)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Habits & Consistency Ladder", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Active",
                        color = OnMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Mint)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text("Step 2: Log Daily Meals", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("  ·  ", color = InkMuted, fontSize = 12.sp)
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Amber, modifier = Modifier.size(13.dp))
                    Text(
                        if (loggedToday) "  Logged today" else "  Build your streak",
                        color = Amber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
        }

        // 4. Metabolic Insights & Statistics
        Column(
            Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(CardRest)
                .border(1.dp, Mint.copy(alpha = 0.2f), cardShape)
                .clickable(onClick = onOpenStats)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Mint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Analytics, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Metabolic Insights & Statistics", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "Postprandial curves, adherence & fat breakdown",
                        color = InkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, Mint.copy(alpha = 0.1f), RoundedCornerShape(0.dp))
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CanvasDeep)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.ShowChart, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text("Stability", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text(stabilityText, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CanvasDeep)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text("Adherence", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text(adherenceText, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 5. Clinical Biometrics & Targets
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ShowChart, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                Text("  Clinical Biometrics & Targets", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(targets?.method ?: "Mifflin-St Jeor v1", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(CardRest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("CURRENT WEIGHT", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                            Text(String.format(Locale.US, "%.1f", draft.weightKg), color = Ink, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                            Text("  kg", color = InkMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TARGET GOAL", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                            Text(String.format(Locale.US, "%.1f", draft.targetWeightKg), color = Mint, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("  kg", color = InkMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CanvasDeep)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (weightDelta <= 0) Icons.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingFlat,
                            contentDescription = null,
                            tint = Mint,
                            modifier = Modifier.size(16.dp),
                        )
                        Text("  $trajectoryLabel", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(
                            "Gentle Pacing",
                            color = Mint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Mint.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(CardHighest)) {
                        Box(
                            Modifier
                                .fillMaxWidth((progressPct / 100f).coerceIn(0.05f, 1f))
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Mint),
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            String.format(Locale.US, "%.1f kg", draft.weightKg),
                            color = InkMuted,
                            fontSize = 10.sp,
                        )
                        Text("Progress: $progressPct% of target", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text(
                            String.format(Locale.US, "%.1f kg", draft.targetWeightKg),
                            color = InkMuted,
                            fontSize = 10.sp,
                        )
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CanvasDeep)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("TARGET CALORIC ENVELOPE", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(
                            targets?.calorieTarget?.let { "${formatCount(it)} kcal/day" } ?: "—",
                            color = Cyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProfileMetricCell("BMR", targets?.bmr?.roundToInt()?.let { "${formatCount(it)} kcal" } ?: "—", Modifier.weight(1f))
                        ProfileMetricCell("TDEE", targets?.tdee?.roundToInt()?.let { "${formatCount(it)} kcal" } ?: "—", Modifier.weight(1f))
                        ProfileMetricCell(
                            if ((deficit ?: 0) > 0) "Surplus" else "Deficit",
                            deficit?.let { "${formatCount(abs(it))} kcal" } ?: "—",
                            Modifier.weight(1f),
                            accent = true,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Macro Blueprint", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Target Balanced Ratios", color = InkMuted, fontSize = 11.sp)
                    }
                    Row(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)) {
                        Box(Modifier.weight(proteinPct.coerceAtLeast(1).toFloat()).background(Mint))
                        Spacer(Modifier.width(2.dp))
                        Box(Modifier.weight(carbsPct.coerceAtLeast(1).toFloat()).background(Cyan))
                        Spacer(Modifier.width(2.dp))
                        Box(Modifier.weight(fatPct.coerceAtLeast(1).toFloat()).background(Amber))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProfileMacroCell("Protein", targets?.proteinG, proteinPct, Mint, Modifier.weight(1f))
                        ProfileMacroCell("Low-GI Carbs", targets?.carbsG, carbsPct, Cyan, Modifier.weight(1f))
                        ProfileMacroCell("Healthy Lipids", targets?.fatG, fatPct, Amber, Modifier.weight(1f))
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(CardHighest)
                        .clickable(onClick = onReview),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    Text("  Recalibrate Biometrics & Targets", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 6. SafetyEngine™ Medical Guardrails
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
                Text("  SafetyEngine™ Medical Guardrails", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Shield, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(CardRest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CanvasDeep)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Active Clinical Policy", color = InkMuted, fontSize = 10.sp)
                        Text("Policy 2026-10-21.v2", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Mint))
                        Text("  Verified Baseline", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text("ACTIVE GUARDRAIL CONDITIONS", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CanvasDeep)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (draft.conditionKeys.isEmpty()) {
                        ProfileGuardrailRow(
                            tint = Cyan,
                            title = "South Asian Glycemic Protocol",
                            body = "Calculates postprandial glucose curves with cultural carb pairing (dals, chapatis, biryani buffers).",
                        )
                    } else {
                        draft.conditionKeys.forEach { key ->
                            ProfileGuardrailRow(
                                tint = Amber,
                                title = prettyConditionLabel(key),
                                body = "Accounted for in SafetyEngine recommendations and meal pairing.",
                            )
                        }
                    }
                }

                val exclusionChips = buildList {
                    draft.allergenFlags.forEach { flag ->
                        allergenChipLabel(flag)?.let { add(it) }
                    }
                    draft.dietTypes.forEach { type ->
                        dietChipLabel(type)?.let { add(it to true) }
                    }
                }
                if (exclusionChips.isNotEmpty()) {
                    Text("ACTIVE FOOD EXCLUSIONS & CERTIFICATIONS", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        exclusionChips.forEach { (label, positive) ->
                            Row(
                                Modifier
                                    .clip(CircleShape)
                                    .background(CardHighest)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    if (positive) Icons.Filled.CheckCircle else Icons.Filled.Block,
                                    contentDescription = null,
                                    tint = Mint,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text("  $label", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CloudDone, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                    Text("  Cache Verified", color = InkMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Server Sync ready", color = InkMuted, fontSize = 10.sp)
                }
            }
        }

        // 7. Smart Logging Prompts
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Alarm, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                Text("  Smart Logging Prompts", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    "Active",
                    color = Ink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CardHighest)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(CardRest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Meal Window Reminders", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "Breakfast 08:30 · Lunch 14:30 · Dinner 21:00",
                            color = InkMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Switch(checked = mealReminders, onCheckedChange = { mealReminders = it }, colors = switchColors)
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hydration Micro-Prompts", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Row(
                            Modifier.padding(top = 4.dp).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf("10:00", "13:00", "16:00", "19:00").forEach { time ->
                                Text(
                                    time,
                                    color = Ink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CanvasDeep)
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                    Switch(checked = hydrationPrompts, onCheckedChange = { hydrationPrompts = it }, colors = switchColors)
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CanvasDeep)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Bedtime, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clinical Quiet Hours", color = InkMuted, fontSize = 10.sp)
                        Text(
                            "22:00 to 07:00 (${draft.timezone.ifBlank { "Asia/Karachi" }})",
                            color = Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        // 8. Settings destinations
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
                Text("  Clinical Tools & Settings", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(CardRest),
            ) {
                ProfileNavRow(
                    icon = Icons.Filled.LocalFireDepartment,
                    tint = Amber,
                    title = "Habits Ladder & Journey",
                    subtitle = "Consistency ladder, streaks & rituals",
                    onClick = onOpenHabits,
                )
                Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(CardHighest))
                ProfileNavRow(
                    icon = Icons.Filled.Analytics,
                    tint = Mint,
                    title = "Metabolic Insights & Stats",
                    subtitle = "Clinical statistics & adherence rollups",
                    onClick = onOpenStats,
                )
            }
        }

        // 9. Data & Privacy
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
                Text("  Data & Privacy Controls", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(CardRest),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { vm.exportHealthData() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Export Medical & Diary Data", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("JSON / Clinical export", color = InkMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
                }

                Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(CardHighest))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showDisclaimer = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clinical Disclaimer & Terms", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("HIPAA/GDPR Compliance, v4.1 Disclosures", color = InkMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
                }

                Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(CardHighest))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CanvasDeep)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Danger Zone", color = Color(0xFFFFB4AB), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Permanently wipe biomarkers & diary records", color = InkMuted, fontSize = 12.sp)
                    }
                    Text(
                        "Delete",
                        color = Color(0xFFFFDAD6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF93000A))
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }

            Text(
                "Sign out",
                color = InkMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSignOut)
                    .padding(vertical = 8.dp),
            )
            Text(
                "Open Today",
                color = Mint,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUseDashboard)
                    .padding(bottom = 4.dp),
            )
        }
    }

    if (showDisclaimer) {
        AlertDialog(
            onDismissRequest = { showDisclaimer = false },
            confirmButton = { TextButton(onClick = { showDisclaimer = false }) { Text("Got it") } },
            title = { Text("Clinical Disclaimer") },
            text = {
                Text(
                    "FitPath provides informational metabolic guidance and is not a substitute for professional medical advice, diagnosis, or treatment. Always consult a qualified clinician before changing diet, medication, or activity plans.",
                )
            },
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Close") }
            },
            title = { Text("Clinical Tools & Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileNavRow(
                        icon = Icons.Filled.LocalFireDepartment,
                        tint = Amber,
                        title = "Habits Ladder & Journey",
                        subtitle = "Open consistency ladder",
                        onClick = {
                            showSettings = false
                            onOpenHabits()
                        },
                    )
                    ProfileNavRow(
                        icon = Icons.Filled.Analytics,
                        tint = Mint,
                        title = "Metabolic Insights & Stats",
                        subtitle = "Open clinical statistics",
                        onClick = {
                            showSettings = false
                            onOpenStats()
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, color = InkMuted, fontSize = 12.sp)
        }
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
    }
}

@Composable
private fun ProfileMetricCell(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardHighest)
            .padding(8.dp),
    ) {
        Text(label, color = if (accent) Mint else InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = if (accent) Mint else Ink, fontSize = 12.sp, fontWeight = if (accent) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileMacroCell(label: String, grams: Int?, pct: Int, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CanvasDeep)
            .padding(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(tint))
            Text("  $label", color = InkMuted, fontSize = 10.sp, maxLines = 1)
        }
        Text(
            "${grams?.let { "${it}g" } ?: "—"} ($pct%)",
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ProfileGuardrailRow(tint: Color, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(body, color = InkMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

private fun prettyConditionLabel(key: String): String =
    key.split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }

private fun allergenChipLabel(flag: String): Pair<String, Boolean>? = when (flag.lowercase(Locale.US)) {
    "gluten" -> "Gluten-Free" to true
    "seafood" -> "No Shellfish" to false
    "lactose" -> "Lactose-Free" to true
    "nuts" -> "Nut-Free" to true
    "eggs" -> "Egg-Free" to true
    "soy" -> "Soy-Free" to true
    "none", "" -> null
    else -> flag.replaceFirstChar { it.titlecase(Locale.US) } to true
}

private fun dietChipLabel(type: String): String? = when (type.lowercase(Locale.US)) {
    "halal" -> "Halal Certified"
    "vegetarian" -> "Vegetarian"
    "vegan" -> "Vegan"
    "non-vegetarian", "omnivore", "all" -> null
    else -> type.replaceFirstChar { it.titlecase(Locale.US) }
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
private fun MealThumb(mealSlot: String, name: String) {
    val icon = when (mealSlot) {
        "breakfast" -> Icons.Filled.BakeryDining
        "lunch" -> Icons.Filled.Restaurant
        "dinner" -> Icons.Filled.Nightlight
        "snack" -> Icons.Filled.Grain
        else -> Icons.Filled.Restaurant
    }
    val tint = when (mealSlot) {
        "breakfast" -> Amber
        "lunch" -> Mint
        "dinner" -> Cyan
        else -> InkMuted
    }
    Box(
        Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.16f))
            .border(1.dp, tint.copy(alpha = 0.28f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(
                name.take(1).uppercase(Locale.US),
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
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

private fun String?.ifNullOrBlank(fallback: () -> String): String = if (this.isNullOrBlank()) fallback() else this
