package com.pmj.securevault

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SecureVault",
        icon = painterResource("icon.png")
    ) {
        App()
    }
}