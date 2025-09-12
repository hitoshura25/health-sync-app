package io.github.hitoshura25.healthsyncapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
// Explicit imports for Jetpack Compose foundation layout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// Other Jetpack Compose imports
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Added for LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Health Connect and Lifecycle imports
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Observer

// Define the action string as a constant for clarity and to avoid typos
private const val ACTION_SHOW_PERMISSIONS_RATIONALE = "androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE"

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    private lateinit var healthConnectClient: HealthConnectClient
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the activity was launched to show the permissions rationale
        if (intent?.action == ACTION_SHOW_PERMISSIONS_RATIONALE) { // Using the defined constant
            Log.d(TAG, "Activity launched to show permissions rationale.")
            setContent {
                MaterialTheme {
                    PermissionRationaleScreen(
                        onContinue = {
                            setResult(RESULT_OK)
                            finish()
                        },
                        onCancel = {
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        } else {
            Log.d(TAG, "Activity launched for normal app flow.")
            // Normal app startup flow
            initializeHealthConnectAndApp()
            setContent {
                MaterialTheme {
                    HealthDataScreen(
                        mainViewModel,
                        healthConnectAvailable = HealthConnectClient.getSdkStatus(this) == HealthConnectClient.SDK_AVAILABLE
                    )
                }
            }
        }
    }

    private fun initializeHealthConnectAndApp() {
        if (HealthConnectClient.getSdkStatus(this) == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(this)
            mainViewModel.setHealthConnectClient(healthConnectClient)

            requestPermissionsLauncher = registerForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { grantedPermissions ->
                mainViewModel.onPermissionsResult(grantedPermissions)
            }

            mainViewModel.requestPermissionsLauncherEvent.observe(this, Observer { permissionsToRequest ->
                if (permissionsToRequest.isNotEmpty()) {
                    Log.d(TAG, "Observing requestPermissionsLauncherEvent: Requesting permissions via Health Connect contract.")
                    if (::requestPermissionsLauncher.isInitialized) {
                        requestPermissionsLauncher.launch(permissionsToRequest)
                    } else {
                        Log.e(TAG, "requestPermissionsLauncher not initialized when attempting to launch.")
                    }
                }
            })
            mainViewModel.checkOrRequestPermissions()
        } else {
            Log.e(TAG, "Health Connect SDK is not available on this device.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRationaleScreen(onContinue: () -> Unit, onCancel: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Permission Needed") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp), // Additional padding for content
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "To provide insights into your health and fitness, this app needs to access your health data through Health Connect. This data includes steps, heart rate, sleep, and blood glucose.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Your data is processed locally on your device and is not shared with any third party without your explicit consent.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun HealthDataScreen(viewModel: MainViewModel, healthConnectAvailable: Boolean) {
    val permissionsGranted by viewModel.allPermissionsGranted.observeAsState(initial = false)
    val steps by viewModel.stepsData.observeAsState(initial = "Steps: Loading...")
    val heartRate by viewModel.heartRateData.observeAsState(initial = "Heart Rate: Loading...")
    val sleep by viewModel.sleepData.observeAsState(initial = "Sleep: Loading...")
    val bloodGlucose by viewModel.bloodGlucoseData.observeAsState(initial = "Blood Glucose: Loading...")

    // Automatically fetch data when permissions are granted
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            Log.d("HealthDataScreen", "Permissions granted, attempting to fetch health data.")
            viewModel.fetchHealthData()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Health Connect Data", style = MaterialTheme.typography.headlineSmall)

            if (!healthConnectAvailable) {
                Text("Health Connect SDK is not available on this device.")
            } else if (permissionsGranted) {
                Text(text = steps)
                Text(text = heartRate)
                Text(text = sleep)
                Text(text = bloodGlucose)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.fetchHealthData() }) {
                    Text("Refresh Data")
                }
            } else {
                Text("Permissions not granted. Please grant permissions to see data.")
                Button(onClick = { viewModel.checkOrRequestPermissions() }) {
                    Text("Request Permissions")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Health Connect Available - Permissions Granted")
@Composable
fun HealthDataScreenPreview_Granted() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Health Connect Data", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Steps: 10000")
            Text(text = "Heart Rate: 75 BPM")
            Text(text = "Sleep: 480 mins")
            Text(text = "Blood Glucose: 90 mg/dL")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { }) { Text("Refresh Data") }
        }
    }
}

@Preview(showBackground = true, name = "Health Connect Available - Permissions Not Granted")
@Composable
fun HealthDataScreenPreview_NotGranted() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Health Connect Data", style = MaterialTheme.typography.headlineSmall)
            Text("Permissions not granted. Please grant permissions to see data.")
            Button(onClick = { }) { Text("Request Permissions") }
        }
    }
}

@Preview(showBackground = true, name = "Health Connect Not Available")
@Composable
fun HealthDataScreenPreview_NotAvailable() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Health Connect Data", style = MaterialTheme.typography.headlineSmall)
            Text("Health Connect SDK is not available on this device.")
        }
    }
}

@Preview(showBackground = true, name = "Permission Rationale Screen")
@Composable
fun PermissionRationaleScreenPreview() {
    MaterialTheme {
        PermissionRationaleScreen(onContinue = {}, onCancel = {})
    }
}
