package com.nearvoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { isPressed = !isPressed },
            modifier = Modifier.size(200.dp),
            colors = if (isPressed) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                      else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Text(if (isPressed) "TALKING..." else "PUSH TO TALK")
        }
    }
}
