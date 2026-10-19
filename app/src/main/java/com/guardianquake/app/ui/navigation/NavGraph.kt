package com.guardianquake.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.guardianquake.app.ui.auth.LoginScreen
import com.guardianquake.app.ui.auth.RegistroScreen
import com.guardianquake.app.ui.contactos.ContactosScreen
import com.guardianquake.app.ui.contenido.ContenidoDetalleScreen
import com.guardianquake.app.ui.contenido.ContenidoScreen
import com.guardianquake.app.ui.dashboard.DashboardScreen
import com.guardianquake.app.ui.historial.HistorialScreen
import com.guardianquake.app.ui.sonido.SonidoAlarmaScreen
import com.guardianquake.app.ui.suscripcion.SuscripcionScreen

@Composable
fun NavGraph(navController: NavHostController, destinoInicial: String) {
    NavHost(navController = navController, startDestination = destinoInicial) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Screen.Registro.route) }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { navController.popBackStack() },
                onVolverALogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onIrAHistorial = { navController.navigate(Screen.Historial.route) },
                onIrASonidoAlarma = { navController.navigate(Screen.SonidoAlarma.route) },
                onIrAContenido = { navController.navigate(Screen.Contenido.route) },
                onIrASuscripcion = { navController.navigate(Screen.Suscripcion.route) },
                onIrAContactos = { navController.navigate(Screen.Contactos.route) }
            )
        }

        composable(Screen.Historial.route) {
            HistorialScreen(onVolver = { navController.popBackStack() })
        }

        composable(Screen.SonidoAlarma.route) {
            SonidoAlarmaScreen(onVolver = { navController.popBackStack() })
        }

        composable(Screen.Contenido.route) {
            ContenidoScreen(
                onVolver = { navController.popBackStack() },
                onAbrirDetalle = { id -> navController.navigate(Screen.ContenidoDetalle.crearRuta(id)) }
            )
        }

        composable(
            route = Screen.ContenidoDetalle.route,
            arguments = listOf(navArgument("idContenido") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("idContenido") ?: 0
            ContenidoDetalleScreen(idContenido = id, onVolver = { navController.popBackStack() })
        }

        composable(Screen.Suscripcion.route) {
            SuscripcionScreen(onVolver = { navController.popBackStack() })
        }

        composable(Screen.Contactos.route) {
            ContactosScreen(onVolver = { navController.popBackStack() })
        }
    }
}
