package com.example.aplicacinejercito.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.Fechas
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun Pagina(contenido: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.widthIn(max = 960.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), content = contenido)
    }
}

@Composable
fun TituloSeccion(texto: String) { Text(texto, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) }

@Composable
fun CampoTexto(valor: String, cambiar: (String) -> Unit, etiqueta: String, error: String? = null,
               teclado: KeyboardType = KeyboardType.Text, variasLineas: Boolean = false, habilitado: Boolean = true) {
    OutlinedTextField(value = valor, onValueChange = cambiar, label = { Text(etiqueta) }, isError = error != null,
        supportingText = { if (error != null) Text(error) }, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = teclado), singleLine = !variasLineas, enabled = habilitado)
}

@Composable
fun BotonPersonalizado(texto: String, alClick: () -> Unit, modifier: Modifier = Modifier,
                       habilitado: Boolean = true, cargando: Boolean = false) {
    Button(onClick = alClick, enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp), shape = MaterialTheme.shapes.medium) {
        if (cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(texto)
    }
}

@Composable
fun MensajeError(texto: String?) {
    AnimatedVisibility(visible = texto != null) {
        Text(texto.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Selector(etiqueta: String, valor: String, opciones: List<String>, cambiar: (String) -> Unit, habilitado: Boolean = true) {
    var abierto by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { abierto = true }, enabled = habilitado, modifier = Modifier.fillMaxWidth()) { Text(valor) }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }, modifier = Modifier.heightIn(max = 350.dp)) {
                opciones.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { cambiar(opcion); abierto = false }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(valor: String, cambiar: (String) -> Unit, error: String? = null, habilitado: Boolean = true,
               minimo: LocalDate? = Fechas.hoy(), maximo: LocalDate? = null, etiqueta: String = "Fecha") {
    var abierto by rememberSaveable { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { abierto = true }, enabled = habilitado, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(etiqueta, style = MaterialTheme.typography.labelSmall)
                Text(if (valor.isBlank()) "Seleccionar fecha" else Fechas.etiqueta(valor), style = MaterialTheme.typography.bodyLarge)
            }
        }
        MensajeError(error)
    }
    if (abierto) {
        val seleccionables = remember(minimo, maximo) { object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = com.example.aplicacinejercito.model.Horarios.fechaPermitida(
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate(), minimo, maximo)
            override fun isSelectableYear(year: Int): Boolean = (minimo == null || year >= minimo.year) && (maximo == null || year <= maximo.year)
        } }
        val inicial = (Fechas.leer(valor) ?: minimo ?: Fechas.hoy()).let { fecha ->
            when { minimo != null && fecha.isBefore(minimo) -> minimo; maximo != null && fecha.isAfter(maximo) -> maximo; else -> fecha }
        }
        val picker = rememberDatePickerState(initialSelectedDateMillis = inicial.toEpochDay() * 86_400_000L,
            selectableDates = seleccionables)
        DatePickerDialog(onDismissRequest = { abierto = false }, confirmButton = {
            TextButton(enabled = picker.selectedDateMillis?.let(seleccionables::isSelectableDate) == true, onClick = {
                picker.selectedDateMillis?.takeIf(seleccionables::isSelectableDate)?.let {
                    cambiar(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())
                }
                abierto = false
            }) { Text("Aceptar") }
        }, dismissButton = { TextButton(onClick = { abierto = false }) { Text("Volver") } }) {
            DatePicker(state = picker, showModeToggle = false,
                colors = DatePickerDefaults.colors(disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.26f)))
        }
    }
}

@Composable
fun ConfirmarDialogo(titulo: String, detalle: String, confirmar: () -> Unit, cerrar: () -> Unit, textoConfirmar: String = "Confirmar") {
    AlertDialog(onDismissRequest = cerrar, title = { Text(titulo) }, text = { Text(detalle) },
        confirmButton = { TextButton(onClick = { confirmar(); cerrar() }) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = cerrar) { Text("Volver") } })
}
