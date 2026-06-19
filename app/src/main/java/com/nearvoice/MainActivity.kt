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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

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

        // Mag-login anonymously sa Firebase
        Firebase.auth.signInAnonymously()

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
    val db = Firebase.database.reference
    val myUid = Firebase.auth.currentUser?.uid ?: "loading..."

    var myPairCode by remember { mutableStateOf("") }
    var partnerCode by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf("Generate a code to start.") }
    var isConnected by remember { mutableStateOf(false) }

    // Generate 6-digit code
    fun generateCode() {
        val code = (100000..999999).random().toString()
        myPairCode = code
        connectionStatus = "Code: $code\nAsk your partner to enter this."
        
        // Simulate Partner Connection (For testing: If partner enters this code, connect)
        db.child("invitations").child(code).setValue(myUid)
    }

    // Connect to partner
    fun connectToPartner() {
        if (partnerCode.length == 6) {
            connectionStatus = "Connecting to $partnerCode..."
            db.child("invitations").child(partnerCode).get().addOnSuccessListener {
                if (it.exists()) {
                    isConnected = true
                    connectionStatus = "Connected! Push to talk."
                } else {
                    connectionStatus = "Invalid code."
                }
            }
        }
    }

    if (!hasMicPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please allow Microphone Permission.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (!isConnected) {
        // PAIRING SCREEN
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NearVoice", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { generateCode() }) {
                Text("Generate Pairing Code")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(connectionStatus)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = partnerCode,
                onValueChange = { if (it.length <= 6) partnerCode = it },
                label = { Text("Enter Partner's Code") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = { connectToPartner() }) {
                Text("Connect")
            }
        }
    } else {
        // PTT SCREEN (WebRTC Logic goes here for real-time audio)
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
                                // Start WebRTC Audio Stream
                                tryAwaitRelease()
                                isPressed = false
                                // Stop WebRTC Audio Stream
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
