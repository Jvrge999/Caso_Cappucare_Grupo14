package com.cappucare.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cappucare.app.R
import com.cappucare.app.viewmodel.CappucareViewModel

@Composable
fun LoginScreen(viewModel: CappucareViewModel, onIngresar: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var intentoEnvio by remember { mutableStateOf(false) }

    val error by viewModel.errorLogin.collectAsState()

    val usuarioVacio = intentoEnvio && usuario.trim().isEmpty()
    val claveVacia = intentoEnvio && clave.isEmpty()
    val credencialesInvalidas = intentoEnvio && !usuarioVacio && !claveVacia && error != null

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Logo Cappucare",
                modifier = Modifier.size(110.dp)
            )
            Text("Cappucare Stock", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Control de existencias de insumos médicos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    if (intentoEnvio) viewModel.limpiarErrorLogin()
                },
                label = { Text("Usuario (correo)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Correo"
                    )
                },
                trailingIcon = {
                    if (usuarioVacio || credencialesInvalidas) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error en usuario",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = usuarioVacio || credencialesInvalidas,
                supportingText = {
                    when {
                        usuarioVacio -> Text("Ingresa tu correo o usuario")
                        credencialesInvalidas -> Text(error.orEmpty())
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clave,
                onValueChange = {
                    clave = it
                    if (intentoEnvio) viewModel.limpiarErrorLogin()
                },
                label = { Text("Clave") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Clave"
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (claveVacia || credencialesInvalidas) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error en clave",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        IconButton(onClick = { claveVisible = !claveVisible }) {
                            Icon(
                                imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (claveVisible) "Ocultar clave" else "Mostrar clave"
                            )
                        }
                    }
                },
                isError = claveVacia || credencialesInvalidas,
                supportingText = {
                    when {
                        claveVacia -> Text("Ingresa tu clave")
                        credencialesInvalidas -> Text(error.orEmpty())
                    }
                },
                singleLine = true,
                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = error != null && !usuarioVacio && !claveVacia) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Alerta de acceso",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    intentoEnvio = true
                    if (viewModel.login(usuario.trim(), clave)) {
                        onIngresar()
                    }
                }
            ) {
                Text("Ingresar")
            }
        }
    }
}