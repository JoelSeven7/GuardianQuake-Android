package com.guardianquake.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registro : Screen("registro")
    object Dashboard : Screen("dashboard")
    object Historial : Screen("historial")
    object SonidoAlarma : Screen("sonido-alarma")
    object Contenido : Screen("contenido")
    object ContenidoDetalle : Screen("contenido/{idContenido}") {
        fun crearRuta(idContenido: Int) = "contenido/$idContenido"
    }
    object Suscripcion : Screen("suscripcion")
    object Contactos : Screen("contactos")
}
