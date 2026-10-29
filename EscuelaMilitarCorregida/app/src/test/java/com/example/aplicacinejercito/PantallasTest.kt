package com.example.aplicacinejercito

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.*
import com.example.aplicacinejercito.data.DatosDemostracion
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.navigation.Rutas
import com.example.aplicacinejercito.ui.components.*
import com.example.aplicacinejercito.ui.screens.*
import com.example.aplicacinejercito.ui.theme.AplicacinEjercitoTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class, qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PantallasTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val usuario = DatosDemostracion.usuarios.first { it.id == "vs" }
    private val datos = DatosApp(usuarios = DatosDemostracion.usuarios, canchas = DatosDemostracion.canchas,
        politicas = DatosDemostracion.politicas, eventos = DatosDemostracion.eventos(Fechas.hoy()), eventoCanchas = DatosDemostracion.eventoCanchas)
    private fun captura(nombre: String) {
        compose.waitForIdle()
        compose.runOnIdle {
            val vista = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(vista.width, vista.height, Bitmap.Config.ARGB_8888)
            vista.draw(Canvas(bitmap))
            val archivo = File("build/capturas-pruebas/$nombre.png")
            archivo.parentFile?.mkdirs()
            archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    @Test fun nuevaReservaMuestraBloquesYNoPideParticipantes() {
        compose.setContent { AplicacinEjercitoTheme { Surface {
            NuevaReservaScreen(ReservaFormulario(fecha = Fechas.hoy().plusDays(1).toString()), datos, DatosDemostracion.politicas.first(), {}, {})
        } } }
        compose.onNodeWithText("Cantidad de participantes").assertDoesNotExist()
        compose.onNodeWithText("08:00 – 09:00").assertExists()
        compose.onNodeWithText("17:00 – 18:00").assertExists()
        captura("nueva-reserva")
    }
    @Test fun pendienteSoloPermiteAprobarORechazarSinMotivos() {
        var rechazada = false
        val reserva = ReservaModel("r", "vs", "pista", Fechas.hoy().plusDays(1).toString(), 9, EstadoReserva.PENDIENTE, Fechas.semana(Fechas.hoy()))
        compose.setContent { AplicacinEjercitoTheme { Surface {
            DetalleReservaScreen(reserva, datos, true, false, {}, {}, { rechazada = true })
        } } }
        compose.onNodeWithText("Cancelar reserva").assertDoesNotExist()
        compose.onNodeWithText("Modificar fecha y hora").assertDoesNotExist()
        compose.onNodeWithText("Rechazar solicitud").performClick()
        assertTrue(rechazada)
        compose.onNodeWithText("Motivo obligatorio").assertDoesNotExist()
        captura("solicitud-pendiente")
    }
    @Test fun barraInferiorRegresaALaRaizDeCadaSeccion() {
        compose.setContent {
            AplicacinEjercitoTheme {
                val nav = rememberNavController()
                AppScaffold(nav, AppUiState(usuario = usuario, datos = datos), false, false, SnackbarHostState()) {
                    NavHost(nav, Rutas.INICIO) {
                        composable(Rutas.INICIO) { Text("Raíz de inicio") }
                        composable(Rutas.EVENTOS) { Column { Text("Raíz de eventos"); Button(onClick = { nav.navigate(Rutas.detalleEvento("e1")) }) { Text("Abrir detalle de prueba") } } }
                        composable(Rutas.DETALLE_EVENTO) { Text("Contenido del detalle") }
                        composable(Rutas.RESERVAS) { Text("Raíz de reservas") }
                        composable(Rutas.CALENDARIO) { Text("Raíz de agenda") }
                        composable(Rutas.PERFIL) { Text("Raíz de perfil") }
                    }
                }
            }
        }
        compose.onNodeWithText("Eventos", substring = false).performClick()
        compose.onNodeWithText("Abrir detalle de prueba").performClick()
        compose.onNodeWithText("Contenido del detalle").assertIsDisplayed()
        compose.onNodeWithText("Reservas", substring = false).performClick()
        compose.onNodeWithText("Eventos", substring = false).performClick()
        compose.onNodeWithText("Raíz de eventos").assertIsDisplayed()
        compose.onNodeWithText("Contenido del detalle").assertDoesNotExist()
    }
    @Test fun inicioOrdenaEventosYAbreElEventoElegido() {
        var eventoId = ""
        compose.setContent { AplicacinEjercitoTheme {
            val proxima = ReservaModel("proxima", "vs", "pista", Fechas.hoy().plusDays(1).toString(), 9, EstadoReserva.CONFIRMADA, Fechas.semana(Fechas.hoy()))
            val estado = AppUiState(usuario = usuario, datos = datos.copy(reservas = listOf(proxima)))
            val nav = rememberNavController()
            AppScaffold(nav, estado, false, false, SnackbarHostState()) {
                CadeteScreen(estado, {}, {}, {}, { eventoId = it }, {})
            }
        } }
        compose.onNodeWithText("Campeonato interescuelas de atletismo").performScrollTo().performClick()
        assertEquals("e2", eventoId)
        compose.onNodeWithText("Tienes 2 de 3 tokens disponibles esta semana").performScrollTo()
        captura("inicio-eventos")
    }
    @Test fun reportesMuestranDatosFiltradosYGraficos() {
        val r = ReservaModel("r", "vs", "pista", Fechas.hoy().toString(), 9, EstadoReserva.CONFIRMADA, Fechas.semana(Fechas.hoy()))
        compose.setContent { AplicacinEjercitoTheme { Surface { ReportesScreen(datos.copy(reservas = listOf(r, r.copy(id = "r2", usuarioId = "dm", canchaId = "estadio", estado = EstadoReserva.PENDIENTE), r.copy(id = "r3", usuarioId = "ml", canchaId = "tenis"))), ReporteFiltros(), {}) } } }
        compose.onNodeWithText("Distribución por estado").performScrollTo().assertIsDisplayed()
        captura("reportes")
        compose.onNodeWithText("Instalaciones con más solicitudes").performScrollTo().assertIsDisplayed()
        captura("reportes-instalaciones")
    }
    @Test fun administradorSeleccionaBloquesIndividuales() {
        compose.setContent { AplicacinEjercitoTheme { Surface {
            var formulario by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(CanchaFormulario(nombre = "Cancha de Tenis N.° 1", tipo = "Cancha de tenis")) }
            EditarCanchaScreen(formulario, AccionUiState(), { cambio -> formulario = cambio(formulario) }, {})
        } } }
        compose.onNodeWithText("11:00 – 12:00").performScrollTo().performClick().assertIsNotSelected()
        compose.onNodeWithText("17:00 – 18:00").assertIsSelected()
        captura("horarios-instalacion")
    }
    @Test fun instalacionSePuedeFiltrarPorEstado() {
        val prueba = datos.copy(canchas = listOf(datos.canchas.first(), datos.canchas[1].copy(estado = EstadoCancha.MANTENIMIENTO)))
        compose.setContent { AplicacinEjercitoTheme { Surface { InstalacionesScreen(prueba, {}, {}) } } }
        compose.onNodeWithText("Todos").performClick()
        val opciones = compose.onAllNodesWithText("Mantenimiento")
        opciones[opciones.fetchSemanticsNodes().lastIndex].performClick()
        compose.onNodeWithText("Pista Atlética").assertDoesNotExist()
        compose.onNodeWithText("Sala de Musculación").assertExists()
    }
}
