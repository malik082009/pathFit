package com.grittech.pathfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.DayClock
import com.grittech.pathfit.domain.StatsLogic
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.model.HomeTab
import com.grittech.pathfit.ui.components.PrimaryButton
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
import java.util.Locale
import kotlin.math.roundToInt

private enum class StatsHorizonTab(val label: String) {
    D7("7 Days"),
    D14("14 Days"),
    D30("30 Days"),
    Month("Month"),
}

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var horizon by remember { mutableStateOf(StatsHorizonTab.D7) }

    val tz = state.draft.timezone
    val dayKey = DayClock.resolveKey(state.dayKey, tz)
    val dayKeys = remember(horizon, dayKey, tz) {
        when (horizon) {
            StatsHorizonTab.D7 -> DayClock.dayKeysInclusive(dayKey, 7, tz)
            StatsHorizonTab.D14 -> DayClock.dayKeysInclusive(dayKey, 14, tz)
            StatsHorizonTab.D30 -> DayClock.dayKeysInclusive(dayKey, 30, tz)
            StatsHorizonTab.Month -> DayClock.monthKeysToDate(dayKey, tz)
        }
    }
    val waterByDay = remember(dayKey, state.waterMl) {
        mapOf(dayKey to state.waterMl)
    }
    val logs = remember(state.logs, dayKeys, dayKey) {
        if (dayKey in dayKeys) state.logs else emptyList()
    }
    val rollup = remember(dayKeys, logs, waterByDay, state.targets?.calorieTarget) {
        StatsLogic.rollup(
            dayKeys = dayKeys,
            logs = logs,
            waterByDay = waterByDay,
            calorieTarget = state.targets?.calorieTarget,
            oilLevels = emptyList(),
        )
    }
    val target = state.targets?.calorieTarget
    val rangeLabel = remember(dayKeys) {
        if (dayKeys.isEmpty()) "—"
        else {
            val fmt = SimpleDateFormat("MMM d", Locale.US)
            val parse = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val start = runCatching { parse.parse(dayKeys.first()) }.getOrNull()
            val end = runCatching { parse.parse(dayKeys.last()) }.getOrNull()
            val startTxt = start?.let { fmt.format(it) } ?: dayKeys.first()
            val endTxt = end?.let { fmt.format(it) } ?: dayKeys.last()
            "RANGE: $startTxt – $endTxt (${dayKeys.size} Days)"
        }
    }
    val goal = state.targets?.goalEffective ?: state.targets?.goalRequested ?: state.draft.goal
    val pacingText = remember(rollup.netDeficit, rollup.daysLogged, goal) {
        val net = rollup.netDeficit
        val logged = rollup.daysLogged
        if (goal == "lose" && net != null && logged > 0) {
            val kgPerWeek = (net * 7.0 / logged) / 7700.0
            String.format(Locale.US, "≈ %+.2f kg / week", kgPerWeek)
        } else {
            null
        }
    }
    val avgGlasses = if (dayKeys.isNotEmpty()) {
        (rollup.days.sumOf { it.waterMl }.toDouble() / dayKeys.size) / 250.0
    } else {
        0.0
    }
    val avgMl = if (dayKeys.isNotEmpty()) {
        rollup.days.sumOf { it.waterMl }.toDouble() / dayKeys.size
    } else {
        0.0
    }
    val hydrationStrip = dayKeys.takeLast(7)
    val streak = remember(dayKeys, waterByDay, dayKey) {
        var n = 0
        for (key in dayKeys.asReversed()) {
            val ml = waterByDay[key] ?: 0
            if (ml >= 250) n++ else break
        }
        n
    }
    val country = state.draft.countryCode.uppercase(Locale.US)
    val showSatiety = country in setOf("PK", "IN", "BD")
    val maxBar = (rollup.days.maxOfOrNull { it.kcal } ?: 0).coerceAtLeast(target ?: 1).coerceAtLeast(1)

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
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Metabolic Insights…",
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "CLINICAL STATISTICS",
                        color = Mint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CardRest)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }
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
            Text(rangeLabel, color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "On-device from your logs · export only when you ask",
                color = InkMuted,
                fontSize = 12.sp,
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatsHorizonTab.entries.forEach { tab ->
                    val selected = horizon == tab
                    Text(
                        tab.label,
                        color = if (selected) OnMint else InkMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (selected) Mint else CardRest)
                            .border(1.dp, if (selected) Mint else Line, CircleShape)
                            .clickable { horizon = tab }
                            .padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            StatsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ShowChart, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    Text(
                        "  Metabolic Stability Index",
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.14f))
                            .border(2.dp, Mint, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(rollup.stabilityLetter, color = Mint, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "${rollup.stabilityScore} / 100",
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                        Text(
                            "${rollup.daysMet}/${rollup.dayKeys.size} Target Met · ${rollup.daysLogged} logged",
                            color = InkMuted,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            StatsCard {
                Text("Caloric Energy & Adherence", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                if (target != null && target > 0) {
                    Text(
                        "Mifflin-St Jeor Target · ${formatCount(target)} kcal",
                        color = InkMuted,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    "${rollup.daysMet}/${rollup.dayKeys.size} Target Met",
                    color = Mint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().height(96.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    rollup.days.forEach { day ->
                        val over = target != null && target > 0 && day.kcal > (target * 1.10).roundToInt()
                        val h = if (day.logged) ((day.kcal.toFloat() / maxBar) * 88f).coerceIn(4f, 88f) else 4f
                        Box(
                            Modifier
                                .weight(1f)
                                .height(h.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    when {
                                        !day.logged -> CanvasDeep
                                        over -> Amber
                                        else -> Mint
                                    },
                                ),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Mean", color = InkMuted, fontSize = 11.sp)
                        Text(
                            rollup.meanKcal?.let { "${formatCount(it)} kcal" } ?: "—",
                            color = Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Net Deficit", color = InkMuted, fontSize = 11.sp)
                        Text(
                            rollup.netDeficit?.let {
                                val sign = if (it > 0) "+" else ""
                                "$sign${formatCount(it)} kcal"
                            } ?: "—",
                            color = if ((rollup.netDeficit ?: 0) >= 0) Mint else Amber,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                    if (pacingText != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pacing", color = InkMuted, fontSize = 11.sp)
                            Text(pacingText, color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            StatsCard {
                Text("Nutritional Architecture", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                MacroRow("Lean Protein", rollup.meanProtein, state.targets?.proteinG, Cyan)
                Spacer(Modifier.height(8.dp))
                MacroRow("Complex Low-GI Carbs", rollup.meanCarbs, state.targets?.carbsG, Amber)
                Spacer(Modifier.height(8.dp))
                MacroRow("Essential Lipids", rollup.meanFat, state.targets?.fatG, Mint)
                if (showSatiety) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "SOUTH ASIAN SATIETY FACTOR",
                        color = Mint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Mint.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }

            StatsCard {
                Text("Hydration Consistency", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = Cyan)
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(
                            String.format(Locale.US, "%.1f glasses (%,.0f ml)", avgGlasses, avgMl),
                            color = Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                        Text(
                            "${rollup.waterDaysMet} days ≥ 2 L · Streak $streak Days",
                            color = InkMuted,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    hydrationStrip.forEach { key ->
                        val filled = (waterByDay[key] ?: 0) >= 250
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (filled) Cyan.copy(alpha = 0.22f) else CanvasDeep)
                                .border(1.dp, if (filled) Cyan.copy(alpha = 0.45f) else Line, RoundedCornerShape(8.dp)),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Log Daily Meals · ${rollup.daysLogged} of ${rollup.dayKeys.size} days met",
                    color = InkMuted,
                    fontSize = 12.sp,
                )
            }

            val tip = state.coachTip
            if (!tip.isNullOrBlank()) {
                StatsCard {
                    Text("Clinical note", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(tip, color = InkMuted, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            PrimaryButton(
                if (state.busy) "Exporting…" else "Export Health Telemetry Report",
                onClick = { vm.exportHealthData() },
                enabled = !state.busy,
                showArrow = false,
            )
            Text(
                "JSON export via userExportData · PDF / CSV deferred",
                color = InkMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun StatsCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        content()
    }
}

@Composable
private fun MacroRow(label: String, mean: Double?, targetG: Int?, color: androidx.compose.ui.graphics.Color) {
    val meanTxt = mean?.roundToInt()?.toString() ?: "—"
    val targetTxt = targetG?.toString() ?: "—"
    val pct = if (mean != null && targetG != null && targetG > 0) {
        ((mean / targetG) * 100.0).roundToInt().coerceIn(0, 150)
    } else {
        0
    }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = InkMuted, fontSize = 12.sp)
            Text("${meanTxt}g / ${targetTxt}g", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(CanvasDeep),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(pct / 150f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
