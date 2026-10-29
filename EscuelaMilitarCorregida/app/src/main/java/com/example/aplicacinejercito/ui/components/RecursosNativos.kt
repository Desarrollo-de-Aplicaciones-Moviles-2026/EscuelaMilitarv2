package com.example.aplicacinejercito.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FotoPerfil(uri: String?, guardar: (String) -> Unit) {
    val context = LocalContext.current
    var foto by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        foto = if (uri.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            runCatching {
                val direccion = Uri.parse(uri)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(direccion)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var muestra = 1
                while (bounds.outWidth / muestra > 600 || bounds.outHeight / muestra > 600) muestra *= 2
                context.contentResolver.openInputStream(direccion)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { direccion ->
        if (direccion != null) {
            val permiso = runCatching { context.contentResolver.takePersistableUriPermission(direccion, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            if (permiso.isSuccess) guardar(direccion.toString())
            else Toast.makeText(context, "No fue posible conservar el acceso a la imagen. Elige otro archivo.", Toast.LENGTH_LONG).show()
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (foto != null) Image(foto!!, "Foto de perfil", Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        else Icon(Icons.Default.Person, "Perfil sin foto", Modifier.size(72.dp))
        OutlinedButton(onClick = { selector.launch(arrayOf("image/*")) }) { Text("Elegir foto") }
    }
    if (uri != null && foto == null) Text("Si la imagen dejó de estar disponible, puedes seleccionarla nuevamente.", style = MaterialTheme.typography.bodySmall)
}

@Composable
fun BotonCalendario(reserva: ReservaModel, instalacion: String) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val fecha = Fechas.leer(reserva.fecha) ?: return@OutlinedButton
        val inicio = fecha.atTime(reserva.horaInicio, 0).atZone(Fechas.zona).toInstant().toEpochMilli()
        val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "Reserva $instalacion")
            .putExtra(CalendarContract.Events.EVENT_LOCATION, "Escuela Militar")
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, inicio + 3_600_000)
        try { context.startActivity(intent) }
        catch (e: ActivityNotFoundException) { Toast.makeText(context, "No hay una aplicación de calendario disponible.", Toast.LENGTH_LONG).show() }
        catch (e: SecurityException) { Toast.makeText(context, "No se pudo abrir el calendario.", Toast.LENGTH_LONG).show() }
    }, modifier = Modifier.fillMaxWidth()) { Text("Agregar al calendario del dispositivo") }
}

internal fun reporteCsv(datos: DatosApp): String {
    fun celda(valor: String): String {
        val seguro = if (valor.trimStart().firstOrNull() in listOf('=', '+', '-', '@')) "'$valor" else valor
        return "\"${seguro.replace("\"", "\"\"")}\""
    }
    val filas = datos.reservas.map { r -> listOf(r.fecha,
        datos.canchas.firstOrNull { it.id == r.canchaId }?.nombre.orEmpty(),
        datos.usuarios.firstOrNull { it.id == r.usuarioId }?.nombre.orEmpty(),
        Fechas.horario(r.horaInicio), r.estado.etiqueta) }
    return "\uFEFF" + (listOf(listOf("Fecha", "Instalación", "Usuario", "Horario", "Estado")) + filas)
        .joinToString("\r\n") { fila -> fila.joinToString(";", transform = ::celda) }
}

@Composable
fun BotonExportar(datos: DatosApp) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }
    val contenido by rememberUpdatedState(reporteCsv(datos))
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            guardando = true
            val resultado = withContext(Dispatchers.IO) { runCatching {
                val salida = context.contentResolver.openOutputStream(uri) ?: error("No se abrió el archivo.")
                salida.use { it.write(contenido.toByteArray(Charsets.UTF_8)) }
            } }
            guardando = false
            Toast.makeText(context, if (resultado.isSuccess) "Reporte guardado." else "No fue posible guardar el reporte.", Toast.LENGTH_LONG).show()
        }
    }
    OutlinedButton(onClick = { selector.launch("reservas_${Fechas.hoy()}.csv") }, enabled = !guardando, modifier = Modifier.fillMaxWidth()) { Text(if (guardando) "Guardando…" else "Exportar reservas a CSV") }
}
