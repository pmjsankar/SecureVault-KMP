package com.pmj.securevault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    val scope = rememberCoroutineScope()
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val vault = remember { VaultProvider.getInstance() }

    MaterialTheme {

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize().padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Key") }
            )

            TextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value") }
            )

            Button(onClick = {
                scope.launch {
                    try {
                        vault.put(key, value, VaultOptions(requireBiometric = true))
                        status = "Saved successfully"
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    }
                }
            }) {
                Text("Save to Vault")
            }

            Button(onClick = {
                scope.launch {
                    try {
                        val result = vault.get(key, VaultOptions(requireBiometric = true))
                        status = "Retrieved: ${result ?: "null"}"
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    }
                }
            }) {
                Text("Get from Vault")
            }

            Text(text = status, color = Gray)
        }
    }
}
