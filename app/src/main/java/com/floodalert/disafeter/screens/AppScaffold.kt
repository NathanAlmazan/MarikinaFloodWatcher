package com.floodalert.disafeter.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.floodalert.disafeter.MainViewModel
import com.floodalert.disafeter.R
import com.floodalert.disafeter.screens.authentication.LoginScreen
import com.floodalert.disafeter.screens.authentication.RegisterScreen
import com.floodalert.disafeter.screens.bulletin.BulletinFormScreen
import com.floodalert.disafeter.screens.bulletin.BulletinScreen
import com.floodalert.disafeter.screens.bulletin.BulletinViewModel
import com.floodalert.disafeter.screens.emergency.DirectoryScreen
import com.floodalert.disafeter.screens.emergency.EmergencyScreen
import com.floodalert.disafeter.screens.emergency.EmergencyViewModel
import com.floodalert.disafeter.screens.evacuate.EvacuateFormScreen
import com.floodalert.disafeter.screens.evacuate.EvacuateMapScreen
import com.floodalert.disafeter.screens.evacuate.EvacuateScreen
import com.floodalert.disafeter.screens.evacuate.EvacuateViewModel
import com.floodalert.disafeter.screens.profile.AdminPanel
import com.floodalert.disafeter.screens.profile.ProfileScreen
import com.floodalert.disafeter.screens.weather.FloodHistoryScreen
import com.floodalert.disafeter.screens.weather.WeatherScreen
import com.floodalert.disafeter.screens.weather.WeatherViewModel

sealed class Screen(val route: String, val title: String, @DrawableRes val icon: Int) {
    data object Weather : Screen("weatherScreen", "Weather", R.drawable.ic_weather_partly_cloudy)
    data object FloodHistory : Screen("floodHistory", "History", R.drawable.ic_weather_partly_cloudy)
    data object Evacuate : Screen("evacuateScreen", "Evacuate", R.drawable.ic_run_fast)
    data object Bulletin : Screen("bulletinScreen", "Bulletin", R.drawable.ic_view_dashboard)
    data object BulletinForm : Screen("bulletinFormScreen", "BulletinForm", R.drawable.ic_view_dashboard)
    data object EvacuateMap: Screen("evacuateMap", "EvacuateMap", R.drawable.ic_run_fast)
    data object EvacuateForm: Screen("evacuateFormScreen", "EvacuateForm", R.drawable.ic_run_fast)
    data object Emergency: Screen("emergencyScreen", "Emergency", R.drawable.ic_ambulance)
    data object Directory: Screen("directoryScreen", "Directory", R.drawable.ic_ambulance)
    data object Profile: Screen("profileScreen", "Profile", R.drawable.ic_baseline_account_circle_24)
    data object AdminPanel: Screen("adminPanelScreen", "AdminPanel", R.drawable.ic_baseline_account_circle_24)
    data object Register: Screen("registerScreen", "Register", R.drawable.ic_email)
    data object Login: Screen("loginScreen", "Login", R.drawable.ic_email)
}

@Composable
fun AppScaffold(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val darkMode = isSystemInDarkTheme()
    val navigationList = listOf(
        Screen.Weather,
        Screen.Evacuate,
        Screen.Emergency,
        Screen.Bulletin,
        Screen.Profile
    )

    val currentScreen = mainViewModel.currentScreen

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "Missing view model owner."
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            Color.Transparent,
            darkIcons = darkMode)
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.systemBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        ),
        bottomBar = {
            if (!mainViewModel.hideNavbar) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    navigationList.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(painterResource(screen.icon), contentDescription = null) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                                mainViewModel.setScreen(screen.title)
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!mainViewModel.hideActionButton) {
                FloatingActionButton(
                    onClick = {
                        when (currentScreen) {
                            Screen.Emergency.title -> {
                                mainViewModel.hideActionButton = true
                                navController.navigate(Screen.Directory.route)
                            }
                            Screen.Evacuate.title -> {
                                mainViewModel.hideActionButton = true
                                navController.navigate(Screen.EvacuateForm.route)
                            }
                            Screen.Bulletin.title -> {
                                mainViewModel.hideActionButton = true
                                navController.navigate(Screen.BulletinForm.route)
                            }
                            Screen.Profile.title -> {
                                mainViewModel.hideActionButton = true
                                navController.navigate(Screen.AdminPanel.route)
                            }
                            else -> navController.navigate(Screen.Directory.route)
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        AppNavHost(modifier = Modifier.padding(it), navController, mainViewModel, viewModelStoreOwner)
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModelStoreOwner: ViewModelStoreOwner,
    startDestination: String = if (mainViewModel.isLoggedIn) Screen.Weather.route else Screen.Register.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Weather.route) {
            val weatherViewModel: WeatherViewModel = viewModel(
                factory = WeatherViewModel.Factory,
                viewModelStoreOwner = viewModelStoreOwner
            )

            WeatherScreen(
                viewModel = weatherViewModel,
                mainViewModel = mainViewModel,
                onNavigateToEvacuate = {
                    navController.navigate(Screen.Evacuate.route)
                }
            )
        }

        composable(Screen.FloodHistory.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                FloodHistoryScreen(
                    viewModel = viewModel(),
                    onNavigateToWeather = {
                        navController.navigate(Screen.Weather.route)
                    }
                )
            }
        }

        composable(Screen.Evacuate.route) {
            val evacuateViewModel: EvacuateViewModel = viewModel(
                factory = EvacuateViewModel.Factory,
                viewModelStoreOwner = viewModelStoreOwner
            )

            EvacuateScreen(
                viewModel = evacuateViewModel,
                mainViewModel = mainViewModel,
                onNavigateToMap = {
                    navController.navigate(Screen.EvacuateMap.route)
                    mainViewModel.hideActionButton = true
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.EvacuateForm.route)
                    mainViewModel.hideActionButton = true
                }
            )
        }

        composable(Screen.EvacuateMap.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                EvacuateMapScreen(
                    mainViewModel = mainViewModel,
                    viewModel = viewModel(),
                    onNavigateToMain = {
                        navController.navigate(Screen.Evacuate.route)
                    }
                )
            }
        }

        composable(Screen.EvacuateForm.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                EvacuateFormScreen(
                    viewModel = viewModel(),
                    onNavigateToMain = {
                        navController.navigate(Screen.Evacuate.route)
                    }
                )
            }
        }

        composable(Screen.Emergency.route) {
            val emergencyViewModel: EmergencyViewModel = viewModel(
                factory = EmergencyViewModel.Factory,
                viewModelStoreOwner = viewModelStoreOwner
            )

            EmergencyScreen(
                viewModel = emergencyViewModel,
                mainViewModel = mainViewModel,
                onNavigateToDirectory = {
                    mainViewModel.hideActionButton = true
                    navController.navigate(Screen.Directory.route)
                }
            )
        }

        composable(Screen.Directory.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                DirectoryScreen(
                    viewModel = viewModel(),
                    onNavigateToEmergency = {
                        navController.navigate(Screen.Emergency.route)
                    }
                )
            }
        }

        composable(Screen.Bulletin.route) {
            val bulletinViewModel: BulletinViewModel = viewModel(
                factory = BulletinViewModel.Factory,
                viewModelStoreOwner = viewModelStoreOwner
            )

            BulletinScreen(
                viewModel = bulletinViewModel,
                mainViewModel = mainViewModel,
                onNavigateToForm = {
                    navController.navigate(Screen.BulletinForm.route)
                    mainViewModel.hideActionButton = true
                }
            )
        }

        composable(Screen.BulletinForm.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                BulletinFormScreen(
                    viewModel = viewModel(),
                    onNavigateToBulletin = { navController.navigate(Screen.Bulletin.route) }
                )
            }
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToHome = {
                    mainViewModel.getCurrentUser()
                    navController.navigate(Screen.Weather.route)
                    mainViewModel.setScreen(Screen.Weather.title)
                },
                mainViewModel = mainViewModel,
                onNavigateToLogIn = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    mainViewModel.getCurrentUser()
                    navController.navigate(Screen.Weather.route)
                    mainViewModel.setScreen(Screen.Weather.title)
                },
                mainViewModel = mainViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                mainViewModel = mainViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanel(
                mainViewModel = mainViewModel,
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
    }
}