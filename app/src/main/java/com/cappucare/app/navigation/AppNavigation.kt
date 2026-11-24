package com.cappucare.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cappucare.app.ui.screen.LoginScreen
import com.cappucare.app.ui.screen.PanelScreen
import com.cappucare.app.ui.screen.movimientos.HistorialMovimientosScreen
import com.cappucare.app.ui.screen.movimientos.RegistrarMovimientoScreen
import com.cappucare.app.ui.screen.productos.ProductoDetalleScreen
import com.cappucare.app.ui.screen.productos.ProductoFormScreen
import com.cappucare.app.ui.screen.productos.ProductosScreen
import com.cappucare.app.ui.screen.reposicion.ReposicionScreen
import com.cappucare.app.viewmodel.CappucareViewModel

object Rutas {
    const val LOGIN = "login"
    const val PANEL = "panel"
    const val PRODUCTOS = "productos"
    const val PRODUCTO_FORM = "producto_form"
    const val PRODUCTO_DETALLE = "producto_detalle"
    const val MOVIMIENTO = "movimiento"
    const val HISTORIAL = "historial"
    const val REPOSICION = "reposicion"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: CappucareViewModel = viewModel(
        factory = CappucareViewModel.factory(LocalContext.current)
    )
) {
    val cerrarSesion: () -> Unit = {
        viewModel.logout()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    val irAlPanel: () -> Unit = {
        navController.navigate(Rutas.PANEL) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(viewModel = viewModel, onIngresar = irAlPanel)
        }
        composable(Rutas.PANEL) {
            PanelScreen(navController = navController, viewModel = viewModel, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.PRODUCTOS) {
            ProductosScreen(navController = navController, viewModel = viewModel)
        }
        composable(Rutas.PRODUCTO_FORM) {
            ProductoFormScreen(navController = navController, viewModel = viewModel)
        }
        composable(Rutas.PRODUCTO_DETALLE) {
            ProductoDetalleScreen(navController = navController, viewModel = viewModel)
        }
        composable(Rutas.MOVIMIENTO) {
            RegistrarMovimientoScreen(navController = navController, viewModel = viewModel)
        }
        composable(Rutas.HISTORIAL) {
            HistorialMovimientosScreen(navController = navController, viewModel = viewModel)
        }
        composable(Rutas.REPOSICION) {
            ReposicionScreen(navController = navController, viewModel = viewModel)
        }
    }
}