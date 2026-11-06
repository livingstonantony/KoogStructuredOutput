package dev.ell.koogstructuredoutput

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.ell.koogstructuredoutput.ui.StructuredOutputUI

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KoogStructuredOutput",
    ) {
        StructuredOutputUI()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true, backgroundColor = 0xFFFFFFFF
)
@Composable
fun StructuredOutputUIPreview() {
    MaterialTheme {
        Surface(color = Color.White) {
            StructuredOutputUI()
        }
    }
}