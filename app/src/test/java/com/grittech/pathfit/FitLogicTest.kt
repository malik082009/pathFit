package com.grittech.pathfit

import com.grittech.pathfit.domain.CalorieMath
import com.grittech.pathfit.domain.DayClock
import com.grittech.pathfit.domain.EnergyInput
import com.grittech.pathfit.domain.PreviewLogic
import com.grittech.pathfit.model.Targets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitLogicTest {
    @Test
    fun losePaceSubtitleUsesEffectiveGoal() {
        val ui = PreviewLogic.bindPreview(
            Targets(
                status = "active",
                calorieTarget = 2209,
                goalEffective = "lose",
                weeklyPaceKg = 0.5,
                appliedGoalDelta = -550.0,
                bmr = 1780.0,
                tdee = 2759.0,
                proteinG = 166,
                carbsG = 221,
                fatG = 74,
                proteinPct = 30,
                carbsPct = 40,
                fatPct = 30,
            ),
        )
        assertEquals("Target intake for 0.5 kg/week steady fat loss", ui.subtitle)
        assertEquals("DEFICIT", ui.deltaLabel)
        assertEquals(-550, ui.delta)
        assertEquals(2209, ui.heroKcal)
    }

    @Test
    fun surplusLabelWhenDeltaPositive() {
        val ui = PreviewLogic.bindPreview(
            Targets(
                status = "active",
                calorieTarget = 2800,
                goalEffective = "build_muscle",
                weeklyPaceKg = 0.25,
                appliedGoalDelta = 300.0,
            ),
        )
        assertEquals("SURPLUS", ui.deltaLabel)
        assertTrue(ui.subtitle.contains("lean hypertrophy"))
    }

    @Test
    fun blockedProfileHidesCalories() {
        val ui = PreviewLogic.bindPreview(
            Targets(status = "blocked", blockReasons = listOf("under_18"), calorieTarget = 2150),
        )
        assertEquals(null, ui.heroKcal)
        assertTrue(ui.blockMessage!!.contains("18"))
    }

    @Test
    fun oilScalesAddedFatOnly() {
        val normal = CalorieMath.computeItemEnergy(EnergyInput(200.0, 126.0, 1.0, "normal"))
        val light = CalorieMath.computeItemEnergy(EnergyInput(200.0, 126.0, 1.0, "light"))
        val none = CalorieMath.computeItemEnergy(EnergyInput(200.0, 126.0, 1.0, null))
        assertEquals(326.0, normal, 0.1)
        assertTrue(light < normal)
        assertEquals(200.0, none, 0.1)
    }

    @Test
    fun slotFollowsLocalHour() {
        assertEquals("breakfast", DayClock.defaultSlot(7))
        assertEquals("lunch", DayClock.defaultSlot(13))
        assertEquals("dinner", DayClock.defaultSlot(18))
        assertEquals("snack", DayClock.defaultSlot(22))
    }

    @Test
    fun headerLabelSurvivesBlankDayKey() {
        val label = DayClock.headerLabel("", "Asia/Karachi")
        assertTrue(label.startsWith("Today"))
    }
}
