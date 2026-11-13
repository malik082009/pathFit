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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.PaceLogic
import com.grittech.pathfit.domain.PreviewLogic
import com.grittech.pathfit.domain.formatCount
import com.grittech.pathfit.ui.components.PrimaryButton
import com.grittech.pathfit.ui.components.PulseMark
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Amber
import com.grittech.pathfit.ui.theme.Canvas
import com.grittech.pathfit.ui.theme.CardRest
import com.grittech.pathfit.ui.theme.Cyan
import com.grittech.pathfit.ui.theme.Ink
import com.grittech.pathfit.ui.theme.InkMuted
import com.grittech.pathfit.ui.theme.Line
import com.grittech.pathfit.ui.theme.Mint
import com.grittech.pathfit.ui.theme.OnMint

@Composable
fun PreviewScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val targets = state.targets
    val preview = targets?.let(PreviewLogic::bindPreview)
    val reach = PaceLogic.reachPhrase(state.draft.weightKg, state.draft.targetWeightKg, state.draft.weeklyPaceKg)
    val showReach = reach != null

    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Ink,
                modifier = Modifier.clickable(onClick = onBack).padding(12.dp),
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulseMark()
                Text("  FitPath", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.weight(1f))
            if (showReach) {
                Box(
                    Modifier
                        .padding(end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Mint)
                        .clickable { /* profile from preview is visual only */ },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = OnMint, modifier = Modifier.size(20.dp))
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(
                "PERSONALIZED PREVIEW",
                color = Mint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(Mint.copy(alpha = 0.10f))
                    .border(1.dp, Mint.copy(alpha = 0.35f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Your Daily Fuel Targets",
                color = Ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Personalized metabolic baseline calibrated for your body and goals.",
                color = InkMuted,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
            Spacer(Modifier.height(16.dp))

            if (preview == null || !preview.active) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardRest)
                        .border(1.dp, Line, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                ) {
                    Text(preview?.blockMessage ?: "Targets are not ready yet.", color = Ink)
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardRest)
                        .border(1.dp, Line, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(formatCount(preview.heroKcal ?: 0), color = Ink, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                        Text("  kcal / day", color = InkMuted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .background(Canvas)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Mint))
                        Text("  ", color = InkMuted)
                        Text(highlightPace(preview.subtitle), fontSize = 12.sp)
                    }
                    if (showReach) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Mint.copy(alpha = 0.08f))
                                .border(1.dp, Mint.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                            Text(
                                highlightReach(reach!!),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(start = 8.dp).weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("BMR FLOOR", preview.bmr, if (showReach) null else "Burned at complete rest", Modifier.weight(1f)) {
                            vm.showBmr(true)
                        }
                        MiniStat("TDEE BURN", preview.tdee, if (showReach) null else "Total daily burn + activity", Modifier.weight(1f)) {
                            vm.showBmr(true)
                        }
                        MiniStat(
                            preview.deltaLabel,
                            preview.delta,
                            if (showReach) null else preview.deltaCaption,
                            Modifier.weight(1f),
                            signed = true,
                        ) { vm.showBmr(true) }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Macro Distribution", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("100% Calculated", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)) {
                    Box(Modifier.weight(preview.proteinPct.coerceAtLeast(1).toFloat()).background(Mint))
                    Box(Modifier.weight(preview.carbsPct.coerceAtLeast(1).toFloat()).background(Cyan))
                    Box(Modifier.weight(preview.fatPct.coerceAtLeast(1).toFloat()).background(Amber))
                }
                Spacer(Modifier.height(12.dp))
                MacroRow(Icons.Filled.FitnessCenter, Mint, "Protein", "Lean preservation", preview.proteinG, preview.proteinPct)
                Spacer(Modifier.height(8.dp))
                MacroRow(Icons.Filled.Bolt, Cyan, "Carbs", "Sustained energy", preview.carbsG, preview.carbsPct)
                Spacer(Modifier.height(8.dp))
                MacroRow(Icons.Filled.WaterDrop, Amber, "Fats", "Hormonal balance", preview.fatG, preview.fatPct)

                Spacer(Modifier.height(16.dp))
                if (showReach) {
                    Text(
                        "✨  Calibrated for your body, activity level, and meal preferences",
                        color = InkMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                        Text("  Clinically verified safe for your metabolic profile", color = InkMuted, fontSize = 12.sp)
                    }
                }
                if (preview.clinicalPending) {
                    Text(
                        "Clinical review can refine these numbers later.",
                        color = InkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                if (showReach) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Next: Pick the meals you regularly eat for quick 1-tap logging",
                        color = InkMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton("Continue to Meal Setup", onClick = { vm.openMealSetup() }, enabled = preview?.active == true)
        }
    }

    if (state.showBmrInfo) {
        AlertDialog(
            onDismissRequest = { vm.showBmr(false) },
            confirmButton = { TextButton(onClick = { vm.showBmr(false) }) { Text("Got it") } },
            title = { Text("Clinical Metric Reference") },
            text = {
                Text(
                    "Basal Metabolic Rate (BMR): The minimum calories your body requires to perform basic physiological functions (breathing, cell production, circulation) at complete rest. FitPath ensures your caloric floor never drops below this safety threshold.",
                )
            },
        )
    }
}

@Composable
private fun highlightPace(subtitle: String) = buildAnnotatedString {
    val regex = Regex("""(\d+(?:\.\d+)?\s*kg/week)""", RegexOption.IGNORE_CASE)
    val match = regex.find(subtitle)
    if (match == null) {
        withStyle(SpanStyle(color = InkMuted)) { append(subtitle) }
        return@buildAnnotatedString
    }
    withStyle(SpanStyle(color = InkMuted)) { append(subtitle.substring(0, match.range.first)) }
    withStyle(SpanStyle(color = Mint, fontWeight = FontWeight.Bold)) { append(match.value) }
    withStyle(SpanStyle(color = InkMuted)) { append(subtitle.substring(match.range.last + 1)) }
}

@Composable
private fun highlightReach(reach: String) = buildAnnotatedString {
    val kg = Regex("""(\d+(?:\.\d+)?\s*kg(?:\s+goal)?)""", RegexOption.IGNORE_CASE)
    val around = Regex("""(around\s+[\w\-]+)""", RegexOption.IGNORE_CASE)
    var index = 0
    val spans = (kg.findAll(reach).toList() + around.findAll(reach).toList()).sortedBy { it.range.first }
    if (spans.isEmpty()) {
        withStyle(SpanStyle(color = InkMuted)) { append(reach) }
        return@buildAnnotatedString
    }
    spans.forEach { match ->
        if (match.range.first > index) {
            withStyle(SpanStyle(color = InkMuted)) { append(reach.substring(index, match.range.first)) }
        }
        withStyle(SpanStyle(color = Mint, fontWeight = FontWeight.Bold)) { append(match.value) }
        index = match.range.last + 1
    }
    if (index < reach.length) withStyle(SpanStyle(color = InkMuted)) { append(reach.substring(index)) }
}

@Composable
private fun MiniStat(
    label: String,
    value: Int?,
    caption: String?,
    modifier: Modifier,
    signed: Boolean = false,
    onInfo: () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Canvas)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = InkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(2.dp))
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = InkMuted,
                modifier = Modifier.size(12.dp).clickable(onClick = onInfo),
            )
        }
        Spacer(Modifier.height(4.dp))
        val shown = when {
            value == null -> "—"
            signed && value > 0 -> "+${formatCount(value)}"
            else -> formatCount(value)
        }
        Text(
            shown,
            color = if (signed && (value ?: 0) < 0) Mint else Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Text("kcal", color = InkMuted, fontSize = 10.sp)
        if (caption != null) {
            Text(caption, color = InkMuted, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

@Composable
private fun MacroRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    caption: String,
    grams: Int?,
    pct: Int,
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
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
            Text(caption, color = InkMuted, fontSize = 12.sp)
        }
        Text("${grams ?: "—"} g", color = tint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(" ($pct%)", color = InkMuted, fontSize = 12.sp)
    }
}
