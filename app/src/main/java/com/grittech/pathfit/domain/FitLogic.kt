package com.grittech.pathfit.domain

import com.grittech.pathfit.model.CommonCard
import com.grittech.pathfit.model.CommonItem
import com.grittech.pathfit.model.DishHit
import com.grittech.pathfit.model.IngredientHit
import com.grittech.pathfit.model.MacroTotals
import com.grittech.pathfit.model.OnboardingDraft
import com.grittech.pathfit.model.PlateItem
import com.grittech.pathfit.model.PreviewUi
import com.grittech.pathfit.model.RegionOption
import com.grittech.pathfit.model.Targets
import com.grittech.pathfit.model.UsualItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Place → countryCode / regionKey resolver (Maps/Places geocode handoff).
 * PK keys must match Common meals seeds — never invent; never write `pk-ict`.
 */
object PlaceResolver {
    val options = listOf(
        RegionOption("Lahore, Punjab", "PK", "pk-punjab"),
        RegionOption("Faisalabad, Punjab", "PK", "pk-punjab"),
        RegionOption("Multan, Punjab", "PK", "pk-punjab"),
        RegionOption("Karachi, Sindh", "PK", "pk-sindh"),
        RegionOption("Hyderabad, Sindh", "PK", "pk-sindh"),
        RegionOption("Islamabad", "PK", "pk-islamabad"),
        RegionOption("Rawalpindi", "PK", "pk-islamabad"),
        RegionOption("Peshawar, Khyber Pakhtunkhwa", "PK", "pk-kpk"),
        RegionOption("Abbottabad, Khyber Pakhtunkhwa", "PK", "pk-kpk"),
        RegionOption("Quetta, Balochistan", "PK", "pk-balochistan"),
        RegionOption("Gwadar, Balochistan", "PK", "pk-balochistan"),
        RegionOption("Chennai, Tamil Nadu", "IN", "in-south"),
        RegionOption("Delhi, India", "IN", "in-delhi"),
        RegionOption("Mumbai, Maharashtra", "IN", "in-maharashtra"),
        RegionOption("Dhaka, Bangladesh", "BD", "bd-dhaka"),
        RegionOption("London, United Kingdom", "GB", "gb-london"),
    )

    fun migrateRegionKey(key: String?): String? = when (key?.trim()?.lowercase()) {
        null, "" -> null
        "pk-ict" -> "pk-islamabad"
        else -> key
    }

    fun search(query: String): List<RegionOption> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return options
        return options.filter { it.label.lowercase().contains(q) || matchesAdmin(q, it) }
    }

    /** Resolve a Places/geocode label or free-text place into FitPath country + region. */
    fun resolve(placeLabel: String, countryHint: String? = null): RegionOption {
        val raw = placeLabel.trim()
        val lower = raw.lowercase()
        val exact = options.firstOrNull { it.label.equals(raw, ignoreCase = true) }
        if (exact != null) return exact

        val country = (countryHint ?: inferCountry(lower) ?: "PK").uppercase()
        val regionKey = when (country) {
            "PK" -> resolvePk(lower)
            "IN" -> resolveIn(lower)
            "BD" -> if (lower.contains("dhaka")) "bd-dhaka" else null
            "GB" -> if (lower.contains("london")) "gb-london" else null
            else -> null
        }
        val label = raw.ifBlank {
            options.firstOrNull { it.countryCode == country && it.regionKey == regionKey }?.label
                ?: country
        }
        return RegionOption(label = label, countryCode = country, regionKey = regionKey)
    }

    private fun matchesAdmin(q: String, option: RegionOption): Boolean {
        val tokens = option.label.lowercase().split(',', ' ', '-').filter { it.length > 2 }
        return tokens.any { it.startsWith(q) || q.startsWith(it) }
    }

    private fun inferCountry(lower: String): String? = when {
        listOf("pakistan", "lahore", "karachi", "islamabad", "rawalpindi", "peshawar",
            "quetta", "multan", "faisalabad", "hyderabad", "abbottabad", "gwadar",
            "punjab", "sindh", "balochistan", "kpk", "khyber").any { it in lower } -> "PK"
        listOf("india", "chennai", "delhi", "mumbai", "tamil", "maharashtra").any { it in lower } -> "IN"
        listOf("bangladesh", "dhaka").any { it in lower } -> "BD"
        listOf("london", "united kingdom", "uk").any { it in lower } -> "GB"
        else -> null
    }

    private fun resolvePk(lower: String): String? = when {
        listOf("lahore", "faisalabad", "multan", "punjab").any { it in lower } &&
            !listOf("rawalpindi", "islamabad").any { it in lower } -> "pk-punjab"
        listOf("karachi", "hyderabad", "sindh").any { it in lower } -> "pk-sindh"
        listOf("islamabad", "rawalpindi", "ict", "islamabad capital territory").any { it in lower } ->
            "pk-islamabad"
        listOf("peshawar", "abbottabad", "kpk", "khyber", "pakhtunkhwa").any { it in lower } -> "pk-kpk"
        listOf("quetta", "gwadar", "balochistan").any { it in lower } -> "pk-balochistan"
        else -> null
    }

    private fun resolveIn(lower: String): String? = when {
        listOf("chennai", "tamil", "south").any { it in lower } -> "in-south"
        listOf("delhi").any { it in lower } -> "in-delhi"
        listOf("mumbai", "maharashtra").any { it in lower } -> "in-maharashtra"
        else -> null
    }
}

@Deprecated("Use PlaceResolver", ReplaceWith("PlaceResolver"))
object Regions {
    val options get() = PlaceResolver.options
}

object DishSearch {
    /** Token / word-prefix match — not mid-word substring (anda ≠ Pasanday / Hollandaise). */
    fun matches(dish: DishHit, query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isBlank()) return true
        val needles = listOf(dish.nameEn, dish.slug, dish.detail) + dish.tags + dish.searchAliases
        return q.split(Regex("\\s+")).all { token ->
            needles.any { field -> tokenPrefixHit(field, token) }
        }
    }

    fun tokenPrefixHit(field: String, token: String): Boolean {
        if (token.isBlank()) return true
        val words = field.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
        return words.any { it.startsWith(token) }
    }
}

object DayClock {
    fun zone(id: String): TimeZone = TimeZone.getTimeZone(id.ifBlank { "Asia/Karachi" })

    fun todayKey(timezone: String, now: Long = System.currentTimeMillis()): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fmt.timeZone = zone(timezone)
        return fmt.format(now)
    }

    fun resolveKey(dayKey: String, timezone: String, now: Long = System.currentTimeMillis()): String =
        if (dayKey.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) dayKey else todayKey(timezone, now)

    fun shiftKey(dayKey: String, days: Int, timezone: String = "Asia/Karachi"): String {
        val key = resolveKey(dayKey, timezone)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = runCatching { fmt.parse(key) }.getOrNull() ?: return todayKey(timezone)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, days)
        return fmt.format(cal.time)
    }

    fun headerLabel(dayKey: String, timezone: String, now: Long = System.currentTimeMillis()): String {
        val key = resolveKey(dayKey, timezone, now)
        val today = todayKey(timezone, now)
        val fmtIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = runCatching { fmtIn.parse(key) }.getOrNull() ?: return "Today"
        val pretty = SimpleDateFormat("MMM d", Locale.US).format(date)
        return when (key) {
            today -> "Today, $pretty"
            shiftKey(today, -1, timezone) -> "Yesterday, $pretty"
            shiftKey(today, 1, timezone) -> "Tomorrow, $pretty"
            else -> pretty
        }
    }

    fun hour(timezone: String, now: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance(zone(timezone))
        cal.timeInMillis = now
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    fun clockLabel(timezone: String, now: Long = System.currentTimeMillis()): String {
        val fmt = SimpleDateFormat("h:mm a", Locale.US)
        fmt.timeZone = zone(timezone)
        return fmt.format(now)
    }

    fun defaultSlot(hour: Int): String = when (hour) {
        in 5..9 -> "breakfast"
        in 10..14 -> "lunch"
        in 15..20 -> "dinner"
        else -> "snack"
    }

    fun slotWindow(slot: String): String = when (slot) {
        "breakfast" -> "8:00 AM"
        "lunch" -> "1:00 PM"
        "dinner" -> "8:00 PM"
        else -> "5:00 PM"
    }
}

object PreviewLogic {
    fun bindPreview(targets: Targets): PreviewUi {
        val active = targets.status == "active" && targets.calorieTarget != null
        val floorCapped = targets.floorApplied ||
            targets.adjustments.any { it == "floor_applied" || it == "lose_capped_at_tdee" }
        val delta = (targets.appliedGoalDelta ?: targets.goalDelta)?.roundToInt()
        return PreviewUi(
            active = active,
            blockMessage = if (active) null else blockCopy(targets),
            heroKcal = if (active) targets.calorieTarget else null,
            subtitle = if (active) subtitle(targets) else "",
            bmr = if (active) targets.bmr?.roundToInt() else null,
            tdee = if (active) targets.tdee?.roundToInt() else null,
            delta = if (active) delta else null,
            deltaLabel = deltaLabel(delta),
            deltaCaption = deltaCaption(targets.goalEffective, delta, floorCapped),
            proteinG = if (active) targets.proteinG else null,
            carbsG = if (active) targets.carbsG else null,
            fatG = if (active) targets.fatG else null,
            proteinPct = targets.proteinPct ?: 30,
            carbsPct = targets.carbsPct ?: 40,
            fatPct = targets.fatPct ?: 30,
            clinicalPending = targets.clinicalPending,
            floorCapped = floorCapped,
        )
    }

    fun subtitle(targets: Targets): String {
        val goal = targets.goalEffective ?: targets.goalRequested
        val pace = targets.weeklyPaceKg
        val paceText = pace?.let { String.format(Locale.US, "%.1f", it) }
        val fragment = when (goal) {
            "lose" -> if (paceText != null) "$paceText kg/week steady fat loss" else "steady fat loss"
            "build_muscle" -> if (paceText != null) "$paceText kg/week lean hypertrophy" else "lean hypertrophy"
            "gain" -> if (paceText != null) "$paceText kg/week lean gain" else "lean gain"
            "maintain" -> "weight maintenance"
            "eat_healthier" -> "healthier eating"
            else -> "your metabolic baseline"
        }
        return "Target intake for $fragment"
    }

    fun deltaLabel(delta: Int?): String = when {
        delta == null -> "BALANCE"
        delta < 0 -> "DEFICIT"
        delta > 0 -> "SURPLUS"
        else -> "BALANCE"
    }

    fun deltaCaption(goal: String?, delta: Int?, floorCapped: Boolean): String {
        if (floorCapped) return "Safety floor applied"
        return when {
            delta != null && delta < 0 -> "Target cut for fat loss"
            delta != null && delta > 0 -> when (goal) {
                "build_muscle" -> "Surplus for lean muscle"
                else -> "Target surplus for gain"
            }
            goal == "eat_healthier" -> "Intake matched to healthier eating"
            else -> "Intake matched to maintenance"
        }
    }

    private fun blockCopy(targets: Targets): String {
        if (targets.status == "incomplete") {
            return "Finish the missing profile fields before fuel targets can be shown."
        }
        if (targets.blockReasons.isEmpty()) return "Targets are paused for this profile."
        return targets.blockReasons.joinToString(" ") { reason ->
            when (reason) {
                "under_18" -> "Targets stay paused under 18."
                "low_bmi" -> "BMI is below the safe range for a calorie target."
                "pregnancy" -> "Pregnancy needs a clinician before calorie targets."
                else -> reason.replace('_', ' ').replaceFirstChar { it.titlecase(Locale.US) } + "."
            }
        }
    }
}

data class EnergyInput(
    val kcalPerServing: Double,
    val addedFatKcalPerServing: Double,
    val portion: Double,
    val oilLevel: String?,
)

object CalorieMath {
    fun oilFactor(oilLevel: String?): Double = when (oilLevel) {
        "light" -> 8.0 / 14.0
        "rich" -> 22.0 / 14.0
        "normal" -> 1.0
        else -> 0.0
    }

    fun computeItemEnergy(input: EnergyInput): Double {
        val base = input.kcalPerServing * input.portion
        if (input.oilLevel == null) return base
        val added = if (input.addedFatKcalPerServing > 0.0) {
            input.addedFatKcalPerServing * input.portion * oilFactor(input.oilLevel)
        } else {
            val grams = when (input.oilLevel) {
                "light" -> 8.0
                "rich" -> 22.0
                else -> 14.0
            }
            grams * 9.0 * input.portion
        }
        return base + added
    }

    fun extraFatGrams(oilLevel: String?, portion: Double): Double {
        val grams = when (oilLevel) {
            "light" -> 8.0
            "rich" -> 22.0
            "normal" -> 14.0
            else -> 0.0
        }
        return grams * portion
    }

    fun plateTotals(items: List<PlateItem>): MacroTotals = sum(
        items.map { item ->
            line(
                item.kcalPerServing,
                item.proteinPer,
                item.carbsPer,
                item.fatPer,
                item.addedFatKcal,
                item.portion,
                item.oilLevel,
            )
        },
    )

    fun usualTotals(items: List<UsualItem>): MacroTotals = sum(
        items.map { item ->
            line(
                item.kcalPerServing,
                item.proteinPer,
                item.carbsPer,
                item.fatPer,
                item.addedFatKcal,
                item.portion,
                item.oilLevel,
            )
        },
    )

    fun line(
        kcalPer: Double,
        proteinPer: Double,
        carbsPer: Double,
        fatPer: Double,
        addedFatKcal: Double,
        portion: Double,
        oilLevel: String?,
    ): MacroTotals {
        val kcal = computeItemEnergy(
            EnergyInput(kcalPer, addedFatKcal, portion, oilLevel),
        )
        return MacroTotals(
            kcal = kcal.roundToInt(),
            proteinG = proteinPer * portion,
            carbsG = carbsPer * portion,
            fatG = fatPer * portion + extraFatGrams(oilLevel, portion),
        )
    }

    private fun sum(parts: List<MacroTotals>): MacroTotals = MacroTotals(
        kcal = parts.sumOf { it.kcal },
        proteinG = parts.sumOf { it.proteinG },
        carbsG = parts.sumOf { it.carbsG },
        fatG = parts.sumOf { it.fatG },
    )
}

object PaceLogic {
    val loseSnaps = listOf(0.25 to "Gentle", 0.50 to "Target", 0.75 to "Active", 1.00 to "Max Safe")
    val surplusSnaps = listOf(0.15 to "Gentle", 0.25 to "Target", 0.40 to "Active", 0.50 to "Max Safe")

    fun snapsFor(goal: String) = if (goal == "build_muscle") surplusSnaps else loseSnaps

    fun defaultPace(goal: String): Double = when (goal) {
        "build_muscle" -> 0.25
        "maintain", "eat_healthier" -> 0.0
        else -> 0.5
    }

    fun headline(goal: String): String = when (goal) {
        "build_muscle" -> "Target Pace & Daily Surplus"
        "maintain" -> "Target Pace & Daily Burn"
        "eat_healthier" -> "Target Pace & Daily Burn"
        else -> "Target Pace & Daily Burn"
    }

    fun subhead(goal: String): String = when (goal) {
        "build_muscle" -> "Calibrate your daily energy surplus for controlled lean muscle growth without excessive fat accumulation."
        "maintain" -> "Keep intake near your burn. Movement stays gentle so weight holds steady."
        "eat_healthier" -> "A near-zero pace keeps the focus on food quality rather than a hard cut or surplus."
        else -> "Calibrate your weekly rate of change and calculate exact daily walking and calorie burn targets to reach your goal safely."
    }

    fun badge(goal: String, pace: Double): String = when (goal) {
        "build_muscle" -> "LEAN HYPERTROPHY (RECOMMENDED)"
        "maintain" -> "WEIGHT MAINTENANCE"
        "eat_healthier" -> "HEALTHIER EATING"
        else -> if (pace == 0.5) "STEADY FAT LOSS (RECOMMENDED)" else "WEEKLY FAT-LOSS PACE"
    }

    fun reachPhrase(currentKg: Double, targetKg: Double, paceKgWeek: Double, now: Long = System.currentTimeMillis()): String? {
        if (paceKgWeek <= 0.0) return null
        val delta = abs(targetKg - currentKg)
        if (delta < 0.05) return null
        val weeks = delta / paceKgWeek
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.add(Calendar.DAY_OF_YEAR, (weeks * 7.0).roundToInt())
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val band = when (day) {
            in 1..10 -> "early"
            in 11..20 -> "mid"
            else -> "late"
        }
        val month = SimpleDateFormat("MMMM", Locale.US).format(cal.time)
        val kg = String.format(Locale.US, "%.1f", targetKg)
        return "At this pace, you'll reach your $kg kg goal around $band-$month"
    }

    fun stepsBand(goal: String, pace: Double): String = when (goal) {
        "build_muscle" -> when {
            pace <= 0.15 -> "6,500 – 8,000"
            pace <= 0.25 -> "7,500 – 9,000"
            pace <= 0.40 -> "8,000 – 10,000"
            else -> "9,000 – 11,000"
        }
        "maintain", "eat_healthier" -> "6,000 – 8,000"
        else -> when {
            pace <= 0.25 -> "7,000 – 8,500"
            pace <= 0.50 -> "8,000 – 10,000"
            pace <= 0.75 -> "9,000 – 11,000"
            else -> "10,000 – 12,000"
        }
    }

    fun burnOrSurplus(goal: String, pace: Double): Pair<String, String> = when (goal) {
        "build_muscle" -> when {
            pace <= 0.15 -> "+150 – 250" to "~25g protein buffer + glycogen recovery"
            pace <= 0.25 -> "+250 – 350" to "~35g protein buffer + glycogen recovery"
            pace <= 0.40 -> "+350 – 450" to "~45g protein buffer + glycogen recovery"
            else -> "+450 – 550" to "~55g protein buffer + glycogen recovery"
        }
        "maintain", "eat_healthier" -> "Matched" to "Intake stays near your daily burn"
        else -> when {
            pace <= 0.25 -> "300 – 400" to "Non-exercise NEAT + intentional steady activity"
            pace <= 0.50 -> "450 – 550" to "Non-exercise NEAT + intentional steady activity"
            pace <= 0.75 -> "550 – 700" to "Non-exercise NEAT + intentional steady activity"
            else -> "700 – 850" to "Non-exercise NEAT + intentional steady activity"
        }
    }
}

object ProfileRules {
    fun ageYears(dob: String, now: Long = System.currentTimeMillis()): Int? {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = fmt.parse(dob) ?: return null
        val born = Calendar.getInstance().apply { time = date }
        val today = Calendar.getInstance().apply { timeInMillis = now }
        var years = today.get(Calendar.YEAR) - born.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < born.get(Calendar.DAY_OF_YEAR)) years -= 1
        return years
    }

    fun cmToFeet(cm: Int): String {
        val inches = (cm / 2.54).roundToInt()
        return "${inches / 12}'${inches % 12}\""
    }

    fun kgToLb(kg: Double): String = String.format(Locale.US, "%.1f", kg * 2.20462)

    fun slotTitle(slot: String): String = when (slot) {
        "breakfast" -> "Nashta"
        "lunch" -> "Lunch"
        "dinner" -> "Dinner"
        else -> "Snack"
    }

    fun slotSubtitle(slot: String): String = when (slot) {
        "breakfast" -> "Nashta (Breakfast)"
        "lunch" -> "Lunch (Dopahar)"
        "dinner" -> "Dinner (Raat)"
        else -> "Snack (Asar / Tea)"
    }

    fun goalLabel(goal: String): String = when (goal) {
        "lose" -> "Steady Weight Loss"
        "maintain" -> "Maintain Weight"
        "build_muscle" -> "Build Muscle"
        "eat_healthier" -> "Eat Healthier"
        else -> goal
    }

    fun firstName(displayName: String): String {
        val token = displayName.trim().split(" ").firstOrNull().orEmpty()
        return token.ifBlank { "there" }
    }

    fun rangeStatus(eaten: Int, target: Int?, hour: Int): Pair<String, String> {
        if (target == null || target <= 0) {
            return "Paused" to "Fuel targets are not active for this profile."
        }
        if (eaten <= 0) {
            return "Ready" to "Log your usual to see today's guidance."
        }
        val ratio = eaten.toDouble() / target.toDouble()
        return when {
            ratio <= 1.05 && (ratio >= 0.45 || hour < 17) ->
                "In Range" to "Glycemic trajectory: Stable • South Asian model"
            ratio <= 1.05 ->
                "Below" to "Intake is still light for the day. A planned meal can close the gap."
            ratio <= 1.15 ->
                "Approaching" to "Intake is nearing today's fuel target."
            else ->
                "Above" to "Intake is past today's fuel target. Later meals can stay lighter."
        }
    }

    fun strategyCopy(draft: OnboardingDraft): Pair<String, String> {
        val dropping = draft.targetWeightKg + 0.2 < draft.weightKg
        val rising = draft.targetWeightKg > draft.weightKg + 0.2
        return when {
            draft.goal == "build_muscle" || rising ->
                "Lean Gain Buffer" to "A gradual target leaves room for protein and training recovery."
            draft.goal == "maintain" || draft.goal == "eat_healthier" ->
                "Steady Metabolic Care" to "Holding weight keeps the focus on meal quality and daily rhythm."
            dropping ->
                "Gentle Glycemic Preservation" to "Gradual target retains resting metabolic rate without hunger spikes."
            else ->
                "Steady Metabolic Care" to "Your target sits close to today's weight."
        }
    }
}

object LocalCatalog {
    fun staples(): Map<String, List<CommonCard>> = mapOf(
        "breakfast" to listOf(
            card("pk-bf-aloo-paratha-chai", "breakfast", "Aloo Paratha + Chai", listOf(
                item("dish-aloo-paratha", "Aloo Paratha", 1.0, 320, "piece", 8.0, 42.0, 12.0, 90.0),
                item("dish-elaichi-chai", "Elaichi Chai", 1.0, 100, "cup", 2.0, 12.0, 4.0, 0.0),
            )),
            card("pk-bf-paratha-eggs", "breakfast", "Plain Paratha + 2 Eggs", listOf(
                item("dish-plain-paratha", "Plain Paratha", 1.0, 280, "piece", 6.0, 36.0, 12.0, 80.0),
                item("dish-boiled-egg", "Boiled Egg", 2.0, 105, "piece", 13.0, 1.0, 10.0, 0.0),
            )),
            card("pk-bf-eggs-chai", "breakfast", "Boiled Eggs + Chai", listOf(
                item("dish-boiled-egg", "Boiled Egg", 2.0, 105, "piece", 13.0, 1.0, 10.0, 0.0),
                item("dish-elaichi-chai", "Elaichi Chai", 1.0, 100, "cup", 2.0, 12.0, 4.0, 0.0),
            )),
        ),
        "lunch" to listOf(
            card("pk-lu-daal-roti", "lunch", "Daal Masoor + 2 Roti", listOf(
                item("dish-daal-masoor", "Daal Masoor", 1.0, 220, "katori", 14.0, 32.0, 6.0, 40.0),
                item("dish-roti", "Roti", 2.0, 150, "piece", 4.0, 28.0, 2.0, 20.0),
            )),
            card("pk-lu-karahi-roti", "lunch", "Chicken Karahi + Roti", listOf(
                item("dish-chicken-karahi", "Chicken Karahi", 1.0, 420, "serving", 32.0, 8.0, 24.0, 120.0),
                item("dish-roti", "Roti", 2.0, 150, "piece", 4.0, 28.0, 2.0, 20.0),
            )),
        ),
        "dinner" to listOf(
            card("pk-di-karahi-naan", "dinner", "Chicken Karahi + 1 Naan", listOf(
                item("dish-chicken-karahi", "Chicken Karahi", 1.0, 420, "serving", 32.0, 8.0, 24.0, 120.0),
                item("dish-naan", "Naan", 1.0, 260, "piece", 8.0, 42.0, 6.0, 40.0),
            )),
            card("pk-di-moong-roti", "dinner", "Daal Moong + 2 Roti + Salad", listOf(
                item("dish-daal-moong", "Daal Moong", 1.0, 180, "katori", 12.0, 28.0, 4.0, 30.0),
                item("dish-roti", "Roti", 2.0, 150, "piece", 4.0, 28.0, 2.0, 20.0),
            )),
            card("pk-di-sabzi-roti", "dinner", "Mixed Sabzi + 2 Roti", listOf(
                item("dish-mixed-sabzi", "Mixed Sabzi", 1.0, 160, "katori", 4.0, 16.0, 8.0, 50.0),
                item("dish-roti", "Roti", 2.0, 150, "piece", 4.0, 28.0, 2.0, 20.0),
            )),
        ),
        "snack" to listOf(
            card("pk-sn-chai-biscuit", "snack", "Elaichi Chai + 2 Biscuits", listOf(
                item("dish-elaichi-chai", "Elaichi Chai", 1.0, 100, "cup", 2.0, 12.0, 4.0, 0.0),
                item("dish-biscuit", "Biscuit", 2.0, 30, "piece", 1.0, 4.0, 1.0, 0.0),
            )),
            card("pk-sn-masala-eggs", "snack", "Masala Boiled Eggs", listOf(
                item("dish-masala-egg", "Masala Boiled Egg", 2.0, 90, "piece", 12.0, 2.0, 8.0, 0.0),
            )),
            card("pk-sn-fruit-chaat", "snack", "Fruit Chaat", listOf(
                item("dish-fruit-chaat", "Fruit Chaat", 1.0, 140, "bowl", 2.0, 28.0, 2.0, 0.0),
            )),
        ),
    )

    fun dishes(): List<DishHit> = listOf(
        DishHit(
            "dish-boiled-egg", "Boiled eggs (plain)", "Farm anda • hard boiled", 105.0, 13.0, 1.0, 10.0,
            tags = listOf("anda", "egg", "boiled"), searchAliases = listOf("anda", "egg", "boiled"),
            preps = listOf("Boiled (Hard)"), unit = "piece", slug = "boiled-eggs-plain",
        ),
        DishHit(
            "dish-masala-egg", "Masala boiled eggs", "Chaar masala anda", 90.0, 12.0, 2.0, 8.0,
            tags = listOf("anda", "egg", "boiled", "masala"), searchAliases = listOf("anda", "egg", "boiled", "masala"),
            unit = "piece", slug = "masala-boiled-eggs",
        ),
        DishHit(
            "dish-farm-egg", "Farm Fresh Eggs", "Desi / farm anda", 155.0, 13.0, 1.0, 10.0,
            tags = listOf("anda", "egg"), searchAliases = listOf("anda", "egg"),
            preps = listOf("Boiled (Hard)", "Desi Omelette", "Half Fry", "Poached"), unit = "piece", slug = "farm-fresh-eggs",
        ),
        DishHit(
            "dish-egg-white", "Desi Murgh Egg White", "Boiled • high protein", 17.0, 3.6, 0.2, 0.1,
            tags = listOf("anda", "egg", "boiled"), searchAliases = listOf("anda", "egg", "boiled"),
            unit = "piece", slug = "desi-murgh-egg-white",
        ),
        DishHit(
            "dish-cheese-omelette", "Cheese Mushroom Omelette", "1 serving", 240.0, 16.0, 4.0, 16.0, 40.0,
            tags = listOf("anda", "omelette", "egg"), searchAliases = listOf("anda", "egg", "omelette"),
            preps = listOf("Desi Omelette"), slug = "cheese-mushroom-omelette",
        ),
        DishHit(
            "dish-anda-ghotala", "Anda ghotala", "Scrambled spicy eggs", 260.0, 16.0, 4.0, 18.0, 50.0,
            tags = listOf("anda", "egg"), searchAliases = listOf("anda", "egg"),
            unit = "serving", slug = "anda-ghotala",
        ),
        DishHit(
            "dish-shami-kabab", "Classic Shami Kabab", "Lean beef & lentil", 160.0, 12.0, 6.0, 9.0, 30.0,
            tags = listOf("kabab"), unit = "piece", slug = "classic-shami-kabab",
        ),
        DishHit(
            "dish-chicken-pasanday", "Chicken Pasanday", "Rich gravy", 380.0, 28.0, 6.0, 26.0, 100.0,
            tags = listOf("chicken"), unit = "serving", slug = "chicken-pasanday",
        ),
        DishHit(
            "dish-roti", "Wholewheat Roti", "Tawa", 150.0, 4.0, 28.0, 2.0, 20.0,
            tags = listOf("roti"), unit = "piece", slug = "wholewheat-roti",
        ),
        DishHit(
            "dish-aloo-paratha", "Aloo Paratha", "Tawa pan baked", 320.0, 8.0, 42.0, 12.0, 90.0,
            tags = listOf("paratha"), unit = "piece", slug = "aloo-paratha",
        ),
        DishHit(
            "dish-plain-paratha", "Wholewheat Paratha", "Tawa pan baked", 280.0, 6.0, 36.0, 12.0, 80.0,
            tags = listOf("paratha"), unit = "piece", slug = "wholewheat-paratha",
        ),
        DishHit(
            "dish-elaichi-chai", "Elaichi Chai", "Standard brewed cup", 65.0, 2.0, 8.0, 2.0,
            tags = listOf("chai"), unit = "cup", slug = "elaichi-chai",
        ),
        DishHit(
            "dish-daal-masoor", "Daal Masoor", "1 katori", 220.0, 14.0, 32.0, 6.0, 40.0,
            tags = listOf("daal"), unit = "katori", slug = "daal-masoor",
        ),
        DishHit(
            "dish-chicken-karahi", "Chicken Karahi", "Home style", 420.0, 32.0, 8.0, 24.0, 120.0,
            tags = listOf("chicken"), unit = "serving", slug = "chicken-karahi",
        ),
        DishHit(
            "dish-mixed-sabzi", "Mixed Sabzi", "Seasonal", 160.0, 4.0, 16.0, 8.0, 50.0,
            tags = listOf("sabzi"), unit = "katori", slug = "mixed-sabzi",
        ),
    )

    fun ingredients(): List<IngredientHit> = listOf(
        IngredientHit("ing-chicken-breast", "Chicken Breast", "Raw, boneless & skinless cuts", "protein", 120.0, 25.0, 0.0, 1.8),
        IngredientHit("ing-spinach", "Fresh Spinach / Palak", "Rich in iron & folate", "veg", 23.0, 2.9, 3.6, 0.4),
        IngredientHit("ing-dahi", "Greek Yogurt / Plain Dahi", "10g protein per 100g", "dairy", 59.0, 10.0, 3.6, 0.4),
        IngredientHit("ing-tomato-onion", "Tomato & Onion Base", "Low glycemic", "veg", 45.0, 1.5, 8.0, 0.4),
        IngredientHit("ing-ghee", "Olive Oil / Desi Ghee", "Pure lipid", "fat", 884.0, 0.0, 0.0, 100.0),
        IngredientHit("ing-egg", "Whole Egg", "Farm anda", "dairy", 143.0, 13.0, 1.1, 10.0),
        IngredientHit("ing-ginger", "Ginger-Garlic & Spices", "Aromatic base", "spice", 40.0, 1.0, 8.0, 0.5),
    )

    private fun card(id: String, slot: String, name: String, items: List<CommonItem>) =
        CommonCard(templateId = id, nameEn = name, items = items, mealSlot = slot, source = "offline")

    private fun item(
        id: String,
        name: String,
        portion: Double,
        kcalEach: Int,
        unit: String,
        protein: Double,
        carbs: Double,
        fat: Double,
        addedFat: Double,
    ) = CommonItem(id, name, portion, (kcalEach * portion).roundToInt(), unit, protein, carbs, fat, addedFat)
}

fun formatCount(value: Int): String = String.format(Locale.US, "%,d", value)

fun Map<String, Any?>.fitString(key: String): String? = this[key] as? String

fun Map<String, Any?>.fitNumber(key: String): Double? = when (val value = this[key]) {
    is Number -> value.toDouble()
    is String -> value.toDoubleOrNull()
    else -> null
}

fun Map<String, Any?>.fitInt(key: String): Int? = fitNumber(key)?.roundToInt()

fun Map<String, Any?>.fitBool(key: String): Boolean = this[key] as? Boolean ?: false

@Suppress("UNCHECKED_CAST")
fun Any?.asStringKeyMap(): Map<String, Any?> {
    val raw = this as? Map<*, *> ?: return emptyMap()
    return raw.entries.mapNotNull { (key, value) ->
        (key as? String)?.let { it to value }
    }.toMap()
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.fitMap(key: String): Map<String, Any?> = this[key].asStringKeyMap()

fun Map<String, Any?>.fitStringList(key: String): List<String> =
    (this[key] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

fun localizedName(map: Map<String, Any?>): String {
    val name = map["name"]
    if (name is String) return name
    val named = name.asStringKeyMap()
    return named.fitString("en") ?: named.fitString("ur") ?: map.fitString("title") ?: "Meal"
}
