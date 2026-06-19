package com.nearvoice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import java.io.File

class MainActivity : ComponentActivity() {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var hasMicPermission by mutableStateOf(false)

    // Hihingi ng Microphone permission kapag binuksan ang app
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasMicPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check kung may mic permission na
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
                    PTTScreen(
                        hasMicPermission = hasMicPermission,
                        onStartTalking = { startRecording() },
                        onStopTalking = { stopRecording() }
                    )
                }
            }
        }
    }

    private fun startRecording() {
        if (!hasMicPermission) return

        try {
            audioFile = File(externalCacheDir, "ptt_audio.3gp")
            recorder = (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun PTTScreen(
    hasMicPermission: Boolean,
    onStartTalking: () -> Unit,
    onStopTalking: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!hasMicPermission) {
                Text("Please allow Microphone Permission to use NearVoice.", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {},
                enabled = hasMicPermission,
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                onStartTalking() // Buksan ang mic
                                tryAwaitRelease()
                                isPressed = false
                                onStopTalking() // Isara ang mic
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
