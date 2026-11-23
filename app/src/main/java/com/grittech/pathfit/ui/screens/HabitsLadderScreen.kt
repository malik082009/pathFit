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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.DayClock
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.model.HomeTab
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Amber
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CanvasDeep
import com.grittech.pathfit.ui.theme.CardHighest
import com.grittech.pathfit.ui.theme.CardRest
import com.grittech.pathfit.ui.theme.Cyan
import com.grittech.pathfit.ui.theme.Ink
import com.grittech.pathfit.ui.theme.InkMuted
import com.grittech.pathfit.ui.theme.Mint
import com.grittech.pathfit.ui.theme.OnMint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
private enum class HabitStepStatus { Done, Active, Locked }

@Composable
fun HabitsLadderScreen(
    onBack: () -> Unit,
    onLogMeal: () -> Unit = {},
) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val tz = state.draft.timezone
    val dayKey = DayClock.resolveKey(state.dayKey, tz)
    val weekKeys = remember(dayKey, tz) { DayClock.dayKeysInclusive(dayKey, 7, tz) }

    val mealDays = remember(state.logs, weekKeys) {
        weekKeys.associateWith { key ->
            state.logs.any { !it.deleted && it.dayKey == key && it.status in setOf("eaten", "partial") }
        }
    }
    val todayLogged = mealDays[dayKey] == true
    val waterToday = state.waterMl
    val waterMetToday = waterToday >= 1000

    // Honest progress from available day data (water history is current-day only).
    val waterDaysMet = if (waterMetToday) 1 else 0
    val mealDaysMet = mealDays.values.count { it }.coerceAtMost(3)
    val mealGoalDays = 3
    val mealDone = mealDaysMet >= mealGoalDays
    val level = when {
        mealDone -> 3
        mealDaysMet > 0 || waterMetToday -> 2
        else -> 1
    }
    val ladderPct = when {
        mealDone -> 60
        mealDaysMet >= 2 -> 40
        mealDaysMet >= 1 || waterMetToday -> 28
        else -> 12
    }
    val streak = remember(mealDays, weekKeys) {
        var n = 0
        for (key in weekKeys.asReversed()) {
            if (mealDays[key] == true || (key == dayKey && waterMetToday)) n++ else break
        }
        n
    }
    val weekWaterTotal = waterToday // only today known
    val dailyAvg = waterToday
    val weekdayLabels = remember(weekKeys, tz) {
        weekKeys.map { key ->
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = runCatching { fmt.parse(key) }.getOrNull()
            val cal = Calendar.getInstance()
            if (date != null) cal.time = date
            val short = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                else -> "S"
            }
            Triple(key, short, mealDays[key] == true || (key == dayKey && waterMetToday))
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
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardHighest)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Habits & Journey", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("FITPATH PROTOCOL", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            }
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(CardRest)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Mint))
                Text("  CGM LIVE", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(36.dp)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Mint.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                    Text("  LEVEL $level UNLOCKED", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                }
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Amber.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                    Text(
                        if (streak > 0) "  $streak-Day Streak" else "  Start a streak",
                        color = Amber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text("Habits Ladder & Journey", color = Ink, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp)
            Text(
                "One foundational ritual at a time. Unlock daily resilience backed by clinical cadence.",
                color = InkMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardHighest)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CURRENT TIER MOMENTUM", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    Text("$ladderPct% Total Ladder Completed", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(CanvasDeep)) {
                    Box(
                        Modifier
                            .fillMaxWidth((ladderPct / 100f).coerceIn(0.05f, 1f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Mint),
                    )
                }
            }

            // Weekly consistency
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardRest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Weekly Consistency", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("7-day adherence & hydration rollup", color = InkMuted, fontSize = 12.sp)
                    }
                    Text(
                        "Safe Cadence",
                        color = Mint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekdayLabels.forEach { (key, label, met) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            met -> Mint.copy(alpha = 0.18f)
                                            key == dayKey -> Amber.copy(alpha = 0.2f)
                                            else -> CanvasDeep
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                when {
                                    met -> Icon(Icons.Filled.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                                    key == dayKey -> Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp))
                                    else -> Box(Modifier.size(8.dp).clip(CircleShape).background(InkMuted.copy(alpha = 0.35f)))
                                }
                            }
                        }
                    }
                }

                Text("Hydration Rollup", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatCount(weekWaterTotal)} ML Logged today",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(CanvasDeep)) {
                    Box(
                        Modifier
                            .fillMaxWidth((waterToday / 2000f).coerceIn(0f, 1f).coerceAtLeast(if (waterToday > 0) 0.04f else 0f))
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Cyan),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Daily", color = InkMuted, fontSize = 10.sp)
                        Text("${formatCount(dailyAvg)} ML / DAY", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        if (waterToday >= 2000) "On target" else if (waterToday > 0) "Building habit" else "Log water on Today",
                        color = Mint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (todayLogged || waterMetToday) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (streak >= 1) {
                            MilestoneChip("Streak active", Icons.Filled.LocalFireDepartment, Amber)
                        }
                        if (todayLogged) {
                            MilestoneChip("Meal logged", Icons.Filled.Restaurant, Mint)
                        }
                    }
                }
            }

            Text("5-Step Habit Ladder", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            val step1Done = waterDaysMet >= 3
            HabitStepCard(
                step = 1,
                title = "Drink Water Foundation",
                status = if (step1Done) HabitStepStatus.Done else HabitStepStatus.Active,
                icon = Icons.Filled.WaterDrop,
                body = "Drink 1,000ml+ water daily for 3 days to unlock the next ritual.",
                progressLabel = if (step1Done) {
                    "3 of 3 days met (100%) · Mastered"
                } else {
                    "$waterDaysMet of 3 days met · ${if (waterMetToday) "Today counted" else "Today pending"}"
                },
                progressPct = ((waterDaysMet * 100f) / 3f).coerceIn(0f, 100f),
                treatAsDone = step1Done,
            )

            HabitStepCard(
                step = 2,
                title = "Log Daily Meals",
                status = when {
                    mealDone -> HabitStepStatus.Done
                    else -> HabitStepStatus.Active
                },
                icon = Icons.Filled.Restaurant,
                body = "Log at least 1 eaten or partial meal daily for 3 days to build consistency.",
                progressLabel = "$mealDaysMet of $mealGoalDays days met · ${((mealDaysMet * 100) / mealGoalDays)}% Complete",
                progressPct = ((mealDaysMet * 100f) / mealGoalDays).coerceIn(0f, 100f),
                forceDoneVisual = false,
                treatAsDone = mealDone,
                pendingLabel = if (!todayLogged) "Today's meal pending" else null,
                ctaLabel = if (!todayLogged) "+ Log Meal Now" else null,
                onCta = onLogMeal,
            )

            HabitStepCard(
                step = 3,
                title = "Move Daily",
                status = HabitStepStatus.Locked,
                icon = Icons.Filled.FitnessCenter,
                body = "Complete 3 days with ≥1 activity session. Requires Step 2.",
                progressLabel = null,
                progressPct = 0f,
                lockedNote = "Step 2 required",
            )

            HabitStepCard(
                step = 4,
                title = "Sleep Routine",
                status = HabitStepStatus.Locked,
                icon = Icons.Filled.Nightlight,
                body = "Check in sleep for 3 consecutive days to stabilize recovery signals.",
                progressLabel = null,
                progressPct = 0f,
                lockedNote = "Step 3 required",
            )

            HabitStepCard(
                step = 5,
                title = "Mind & Symptom Check-In",
                status = HabitStepStatus.Locked,
                icon = Icons.Filled.EmojiEmotions,
                body = "Record mood or symptoms for 3 days to complete this metabolic tier.",
                progressLabel = null,
                progressPct = 0f,
                lockedNote = "Step 4 required",
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardRest)
                    .clickable(onClick = onLogMeal)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Mint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("FitPath Clinical Coach", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            !todayLogged -> "You're 1 logged meal away from stepping up today's ladder."
                            mealDaysMet < 3 -> "Nice log — ${3 - mealDaysMet} more day${if (3 - mealDaysMet == 1) "" else "s"} to master Step 2."
                            else -> "Ladder momentum looks strong. Keep the cadence."
                        },
                        color = Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MilestoneChip(label: String, icon: ImageVector, tint: Color) {
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardHighest)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text("  $label", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HabitStepCard(
    step: Int,
    title: String,
    status: HabitStepStatus,
    icon: ImageVector,
    body: String,
    progressLabel: String?,
    progressPct: Float,
    forceDoneVisual: Boolean = false,
    treatAsDone: Boolean = false,
    pendingLabel: String? = null,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
    lockedNote: String? = null,
) {
    val done = status == HabitStepStatus.Done || treatAsDone || forceDoneVisual
    val locked = status == HabitStepStatus.Locked
    val active = status == HabitStepStatus.Active && !done
    val accent = when {
        done -> Mint
        active -> Mint
        else -> InkMuted
    }
    val cardAlpha = if (locked) 0.55f else 1f

    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done -> Mint
                            active -> Mint.copy(alpha = 0.18f)
                            else -> CardHighest
                        },
                    )
                    .border(1.dp, if (active) Mint.copy(alpha = 0.4f) else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when {
                        done -> Icons.Filled.CheckCircle
                        locked -> Icons.Filled.Lock
                        else -> icon
                    },
                    contentDescription = null,
                    tint = when {
                        done -> OnMint
                        active -> Mint
                        else -> InkMuted
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
            if (step < 5) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(if (done) Mint.copy(alpha = 0.45f) else CardHighest),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest.copy(alpha = cardAlpha))
                .border(
                    1.dp,
                    when {
                        active -> Mint.copy(alpha = 0.28f)
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(16.dp),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Step $step: $title", color = if (locked) InkMuted else Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                when {
                    done -> Text(
                        "Done",
                        color = OnMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Mint)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                    active -> Text(
                        "Active",
                        color = OnMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Mint)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                    lockedNote != null -> Text(lockedNote, color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(body, color = InkMuted, fontSize = 12.sp, lineHeight = 16.sp)
            if (progressLabel != null) {
                Text(progressLabel, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(CanvasDeep)) {
                    Box(
                        Modifier
                            .fillMaxWidth((progressPct / 100f).coerceIn(0.02f, 1f))
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                }
            }
            if (pendingLabel != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Amber))
                    Text("  $pendingLabel", color = Amber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (ctaLabel != null && onCta != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(Mint)
                        .clickable(onClick = onCta),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(ctaLabel, color = OnMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
