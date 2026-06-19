package com.nearvoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PTTScreen()
                }
            }
        }
    }
}

@Composable
fun PTTScreen() {
    var isPressed by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val buttonSize = if (maxWidth < 600.dp) maxWidth * 0.6f else 250.dp

        Button(
            onClick = {},
            modifier = Modifier
                .size(buttonSize)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                },
            colors = if (isPressed) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                      else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Text(
                text = if (isPressed) "TALKING..." else "PUSH TO TALK",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
