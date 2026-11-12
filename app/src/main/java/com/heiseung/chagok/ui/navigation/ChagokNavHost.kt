package com.heiseung.chagok.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heiseung.chagok.data.repository.AppContainer
import com.heiseung.chagok.ui.arrival.ArrivalScreen
import com.heiseung.chagok.ui.arrival.ArrivalViewModel
import com.heiseung.chagok.ui.boarding.BoardingScreen
import com.heiseung.chagok.ui.boarding.BoardingViewModel
import com.heiseung.chagok.ui.components.ChagokFrame
import com.heiseung.chagok.ui.components.MainTab
import com.heiseung.chagok.ui.handover.HandoverScreen
import com.heiseung.chagok.ui.handover.HandoverViewModel
import com.heiseung.chagok.ui.placeholder.PlaceholderScreen
import com.heiseung.chagok.ui.family.AddPersonScreen
import com.heiseung.chagok.ui.family.AddPersonViewModel
import com.heiseung.chagok.ui.family.FamilyScreen
import com.heiseung.chagok.ui.family.FamilyViewModel
import com.heiseung.chagok.ui.history.HistoryScreen
import com.heiseung.chagok.ui.history.HistoryViewModel
import com.heiseung.chagok.ui.settle.SettleScreen
import com.heiseung.chagok.ui.settle.SettleViewModel
import com.heiseung.chagok.vehicle.FakeVehicleDataSource
import com.heiseung.chagok.vehicle.TripEvent

object Routes {
    const val BOARDING = "boarding"
    const val HOME = "home"
    const val ARRIVAL = "arrival/{tripId}"
    const val HISTORY = "history"
    const val SETTLE = "settle"
    const val FAMILY = "family"
    const val ADD_PERSON = "family/add"
    const val CHARGER = "charger"
    fun arrival(tripId: Long) = "arrival/$tripId"
}

@Composable
fun ChagokNavHost(container: AppContainer, nav: NavHostController = rememberNavController()) {
    val snapshot by container.vehicleDataSource.snapshot.collectAsState()
    val driver by container.session.driver.collectAsState()

    // 다시 P단이면 03 하차 화면으로
    LaunchedEffect(Unit) {
        container.tripWatcher.events.collect { event ->
            if (event is TripEvent.Ended) nav.navigate(Routes.arrival(event.tripId)) { launchSingleTop = true }
        }
    }

    // 가짜 차일 때만 상단에 [출발] [도착] [시트 ±] 테스트 버튼
    val fake = container.vehicleDataSource as? FakeVehicleDataSource
    val debug = if (fake == null) emptyList() else listOf(
        "시트 -10" to { fake.setSeatPosition(snapshot.seatPosition - 10) },
        "시트 +10" to { fake.setSeatPosition(snapshot.seatPosition + 10) },
        "출발" to { fake.drive() },
        "도착" to { fake.park() }
    )

    val onTab: (MainTab) -> Unit = { tab ->
        val route = when (tab) {
            MainTab.HOME -> Routes.HOME
            MainTab.HISTORY -> Routes.HISTORY
            MainTab.SETTLE -> Routes.SETTLE
            MainTab.FAMILY -> Routes.FAMILY
        }
        nav.navigate(route) {
            popUpTo(Routes.HOME)
            launchSingleTop = true
        }
    }

    NavHost(navController = nav, startDestination = Routes.BOARDING) {

        composable(Routes.BOARDING) {
            val vm: BoardingViewModel = viewModel { BoardingViewModel(container) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, null, currentTab = null, onTab = onTab, debugActions = debug) {
                BoardingScreen(state, vm, onConfirmed = {
                    nav.navigate(Routes.HOME) { popUpTo(Routes.BOARDING) { inclusive = true } }
                })
            }
        }

        composable(Routes.HOME) {
            val vm: HandoverViewModel = viewModel { HandoverViewModel(container) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, driver?.name, MainTab.HOME, onTab, debugActions = debug) {
                HandoverScreen(state, snapshot, vm.lowEnergyPct, onConfirm = vm::confirm, onCharger = { nav.navigate(Routes.CHARGER) })
            }
        }

        composable(
            Routes.ARRIVAL,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { entry ->
            val tripId = entry.arguments?.getLong("tripId") ?: 0L
            val vm: ArrivalViewModel = viewModel(key = "arrival-$tripId") { ArrivalViewModel(container, tripId) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, driver?.name, currentTab = null, onTab = onTab, debugActions = debug) {
                ArrivalScreen(state, vm, onDone = {
                    // 내리면 다음 사람을 위해 탑승 화면으로
                    nav.navigate(Routes.BOARDING) { popUpTo(nav.graph.id) { inclusive = true } }
                })
            }
        }

        composable(Routes.HISTORY) {
            val vm: HistoryViewModel = viewModel { HistoryViewModel(container) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, driver?.name, MainTab.HISTORY, onTab, debugActions = debug) {
                HistoryScreen(state, vm)
            }
        }
        composable(Routes.SETTLE) {
            val vm: SettleViewModel = viewModel { SettleViewModel(container) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, driver?.name, MainTab.SETTLE, onTab, debugActions = debug) {
                SettleScreen(state, vm)
            }
        }
        composable(Routes.FAMILY) {
            val vm: FamilyViewModel = viewModel { FamilyViewModel(container) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, driver?.name, MainTab.FAMILY, onTab, debugActions = debug) {
                FamilyScreen(state, vm, onAddPerson = { nav.navigate(Routes.ADD_PERSON) })
            }
        }
        composable(Routes.ADD_PERSON) {
            val vm: AddPersonViewModel = viewModel { AddPersonViewModel(container) }
            val state by vm.state.collectAsState()
            ChagokFrame(snapshot.gear, driver?.name, MainTab.FAMILY, onTab, onBack = { nav.popBackStack() }, debugActions = debug) {
                AddPersonScreen(state, vm, onDone = { nav.popBackStack() })
            }
        }
        composable(Routes.CHARGER) {
            ChagokFrame(snapshot.gear, driver?.name, null, onTab, onBack = { nav.popBackStack() }) { PlaceholderScreen("02-1 근처 충전소") }
        }
    }
}
