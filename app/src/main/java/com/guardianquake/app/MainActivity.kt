package com.guardianquake.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.guardianquake.app.ui.navigation.NavGraph
import com.guardianquake.app.ui.navigation.Screen
import com.guardianquake.app.ui.theme.GuardianQuakeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GuardianQuakeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val app = applicationContext as GuardianQuakeApp
                    var destinoInicial by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val haySesion = app.authRepository.hayTokenGuardado()
                        destinoInicial = if (haySesion) Screen.Dashboard.route else Screen.Login.route
                    }

                    val destino = destinoInicial
                    if (destino == null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        NavGraph(navController = navController, destinoInicial = destino)
                    }
                }
            }
        }
    }
}
