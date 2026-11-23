package com.grittech.pathfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grittech.pathfit.domain.PaceLogic
import com.grittech.pathfit.domain.PlaceResolver
import com.grittech.pathfit.domain.ProfileRules
import com.grittech.pathfit.model.RegionOption
import com.grittech.pathfit.ui.components.PrimaryButton
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.theme.Canvas
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
fun AboutScreen(onNext: () -> Unit) {
    val vm = rememberFitViewModel()
    val draft = vm.state.collectAsStateWithLifecycle().value.draft
    val context = LocalContext.current
    var pickPlace by remember { mutableStateOf(false) }
    var placeQuery by remember { mutableStateOf("") }
    var browseCountry by remember { mutableStateOf(draft.countryCode.ifBlank { "PK" }) }
    var browseRegion by remember { mutableStateOf(draft.regionKey) }
    var locationHint by remember { mutableStateOf<String?>(null) }
    fun applyPlace(option: RegionOption) {
        vm.editDraft {
            it.copy(
                locationLabel = option.label,
                countryCode = option.countryCode,
                regionKey = option.regionKey,
            )
        }
        browseCountry = option.countryCode
        browseRegion = option.regionKey
        pickPlace = false
        locationHint = null
    }
    fun useCurrentLocation() {
        val resolved = resolveDevicePlace(context)
        if (resolved != null) {
            applyPlace(resolved)
        } else {
            locationHint = "Location unavailable. Choose a country, region, and city below."
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) useCurrentLocation()
        else locationHint = "Permission denied. Choose a country, region, and city below."
    }
    OnboardScaffold(
        footer = {
            PrimaryButton(
                "Continue to Biometrics",
                onClick = {
                    vm.markStep("biometrics")
                    onNext()
                },
                enabled = draft.displayName.isNotBlank() && draft.locationLabel.isNotBlank() && draft.countryCode.isNotBlank(),
            )
        },
    ) {
        StepHeading("STEP 1 OF 5 • ABOUT YOU", "About You", "Personalize your profile and calibrate your regional food catalog.", 0.2f)
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("What should we call you?", color = InkMuted, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                Text("  Required", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = InkMuted, modifier = Modifier.size(20.dp))
            BasicTextField(
                value = draft.displayName,
                onValueChange = { value -> vm.editDraft { it.copy(displayName = value) } },
                textStyle = TextStyle(color = Ink, fontSize = 16.sp),
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 14.dp),
                decorationBox = { inner ->
                    if (draft.displayName.isEmpty()) Text("Your name", color = InkMuted)
                    inner()
                },
            )
            if (draft.displayName.isNotBlank()) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Where are you based?", color = InkMuted, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .clickable {
                    placeQuery = ""
                    pickPlace = true
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Mint, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    draft.locationLabel.ifBlank { "Search city or area" },
                    color = if (draft.locationLabel.isBlank()) InkMuted else Ink,
                    fontSize = 16.sp,
                )
                if (draft.regionKey != null) {
                    Text(draft.regionKey!!, color = InkMuted, fontSize = 11.sp)
                }
            }
            if (draft.locationLabel.isNotBlank()) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardRest.copy(alpha = 0.7f))
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Verified, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
            Text(
                "Culturally calibrated food catalog with authentic regional baselines",
                color = InkMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 10.dp),
                lineHeight = 18.sp,
            )
        }
    }
    if (pickPlace) {
        val hits = if (placeQuery.isNotBlank()) {
            PlaceResolver.search(placeQuery)
        } else {
            browseRegion?.let { PlaceResolver.citiesFor(it) }.orEmpty()
        }
        val regions = PlaceResolver.regionsFor(browseCountry)
        AlertDialog(
            onDismissRequest = { pickPlace = false },
            confirmButton = {
                TextButton(onClick = {
                    val typed = placeQuery.trim()
                    if (typed.isNotBlank()) applyPlace(PlaceResolver.resolve(typed))
                    else pickPlace = false
                }) { Text("Use search") }
            },
            dismissButton = {
                TextButton(onClick = { pickPlace = false }) { Text("Cancel") }
            },
            title = { Text("Where are you based?") },
            text = {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Mint.copy(alpha = 0.12f))
                            .clickable {
                                val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (fine || coarse) useCurrentLocation()
                                else permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                        Text("  Use current location", color = Mint, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    if (locationHint != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(locationHint!!, color = InkMuted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    BasicTextField(
                        value = placeQuery,
                        onValueChange = { placeQuery = it },
                        textStyle = TextStyle(color = Ink, fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardRest)
                            .border(1.dp, Line, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        decorationBox = { inner ->
                            if (placeQuery.isEmpty()) Text("Search city…", color = InkMuted)
                            inner()
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Country", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlaceResolver.countries().forEach { country ->
                            val selected = country.code == browseCountry
                            Text(
                                country.name,
                                color = if (selected) OnMint else Ink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selected) Mint else CardRest)
                                    .border(1.dp, if (selected) Mint else Line, CircleShape)
                                    .clickable {
                                        browseCountry = country.code
                                        browseRegion = PlaceResolver.regionsFor(country.code).firstOrNull()?.key
                                        placeQuery = ""
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Region", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        regions.forEach { region ->
                            val selected = region.key == browseRegion
                            Text(
                                region.name,
                                color = if (selected) OnMint else Ink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selected) Mint else CardRest)
                                    .border(1.dp, if (selected) Mint else Line, CircleShape)
                                    .clickable {
                                        browseRegion = region.key
                                        placeQuery = ""
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("City", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Column(Modifier.height(180.dp).verticalScroll(rememberScrollState())) {
                        hits.forEach { region ->
                            Text(
                                region.label,
                                color = if (region.label == draft.locationLabel) Mint else Ink,
                                modifier = Modifier.fillMaxWidth().clickable { applyPlace(region) }.padding(vertical = 12.dp),
                            )
                        }
                        if (hits.isEmpty()) {
                            Text(
                                if (placeQuery.isNotBlank()) "No match. Tap Use search to resolve."
                                else "Select a region to load cities.",
                                color = InkMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricsScreen(onNext: () -> Unit, onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val draft = vm.state.collectAsStateWithLifecycle().value.draft
    var showDate by remember { mutableStateOf(false) }
    val age = ProfileRules.ageYears(draft.dateOfBirth)
    OnboardScaffold(
        footer = {
            PrimaryButton("Continue to Activity & Goals", onClick = {
                vm.markStep("activity_goals")
                onNext()
            })
        },
    ) {
        StepHeading(
            "STEP 2 OF 5 • CORE BIOMETRICS",
            "Your Biometrics",
            "Calibrate your biological baseline and metabolic floor.",
            0.4f,
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Text("Basic Details", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Text("Date of Birth", color = InkMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Canvas)
                        .border(1.dp, Line, RoundedCornerShape(14.dp))
                        .clickable { showDate = true }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(prettyDobShort(draft.dateOfBirth), color = Ink, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
                }
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Canvas)
                        .border(1.dp, Mint.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
                    Text(
                        if (age != null) "  $age yrs (Verified)" else "  —",
                        color = Mint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Biological Sex", color = InkMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Canvas)
                    .padding(4.dp),
            ) {
                SexPill("Male", Icons.Filled.Male, draft.sexAtBirth == "male", Modifier.weight(1f)) {
                    vm.editDraft { it.copy(sexAtBirth = "male") }
                }
                SexPill("Female", Icons.Filled.Female, draft.sexAtBirth == "female", Modifier.weight(1f)) {
                    vm.editDraft { it.copy(sexAtBirth = "female") }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Stature / Standing Height", color = InkMuted, fontSize = 12.sp)
                Text(
                    "Equivalent: ${ProfileRules.cmToFeet(draft.heightCm)}",
                    color = Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (draft.heightUnit == "cm") "${draft.heightCm}" else ProfileRules.cmToFeet(draft.heightCm),
                    color = Ink,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (draft.heightUnit == "cm") {
                    Text(" cm", color = InkMuted, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Canvas)
                        .border(1.dp, Line, CircleShape)
                        .clickable {
                            vm.editDraft {
                                it.copy(heightUnit = if (it.heightUnit == "cm") "ft" else "cm")
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("CM • FT", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StepperIcon(false, Modifier.weight(1f)) {
                    vm.editDraft {
                        it.copy(heightCm = ProfileRules.bumpHeightCm(it.heightCm, it.heightUnit, -1))
                    }
                }
                StepperIcon(true, Modifier.weight(1f)) {
                    vm.editDraft {
                        it.copy(heightCm = ProfileRules.bumpHeightCm(it.heightCm, it.heightUnit, 1))
                    }
                }
            }
        }
    }
    if (showDate) {
        val picker = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let { millis ->
                        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        vm.editDraft { it.copy(dateOfBirth = fmt.format(Date(millis))) }
                    }
                    showDate = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } },
        ) { DatePicker(state = picker) }
    }
}

@Composable
fun ActivityScreen(onNext: () -> Unit, onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val draft = vm.state.collectAsStateWithLifecycle().value.draft
    val strategy = ProfileRules.strategyCopy(draft)
    val deltaKg = ((draft.targetWeightKg - draft.weightKg) * 10).roundToInt() / 10.0
    val goalCaption = when {
        abs(deltaKg) < 0.05 -> "stabilization target"
        deltaKg < 0 -> "${String.format(Locale.US, "%.1f", abs(deltaKg))} kg target"
        else -> "+${String.format(Locale.US, "%.1f", deltaKg)} kg target"
    }
    OnboardScaffold(
        footer = {
            PrimaryButton("Continue to Target Pace & Burn", onClick = {
                vm.markStep("target_pace")
                onNext()
            })
        },
    ) {
        StepHeading(
            "STEP 3 OF 5 • ACTIVITY & INTENT",
            "Activity & Goals",
            "Choose your daily movement level and target pace.",
            0.6f,
            onBack = onBack,
        )
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DAILY ACTIVITY LEVEL", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Text("Select one", color = InkMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        ActivityRow("Sedentary", "Desk work, light daily steps", Icons.Filled.Weekend, draft.activityLevel == "sedentary") {
            vm.editDraft { it.copy(activityLevel = "sedentary") }
        }
        Spacer(Modifier.height(8.dp))
        ActivityRow("Moderate", "Gym or active 3-4 days/week", Icons.Filled.FitnessCenter, draft.activityLevel == "moderate") {
            vm.editDraft { it.copy(activityLevel = "moderate") }
        }
        Spacer(Modifier.height(8.dp))
        ActivityRow("Active", "Very active or intense workouts", Icons.Filled.DirectionsRun, draft.activityLevel == "active") {
            vm.editDraft { it.copy(activityLevel = "active") }
        }
        Spacer(Modifier.height(20.dp))
        Text("YOUR PRIMARY GOAL", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoalTile("Steady Weight Loss", "Gradual, healthy fat loss", Icons.Filled.TrendingDown, draft.goal == "lose", Modifier.weight(1f)) { vm.setGoal("lose") }
            GoalTile("Maintain Weight", "Keep current weight", Icons.Filled.Balance, draft.goal == "maintain", Modifier.weight(1f)) { vm.setGoal("maintain") }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoalTile("Build Muscle", "Lean muscle & strength", Icons.Filled.FitnessCenter, draft.goal == "build_muscle", Modifier.weight(1f)) { vm.setGoal("build_muscle") }
            GoalTile("Eat Healthier", "Better nutrition & energy", Icons.Filled.Favorite, draft.goal == "eat_healthier", Modifier.weight(1f)) { vm.setGoal("eat_healthier") }
        }
        Spacer(Modifier.height(18.dp))
        Text("CURRENT & GOAL WEIGHT", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WeightCard(
                label = "CURRENT WEIGHT",
                value = String.format(Locale.US, "%.1f", draft.weightKg),
                caption = "${ProfileRules.kgToLb(draft.weightKg)} lbs",
                modifier = Modifier.weight(1f),
                onMinus = { vm.editDraft { it.copy(weightKg = (it.weightKg - 0.5).coerceIn(30.0, 300.0)) } },
                onPlus = { vm.editDraft { it.copy(weightKg = (it.weightKg + 0.5).coerceIn(30.0, 300.0)) } },
            )
            GoalWeightCard(
                value = String.format(Locale.US, "%.1f", draft.targetWeightKg),
                caption = goalCaption,
                modifier = Modifier.weight(1f),
                onMinus = { vm.editDraft { it.copy(targetWeightKg = (it.targetWeightKg - 0.5).coerceIn(30.0, 300.0)) } },
                onPlus = { vm.editDraft { it.copy(targetWeightKg = (it.targetWeightKg + 0.5).coerceIn(30.0, 300.0)) } },
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text(strategy.first, color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(strategy.second, color = InkMuted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun PaceScreen(onNext: () -> Unit, onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val draft = vm.state.collectAsStateWithLifecycle().value.draft
    var useLbs by remember { mutableStateOf(false) }
    val surplus = draft.goal == "build_muscle"
    val pinned = draft.goal == "maintain" || draft.goal == "eat_healthier"
    val snaps = PaceLogic.snapsFor(draft.goal)
    val index = snaps.indexOfFirst { it.first == draft.weeklyPaceKg }.let { if (it < 0) 1 else it }
    val burn = PaceLogic.burnOrSurplus(draft.goal, draft.weeklyPaceKg)
    val weeks = if (draft.weeklyPaceKg > 0) abs(draft.targetWeightKg - draft.weightKg) / draft.weeklyPaceKg else 0.0
    val shownPace = if (useLbs) draft.weeklyPaceKg * 2.20462 else draft.weeklyPaceKg
    val unit = if (useLbs) "lbs" else "kg"
    val pacePrefix = when {
        pinned -> ""
        surplus -> "+"
        else -> ""
    }
    OnboardScaffold(
        footer = {
            PrimaryButton("Continue to Dietary Safeguards", onClick = {
                vm.markStep("diet_safeguards")
                onNext()
            })
        },
    ) {
        StepHeading(
            if (surplus) "STEP 4 OF 5 • PACE & SURPLUS" else "STEP 4 OF 5 • PACE & DAILY BURN",
            PaceLogic.headline(draft.goal),
            PaceLogic.subhead(draft.goal),
            0.8f,
            onBack = onBack,
            progressLabel = "80% CALIBRATED",
            titleAlignStart = true,
        )
        Spacer(Modifier.height(16.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardRest)
                .border(1.dp, Line, RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Speed, contentDescription = null, tint = Mint, modifier = Modifier.size(20.dp))
                Text(
                    if (surplus) "  Weekly Surplus Pace" else "  Weekly Deficit Pace",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.clip(CircleShape).background(Canvas).padding(4.dp)) {
                UnitChip("KG / week", !useLbs) { useLbs = false }
                UnitChip("LBS / week", useLbs) { useLbs = true }
            }
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Canvas)
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (pinned) "0.00" else String.format(Locale.US, "%s%.2f", pacePrefix, shownPace),
                        color = Mint,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(" $unit / week", color = Ink, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                    Spacer(Modifier.weight(1f))
                    Text(
                        PaceLogic.badge(draft.goal, draft.weeklyPaceKg),
                        color = Mint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .widthIn(max = 140.dp)
                            .clip(CircleShape)
                            .background(Mint.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 2,
                    )
                }
                if (!pinned && weeks > 0.2) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cyan.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text(
                            if (surplus) {
                                "  Projected reach: +${String.format(Locale.US, "%.1f", abs(draft.targetWeightKg - draft.weightKg))} kg lean mass in ~${weeks.roundToInt()} weeks."
                            } else {
                                "  Projected reach: ${String.format(Locale.US, "%.1f", draft.targetWeightKg)} kg in ~${weeks.roundToInt()} weeks without metabolic rebound."
                            },
                            color = Cyan,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (!pinned) {
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = index.toFloat(),
                    onValueChange = { value ->
                        val snap = snaps[value.roundToInt().coerceIn(0, snaps.lastIndex)]
                        vm.editDraft { it.copy(weeklyPaceKg = snap.first) }
                    },
                    valueRange = 0f..snaps.lastIndex.toFloat(),
                    steps = (snaps.size - 2).coerceAtLeast(0),
                    colors = SliderDefaults.colors(
                        thumbColor = Mint,
                        activeTrackColor = Mint,
                        inactiveTrackColor = CardHighest,
                    ),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    snaps.forEach { (kg, label) ->
                        val number = if (useLbs) kg * 2.20462 else kg
                        val selected = kg == draft.weeklyPaceKg
                        val prefix = if (surplus) "+" else ""
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format(Locale.US, "%s%.2f", prefix, number),
                                color = if (selected) Mint else InkMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(label, color = if (selected) Mint else InkMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        if (surplus) {
            MetricSectionCard(
                title = "Target Daily Surplus",
                titleIcon = Icons.Filled.Bolt,
                titleIconTint = Cyan,
                badge = "CALIBRATED SURPLUS",
            ) {
                MetricInnerCard(
                    leadingIcon = Icons.Filled.Bolt,
                    leadingTint = Mint,
                    chip = "Net Surplus",
                    metric = "CALORIC SURPLUS TARGET",
                    value = burn.first,
                    unit = "kcal / day",
                    caption = burn.second,
                )
                Spacer(Modifier.height(10.dp))
                MetricInnerCard(
                    leadingIcon = Icons.Filled.DirectionsWalk,
                    leadingTint = Cyan,
                    chip = "Active Base",
                    metric = "TARGET DAILY MOVEMENT",
                    value = PaceLogic.stepsBand(draft.goal, draft.weeklyPaceKg),
                    unit = "steps / day",
                    caption = PaceLogic.movementCaption(draft.goal, draft.weeklyPaceKg),
                )
            }
        } else {
            MetricSectionCard(
                title = "Target Daily Movement",
                titleIcon = Icons.Filled.DirectionsWalk,
                titleIconTint = Cyan,
                badge = "CALIBRATED EXPENDITURE",
            ) {
                MetricInnerCard(
                    leadingIcon = Icons.Filled.DirectionsWalk,
                    leadingTint = Mint,
                    chip = "Active Base",
                    metric = "DAILY TARGET STEPS",
                    value = PaceLogic.stepsBand(draft.goal, draft.weeklyPaceKg),
                    unit = "steps / day",
                    caption = PaceLogic.movementCaption(draft.goal, draft.weeklyPaceKg),
                )
            }
            Spacer(Modifier.height(12.dp))
            MetricSectionCard(
                title = "Target Burn",
                titleIcon = Icons.Filled.LocalFireDepartment,
                titleIconTint = AmberAccent,
                badge = null,
            ) {
                MetricInnerCard(
                    leadingIcon = Icons.Filled.LocalFireDepartment,
                    leadingTint = AmberAccent,
                    chip = "Active Base",
                    metric = "ACTIVE CALORIE BURN",
                    value = burn.first,
                    unit = "kcal / day",
                    caption = burn.second,
                )
                Spacer(Modifier.height(8.dp))
                val burnSplit = PaceLogic.burnSplitCaption(draft.goal, draft.weeklyPaceKg)
                if (burnSplit.isNotBlank()) {
                    Text(burnSplit, color = InkMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                } else {
                    Spacer(Modifier.height(10.dp))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Canvas)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
                    Column(Modifier.padding(start = 8.dp)) {
                        Text("Preserves Resting Metabolism (BMR)", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            "Hitting this personalized step target maintains mitochondrial activity and prevents adaptive metabolic slowing during calorie deficits.",
                            color = InkMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietScreen(onBack: () -> Unit) {
    val vm = rememberFitViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val draft = state.draft
    val patterns = listOf(
        Triple("all", "All", "All foods included"),
        Triple("vegetarian", "Vegetarian", "No meat or fish"),
        Triple("vegan", "Vegan", "100% plant-based"),
        Triple("omnivore", "Omnivore", "No dietary exclusions"),
    )
    OnboardScaffold(
        footer = {
            if (state.error != null) {
                Text(state.error ?: "", color = Color(0xFFFFB4AB), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            PrimaryButton(
                if (state.busy) "Calculating…" else "Calculate Targets",
                onClick = { vm.calculateTargets() },
                enabled = !state.busy,
            )
            Text(
                "Skip for now",
                color = InkMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        vm.editDraft { it.copy(dietTypes = emptyList(), allergenFlags = emptyList()) }
                        vm.calculateTargets()
                    }
                    .padding(vertical = 8.dp),
            )
        },
    ) {
        StepHeading(
            "STEP 5 OF 5 • DIET & SAFEGUARDS",
            "Diet & Safeguards",
            "Select your dietary pattern and any allergies to filter safe foods.",
            1f,
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("DIETARY PATTERN", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
                Text("  Selected", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))
        patterns.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                row.forEach { (key, title, body) ->
                    val selected = if (key == "all") draft.dietTypes.isEmpty() else draft.dietTypes.contains(dietValue(key))
                    DietTile(title, body, selected, Modifier.weight(1f)) {
                        vm.editDraft { current ->
                            current.copy(
                                dietTypes = when (key) {
                                    "all" -> emptyList()
                                    "vegetarian" -> listOf("vegetarian")
                                    "vegan" -> listOf("vegan")
                                    else -> listOf("non-vegetarian")
                                },
                            )
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("COMMON ALLERGENS", color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Text(if (draft.allergenFlags.isEmpty()) "None selected" else "${draft.allergenFlags.size} selected", color = InkMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        val chips = listOf("none" to "None") + listOf("lactose", "gluten", "nuts", "eggs", "seafood", "soy").map {
            it to it.replaceFirstChar { c -> c.titlecase(Locale.US) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.forEach { (key, label) ->
                val on = if (key == "none") draft.allergenFlags.isEmpty() else key in draft.allergenFlags
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(if (on) Mint else CardRest)
                        .border(1.dp, if (on) Mint else Line, CircleShape)
                        .clickable {
                            vm.editDraft { current ->
                                if (key == "none") current.copy(allergenFlags = emptyList())
                                else {
                                    val next = current.allergenFlags.toMutableSet()
                                    if (!next.add(key)) next.remove(key)
                                    current.copy(allergenFlags = next.toList())
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (on) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (on) OnMint else Ink,
                        modifier = Modifier.size(14.dp),
                    )
                    Text("  $label", color = if (on) OnMint else Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun dietValue(key: String) = if (key == "omnivore") "non-vegetarian" else key

@Composable
private fun OnboardScaffold(footer: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            content()
            Spacer(Modifier.height(12.dp))
        }
        Column(
            Modifier.fillMaxWidth().background(Canvas).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) { footer() }
    }
}

@Composable
private fun StepHeading(
    kicker: String,
    title: String,
    body: String,
    progress: Float,
    onBack: (() -> Unit)? = null,
    progressLabel: String? = null,
    titleAlignStart: Boolean = false,
) {
    val titleAlign = if (titleAlignStart) TextAlign.Start else TextAlign.Center
    val columnAlign = if (titleAlignStart) Alignment.Start else Alignment.CenterHorizontally
    Column(Modifier.fillMaxWidth(), horizontalAlignment = columnAlign) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (onBack != null) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(8.dp)
                        .size(22.dp),
                )
            } else {
                Spacer(Modifier.size(38.dp))
            }
            Text(
                kicker,
                color = Mint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            if (progressLabel != null) {
                Text(
                    progressLabel,
                    color = InkMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                )
            } else {
                Spacer(Modifier.size(38.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0xFF2A3142))) {
            Box(Modifier.fillMaxWidth(progress).height(6.dp).clip(CircleShape).background(Mint))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = titleAlign, lineHeight = 34.sp)
        Spacer(Modifier.height(8.dp))
        Text(body, color = InkMuted, fontSize = 14.sp, textAlign = titleAlign, lineHeight = 20.sp)
    }
}

@Composable
private fun UnitChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) OnMint else InkMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Mint else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun SexPill(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(CircleShape)
            .background(if (selected) Mint else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) OnMint else Ink, modifier = Modifier.size(16.dp))
        Text("  $label", color = if (selected) OnMint else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun StepperIcon(plus: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (plus) Icons.Filled.Add else Icons.Filled.Remove, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun WeightCard(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Text(label, color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(" kg", color = InkMuted, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text(caption, color = InkMuted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepperIcon(false, Modifier.weight(1f), onMinus)
            StepperIcon(true, Modifier.weight(1f), onPlus)
        }
    }
}

@Composable
private fun GoalWeightCard(
    value: String,
    caption: String,
    modifier: Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardRest)
            .border(1.dp, Mint.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("GOAL WEIGHT", color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Icon(Icons.Filled.Flag, contentDescription = null, tint = Mint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Mint, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(" kg", color = InkMuted, modifier = Modifier.padding(bottom = 4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = Mint, modifier = Modifier.size(14.dp))
            Text("  $caption", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepperIcon(false, Modifier.weight(1f), onMinus)
            StepperIcon(true, Modifier.weight(1f), onPlus)
        }
    }
}

@Composable
private fun ActivityRow(title: String, body: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (selected) Color(0xFF10261C) else CardRest).border(1.dp, if (selected) Mint.copy(alpha = 0.8f) else Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) Mint.copy(alpha = 0.16f) else Canvas), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = if (selected) Mint else InkMuted, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(title, color = if (selected) Mint else Ink, fontWeight = FontWeight.SemiBold)
            Text(body, color = InkMuted, fontSize = 12.sp)
        }
        if (selected) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = OnMint, modifier = Modifier.size(14.dp))
            }
        } else {
            Box(Modifier.size(18.dp).clip(CircleShape).border(1.dp, Line, CircleShape))
        }
    }
}

@Composable
private fun GoalTile(title: String, body: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(148.dp).clip(RoundedCornerShape(18.dp)).background(if (selected) Color(0xFF10261C) else CardRest).border(1.dp, if (selected) Mint.copy(alpha = 0.7f) else Line, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) Mint.copy(alpha = 0.16f) else Canvas), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (selected) Mint else InkMuted, modifier = Modifier.size(16.dp))
            }
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Mint, modifier = Modifier.size(18.dp))
            } else {
                Box(Modifier.size(16.dp).clip(CircleShape).border(1.dp, Line, CircleShape))
            }
        }
        Spacer(Modifier.weight(1f))
        Text(title, color = if (selected) Mint else Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp)
        Text(body, color = InkMuted, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun DietTile(title: String, body: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color(0xFF10261C) else CardRest)
            .border(1.dp, if (selected) Mint else Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Mint, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Filled.RadioButtonUnchecked, contentDescription = null, tint = InkMuted, modifier = Modifier.size(20.dp))
            }
            if (selected) {
                Spacer(Modifier.weight(1f))
                Text(
                    "Active",
                    color = OnMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Mint)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(body, color = InkMuted, fontSize = 12.sp)
    }
}

@Composable
private fun MetricSectionCard(
    title: String,
    titleIcon: ImageVector,
    titleIconTint: Color,
    badge: String?,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardRest)
            .border(1.dp, Line, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(titleIcon, contentDescription = null, tint = titleIconTint, modifier = Modifier.size(18.dp))
            Text("  $title", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            if (badge != null) {
                Text(
                    badge,
                    color = Cyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Cyan.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun MetricInnerCard(
    leadingIcon: ImageVector,
    leadingTint: Color,
    chip: String,
    metric: String,
    value: String,
    unit: String,
    caption: String,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Canvas)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(leadingTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(leadingIcon, contentDescription = null, tint = leadingTint, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                chip,
                color = InkMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardHighest)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(metric, color = InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(unit, color = Mint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(caption, color = InkMuted, fontSize = 12.sp)
    }
}

private val AmberAccent = Color(0xFFFFBC68)

private fun prettyDob(iso: String): String = runCatching {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
    SimpleDateFormat("d MMM yyyy", Locale.US).format(date)
}.getOrDefault(iso)

private fun prettyDobShort(iso: String): String = runCatching {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
    SimpleDateFormat("d MMM", Locale.US).format(date)
}.getOrDefault(iso)

@SuppressLint("MissingPermission")
private fun resolveDevicePlace(context: Context): RegionOption? {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val location = sequenceOf(
        LocationManager.NETWORK_PROVIDER,
        LocationManager.GPS_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).mapNotNull { provider ->
        runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
    }.maxByOrNull { it.time } ?: return null
    if (!Geocoder.isPresent()) {
        return PlaceResolver.resolve("${location.latitude},${location.longitude}")
    }
    val address = runCatching {
        @Suppress("DEPRECATION")
        Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
    }.getOrNull()?.firstOrNull() ?: return null
    val label = listOfNotNull(address.locality, address.adminArea, address.countryName)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
        .ifBlank { address.getAddressLine(0).orEmpty() }
    return PlaceResolver.resolve(label, address.countryCode)
}
