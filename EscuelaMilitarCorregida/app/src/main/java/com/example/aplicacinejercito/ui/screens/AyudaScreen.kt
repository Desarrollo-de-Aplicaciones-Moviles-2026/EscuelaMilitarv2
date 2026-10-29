package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun AyudaScreen(politicas: List<PoliticaModel>) {
    Pagina {
        Text("Ayuda y reglas de uso", style = MaterialTheme.typography.headlineSmall)
        TituloSeccion("Reservas")
        Text("Las reservas son bloques exclusivos de una hora. La administración puede exigir aprobación por grado.")
        politicas.forEach { Text("${it.grado.etiqueta}: ${it.tokensSemana} tokens semanales y ${it.anticipacionDias} días de anticipación.") }
        TituloSeccion("Tokens")
        Text("Cada solicitud pendiente o confirmada usa un token de la semana en que se realiza. Las cancelaciones y rechazos liberan ese token. Cada lunes comienza un nuevo cupo semanal. Cambiar de semana no elimina tus reservas futuras.")
        TituloSeccion("Lista de espera")
        Text("Se ofrece cuando otra reserva ocupa el bloque. Al liberarse, se revisa la lista por orden de llegada. Si una cuenta ya no cumple las reglas, recibe un notificacion y se revisa la siguiente. La asignación puede quedar pendiente de aprobación.")
        TituloSeccion("Competencias")
        Text("Crea un equipo con integrantes activos de tu compañía. Cada persona puede estar en un solo equipo por evento. La inscripción de un equipo no consume tokens de reservas personales.")
        TituloSeccion("Datos y recursos del dispositivo")
        Text("Esta versión usa cuentas de demostración y una base de datos local. La foto se elige con el selector de archivos de Android. El calendario abre una aplicación externa para que revises y guardes el evento. No se envían notificaciones push ni recordatorios automáticos.")
    }
}
