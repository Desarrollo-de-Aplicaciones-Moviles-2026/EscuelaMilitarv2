package com.example.aplicacinejercito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aplicacinejercito.model.LoginUiState
import com.example.aplicacinejercito.ui.components.*

@Composable
fun LoginScreen(estado: LoginUiState, cambiarCorreo: (String) -> Unit, cambiarClave: (String) -> Unit, ingresar: () -> Unit, expandida: Boolean) {
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val encabezado: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Star, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.secondaryContainer)
            Text("Gestión Deportiva", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
            Text("Escuela Militar · Reserva de instalaciones", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
    val formulario: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
            CampoTexto(estado.correo, cambiarCorreo, "Correo institucional", estado.errores.correo, KeyboardType.Email, habilitado = !estado.cargando)
            OutlinedTextField(value = estado.clave, onValueChange = cambiarClave, modifier = Modifier.fillMaxWidth(),
                enabled = !estado.cargando, singleLine = true, label = { Text("Contraseña") }, isError = estado.errores.clave != null,
                supportingText = { estado.errores.clave?.let { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { mostrarClave = !mostrarClave }) {
                    Icon(if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña")
                } })
            MensajeError(estado.error)
            BotonPersonalizado("Ingresar", ingresar, cargando = estado.cargando)
            Text("Demostración local para el curso", style = MaterialTheme.typography.titleSmall)
            Text("Usuario: m.leiva@escuelamilitar.cl\nAdministración: admin@escuelamilitar.cl\nContraseña para ambas cuentas: Clave123", style = MaterialTheme.typography.bodySmall)
            Text("Los datos se guardan en este dispositivo. Las cuentas de prueba también incluyen tenientes y cabos.", style = MaterialTheme.typography.bodySmall)
        }
    }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            if (expandida) Row(Modifier.widthIn(max = 1100.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { encabezado() }; Box(Modifier.weight(1f)) { formulario() }
            } else Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) { encabezado(); formulario() }
        }
    }
}
