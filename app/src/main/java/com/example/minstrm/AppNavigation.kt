package com.example.minstrm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Plan       : Screen("plan")
    object AddDevice  : Screen("add_device")
    object EditDevice : Screen("edit_device/{index}") {
        fun createRoute(index: Int) = "edit_device/$index"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val vm: DeviceViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Plan.route) {
        // Plan screen
        composable(Screen.Plan.route) {
            PlanScreen(
                devices = vm.devices,
                onAddDeviceClick = {
                    navController.navigate(Screen.AddDevice.route)
                },
                onEditClick = { index ->
                    navController.navigate(Screen.EditDevice.createRoute(index))
                }
            )
        }

        // Add‐new‐device screen
        composable(Screen.AddDevice.route) {
            AddDeviceScreen(
                initialDevice = null,
                onDeviceSaved = { info ->
                    vm.addDevice(info.toDevice())
                    navController.popBackStack()
                }
            )
        }

        // Edit existing device
        composable(
            route = Screen.EditDevice.route,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStack ->
            val idx = backStack.arguments!!.getInt("index")
            vm.devices.getOrNull(idx)?.let { existing ->
                AddDeviceScreen(
                    initialDevice = existing.toDeviceInfo(),
                    onDeviceSaved = { info ->
                        vm.updateDevice(idx, info.toDevice())
                        navController.popBackStack()
                    }
                )
            } ?: run {
                // invalid index ⇒ go back
                navController.popBackStack()
            }
        }
    }
}

// Helpers to convert between your two data classes
fun DeviceInfo.toDevice() = Device(
    produkt      = produkt,
    model        = model,
    effekt       = effekt,
    estimeretTid = estimeretTid
)

fun Device.toDeviceInfo() = DeviceInfo(
    produkt      = produkt,
    model        = model,
    effekt       = effekt,
    estimeretTid = estimeretTid
)
