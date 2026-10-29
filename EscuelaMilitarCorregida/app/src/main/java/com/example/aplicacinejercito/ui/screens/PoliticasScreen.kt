package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import com.example.aplicacinejercito.ui.components.*

@Composable
fun PoliticasScreen(politicas: List<PoliticaModel>, accion: AccionUiState,
                    cambiar: (Grado, Int, Int, Boolean?) -> Unit, guardar: () -> Unit) {
    Pagina {
        Text("Políticas de reserva", style = MaterialTheme.typography.headlineSmall)
        Text("Los cambios se aplican a nuevas solicitudes. Las reservas registradas conservan su estado y semana de consumo.")
        politicas.sortedBy { it.grado.ordinal }.forEach { p ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TituloSeccion(p.grado.etiqueta)
                    ContadorPolitica("Tokens semanales", p.tokensSemana, !accion.cargando, { cambiar(p.grado, -1, 0, null) }, { cambiar(p.grado, 1, 0, null) })
                    ContadorPolitica("Días de anticipación", p.anticipacionDias, !accion.cargando, { cambiar(p.grado, 0, -1, null) }, { cambiar(p.grado, 0, 1, null) })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Requiere aprobación", Modifier.weight(1f))
                        Switch(checked = p.requiereAprobacion, enabled = !accion.cargando, onCheckedChange = { cambiar(p.grado, 0, 0, it) })
                    }
                }
            }
        }
        Text("Límites de esta demostración: 1 a 20 tokens y 1 a 30 días de anticipación.", style = MaterialTheme.typography.bodySmall)
        MensajeError(accion.error)
        BotonPersonalizado("Guardar políticas", guardar, cargando = accion.cargando)
    }
}

@Composable
private fun ContadorPolitica(etiqueta: String, valor: Int, habilitado: Boolean, menos: () -> Unit, mas: () -> Unit) {
    Column {
        Text(etiqueta)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = menos, enabled = habilitado, modifier = Modifier.semantics { contentDescription = "Disminuir $etiqueta" }) { Text("−") }
            Text(valor.toString(), style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = mas, enabled = habilitado, modifier = Modifier.semantics { contentDescription = "Aumentar $etiqueta" }) { Text("+") }
        }
    }
}
