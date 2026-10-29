package com.example.aplicacinejercito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacinejercito.navigation.AppNavigation
import com.example.aplicacinejercito.ui.theme.AplicacinEjercitoTheme
import com.example.aplicacinejercito.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val factory = (application as DeportesApplication).factory
            val app: AppViewModel = viewModel(factory = factory)
            val estado by app.estado.collectAsStateWithLifecycle()
            // Una sola medición de ventana compartida por las pantallas.
            val ventana = calculateWindowSizeClass(this)
            AplicacinEjercitoTheme(darkTheme = estado.preferencias.temaOscuro) {
                val vista = LocalView.current
                val fondoBarra = if (estado.usuario == null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                val fondoNavegacion = MaterialTheme.colorScheme.surface
                SideEffect {
                    WindowCompat.getInsetsController(window, vista).apply {
                        isAppearanceLightStatusBars = fondoBarra.luminance() > 0.5f
                        isAppearanceLightNavigationBars = fondoNavegacion.luminance() > 0.5f
                    }
                }
                Surface { AppNavigation(estado, ventana.widthSizeClass, factory, app) }
            }
        }
    }
}
