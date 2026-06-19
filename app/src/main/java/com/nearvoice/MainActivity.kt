package com.nearvoice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var hasMicPermission by mutableStateOf(false)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasMicPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasMicPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NearVoiceApp(hasMicPermission)
                }
            }
        }
    }
}

@Composable
fun NearVoiceApp(hasMicPermission: Boolean) {
    var pairCode by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf("Enter a 6-digit code to connect.") }
    var isConnected by remember { mutableStateOf(false) }

    fun connectToPartner() {
        if (pairCode.length == 6) {
            isConnected = true
            connectionStatus = "Connected! Push to talk."
        } else {
            connectionStatus = "Code must be 6 digits."
        }
    }

    if (!hasMicPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please allow Microphone Permission.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (!isConnected) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NearVoice", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = pairCode,
                onValueChange = { if (it.length <= 6) pairCode = it },
                label = { Text("Enter 6-Digit Code") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(connectionStatus)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { connectToPartner() }) {
                Text("Connect")
            }
        }
    } else {
        PTTScreen(connectionStatus)
    }
}

@Composable
fun PTTScreen(status: String) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(status)
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {},
                modifier = Modifier
                    .size(200.dp)
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
                Text(if (isPressed) "TALKING..." else "PUSH TO TALK")
            }
        }
    }
}
