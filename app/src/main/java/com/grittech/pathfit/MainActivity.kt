package com.grittech.pathfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.grittech.pathfit.ui.NavCommand
import com.grittech.pathfit.ui.Routes
import com.grittech.pathfit.ui.components.rememberFitViewModel
import com.grittech.pathfit.ui.screens.AboutScreen
import com.grittech.pathfit.ui.screens.ActivityScreen
import com.grittech.pathfit.ui.screens.BiometricsScreen
import com.grittech.pathfit.ui.screens.BuildMealScreen
import com.grittech.pathfit.ui.screens.DietScreen
import com.grittech.pathfit.ui.screens.DishPickerScreen
import com.grittech.pathfit.ui.screens.HabitsLadderScreen
import com.grittech.pathfit.ui.screens.HomeScreen
import com.grittech.pathfit.ui.screens.IngredientScreen
import com.grittech.pathfit.ui.screens.PaceScreen
import com.grittech.pathfit.ui.screens.PreviewScreen
import com.grittech.pathfit.ui.screens.QuickLogScreen
import com.grittech.pathfit.ui.screens.RecipeScreen
import com.grittech.pathfit.ui.screens.SignInScreen
import com.grittech.pathfit.ui.screens.SplashScreen
import com.grittech.pathfit.ui.screens.StaplesScreen
import com.grittech.pathfit.ui.screens.StatsScreen
import com.grittech.pathfit.ui.theme.FitPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitPathTheme {
                val vm = rememberFitViewModel()
                val nav = rememberNavController()
                LaunchedEffect(vm) {
                    vm.nav.collect { command ->
                        when (command) {
                            is NavCommand.To -> nav.navigate(command.route) {
                                launchSingleTop = true
                                if (command.clear) {
                                    popUpTo(nav.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }
                }
                NavHost(navController = nav, startDestination = Routes.Splash) {
                    composable(Routes.Splash) { SplashScreen() }
                    composable(Routes.SignIn) { SignInScreen() }
                    composable(Routes.About) { AboutScreen(onNext = { nav.navigate(Routes.Biometrics) }) }
                    composable(Routes.Biometrics) {
                        BiometricsScreen(
                            onNext = { nav.navigate(Routes.Activity) },
                            onBack = { nav.popBackStack() },
                        )
                    }
                    composable(Routes.Activity) {
                        ActivityScreen(
                            onNext = { nav.navigate(Routes.Pace) },
                            onBack = { nav.popBackStack() },
                        )
                    }
                    composable(Routes.Pace) {
                        PaceScreen(
                            onNext = { nav.navigate(Routes.Diet) },
                            onBack = { nav.popBackStack() },
                        )
                    }
                    composable(Routes.Diet) { DietScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Preview) { PreviewScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Staples) { StaplesScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.BuildMeal) { BuildMealScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Dishes) { DishPickerScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Ingredients) { IngredientScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Recipe) { RecipeScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Home) {
                        HomeScreen(
                            onOpenStaples = { nav.navigate(Routes.Staples) },
                            onOpenQuickLog = { slot -> vm.openQuickLog(slot) },
                            onOpenStats = { vm.openStats() },
                            onOpenHabits = { vm.openHabits() },
                            onOpenBuildMeal = { nav.navigate(Routes.BuildMeal) },
                        )
                    }
                    composable(Routes.QuickLog) {
                        QuickLogScreen(
                            onBack = { nav.popBackStack() },
                            onBuildMeal = { nav.navigate(Routes.BuildMeal) },
                        )
                    }
                    composable(Routes.Stats) { StatsScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Habits) {
                        HabitsLadderScreen(
                            onBack = { nav.popBackStack() },
                            onLogMeal = {
                                nav.popBackStack()
                                vm.openQuickLog(vm.state.value.selectedSlot.ifBlank { "lunch" })
                            },
                        )
                    }
                }
            }
        }
    }
}
