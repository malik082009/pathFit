package com.grittech.pathfit.model

data class OnboardingDraft(
    val onboardingStep: String = "about_you",
    val displayName: String = "",
    val countryCode: String = "PK",
    val regionKey: String? = "pk-punjab",
    val locationLabel: String = "Lahore, Punjab",
    val dateOfBirth: String = "1998-05-14",
    val sexAtBirth: String = "male",
    val heightCm: Int = 178,
    val heightUnit: String = "cm",
    val weightKg: Double = 78.5,
    val targetWeightKg: Double = 72.0,
    val activityLevel: String = "moderate",
    val goal: String = "lose",
    val weeklyPaceKg: Double = 0.5,
    val allergenFlags: List<String> = emptyList(),
    val dietTypes: List<String> = emptyList(),
    val conditionKeys: List<String> = emptyList(),
    val timezone: String = "Asia/Karachi",
    val schemaVersion: Int = 1,
)

data class RegionOption(
    val label: String,
    val countryCode: String,
    val regionKey: String?,
)

data class Targets(
    val status: String = "incomplete",
    val blockReasons: List<String> = emptyList(),
    val calorieTarget: Int? = null,
    val rawCalorieTarget: Int? = null,
    val floorApplied: Boolean = false,
    val adjustments: List<String> = emptyList(),
    val goalEffective: String? = null,
    val goalRequested: String? = null,
    val proteinG: Int? = null,
    val carbsG: Int? = null,
    val fatG: Int? = null,
    val proteinPct: Int? = null,
    val carbsPct: Int? = null,
    val fatPct: Int? = null,
    val bmr: Double? = null,
    val tdee: Double? = null,
    val activityMultiplier: Double? = null,
    val goalDelta: Double? = null,
    val appliedGoalDelta: Double? = null,
    val weeklyPaceKg: Double? = null,
    val method: String? = null,
    val clinicalPending: Boolean = false,
)

data class PreviewUi(
    val active: Boolean,
    val blockMessage: String?,
    val heroKcal: Int?,
    val subtitle: String,
    val bmr: Int?,
    val tdee: Int?,
    val delta: Int?,
    val deltaLabel: String,
    val deltaCaption: String,
    val proteinG: Int?,
    val carbsG: Int?,
    val fatG: Int?,
    val proteinPct: Int,
    val carbsPct: Int,
    val fatPct: Int,
    val clinicalPending: Boolean,
    val floorCapped: Boolean,
)

data class BootstrapResult(
    val uid: String,
    val onboardingStatus: String,
    val subscriptionStatus: String,
)

data class CommonItem(
    val dishId: String,
    val nameEn: String,
    val defaultPortion: Double,
    val estimateKcal: Int,
    val unit: String,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val addedFatKcal: Double = 0.0,
)

data class CommonCard(
    val templateId: String,
    val nameEn: String,
    val nameUr: String? = null,
    val items: List<CommonItem>,
    val mealSlot: String,
    val source: String = "country",
)

data class DishHit(
    val dishId: String,
    val nameEn: String,
    val detail: String,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val addedFatKcal: Double = 0.0,
    val tags: List<String> = emptyList(),
    val searchAliases: List<String> = emptyList(),
    val preps: List<String> = emptyList(),
    val unit: String = "serving",
    val slug: String = "",
)

data class IngredientHit(
    val id: String,
    val nameEn: String,
    val detail: String,
    val category: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
)

data class PlateItem(
    val key: String,
    val dishId: String? = null,
    val userRecipeId: String? = null,
    val name: String,
    val detail: String = "",
    val portion: Double = 1.0,
    val unit: String = "serving",
    val kcalPerServing: Double = 0.0,
    val proteinPer: Double = 0.0,
    val carbsPer: Double = 0.0,
    val fatPer: Double = 0.0,
    val addedFatKcal: Double = 0.0,
    val oilLevel: String? = null,
    val prep: String? = null,
)

data class CustomMealDraft(
    val mealSlot: String = "breakfast",
    val name: String = "Custom Nashta Plate",
    val items: List<PlateItem> = emptyList(),
)

data class RecipeLine(
    val ingredientId: String,
    val name: String,
    val grams: Double,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
)

data class RecipeDraft(
    val name: String = "",
    val mealSlot: String = "lunch",
    val lines: List<RecipeLine> = emptyList(),
)

data class UsualItem(
    val dishId: String? = null,
    val userRecipeId: String? = null,
    val name: String = "",
    val portion: Double = 1.0,
    val oilLevel: String? = null,
    val unit: String = "serving",
    val count: Double = 1.0,
    val kcalPerServing: Double = 0.0,
    val proteinPer: Double = 0.0,
    val carbsPer: Double = 0.0,
    val fatPer: Double = 0.0,
    val addedFatKcal: Double = 0.0,
)

data class UsualMeal(
    val id: String,
    val mealSlot: String,
    val name: String,
    val items: List<UsualItem>,
    val pinned: Boolean,
    val status: String,
    val lastUsedAt: Long,
    val source: String,
    val templateId: String?,
)

data class MealLog(
    val id: String,
    val dayKey: String,
    val mealSlot: String,
    val status: String,
    val title: String,
    val kcal: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val loggedAt: Long,
    val deleted: Boolean,
    val usualMealId: String?,
)

data class MacroTotals(
    val kcal: Int = 0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
)

enum class HomeTab { Today, Diary, Usuals, Profile }

enum class PickerMode { AddToPlate, LogOnce, AddToRecipe }
