package io.github.hitoshura25.healthsyncapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Observer

private const val ACTION_SHOW_PERMISSIONS_RATIONALE = "androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE"

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    private lateinit var healthConnectClient: HealthConnectClient
    private val mainViewModel: MainViewModel by viewModels {
        if (!::healthConnectClient.isInitialized) {
            if (HealthConnectClient.getSdkStatus(this) == HealthConnectClient.SDK_AVAILABLE) {
                 healthConnectClient = HealthConnectClient.getOrCreate(this)
            } else {
                 throw IllegalStateException("HealthConnectClient could not be initialized for ViewModelFactory and SDK is unavailable.")
            }
        }
        MainViewModelFactory(application, healthConnectClient)
    }

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_SHOW_PERMISSIONS_RATIONALE) {
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
            if (!::healthConnectClient.isInitialized) {
                healthConnectClient = HealthConnectClient.getOrCreate(this)
            }

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
            // ViewModel now handles initial worker scheduling after permission check
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
                .padding(paddingValues)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "To provide insights into your health and fitness, this app needs to access your health data through Health Connect. This data includes steps, heart rate, sleep, and blood glucose.",
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Your data is processed locally on your device and is not shared with any third party without your explicit consent.",
                textAlign = TextAlign.Start,
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
    // Use observeAsState for LiveData (like allPermissionsGranted)
    val permissionsGranted by viewModel.allPermissionsGranted.observeAsState(initial = false)

    // Use collectAsState for StateFlow
    val steps by viewModel.stepsData.collectAsState()
    val heartRate by viewModel.heartRateData.collectAsState()
    val sleep by viewModel.sleepData.collectAsState()
    val bloodGlucose by viewModel.bloodGlucoseData.collectAsState()
    val weight by viewModel.weightData.collectAsState()
    val activeCaloriesBurned by viewModel.activeCaloriesBurnedData.collectAsState()
    val basalBodyTemperature by viewModel.basalBodyTemperatureData.collectAsState()
    val basalMetabolicRate by viewModel.basalMetabolicRateData.collectAsState()
    val distance by viewModel.distanceData.collectAsState()
    val elevationGained by viewModel.elevationGainedData.collectAsState()
    val exerciseSession by viewModel.exerciseSessionData.collectAsState()
    val floorsClimbed by viewModel.floorsClimbedData.collectAsState()
    val heartRateVariabilityRmssd by viewModel.heartRateVariabilityRmssdData.collectAsState()
    val power by viewModel.powerData.collectAsState()
    val restingHeartRate by viewModel.restingHeartRateData.collectAsState()
    val speed by viewModel.speedData.collectAsState()
    val totalCaloriesBurned by viewModel.totalCaloriesBurnedData.collectAsState()
    val vo2Max by viewModel.vo2MaxData.collectAsState()
    val bodyFat by viewModel.bodyFatData.collectAsState()
    val bodyTemperature by viewModel.bodyTemperatureData.collectAsState()
    val bodyWaterMass by viewModel.bodyWaterMassData.collectAsState()
    val boneMass by viewModel.boneMassData.collectAsState()
    val height by viewModel.heightData.collectAsState()
    val leanBodyMass by viewModel.leanBodyMassData.collectAsState()
    val hydration by viewModel.hydrationData.collectAsState()
    val nutrition by viewModel.nutritionData.collectAsState()
    val bloodPressure by viewModel.bloodPressureData.collectAsState()
    val oxygenSaturation by viewModel.oxygenSaturationData.collectAsState()
    val respiratoryRate by viewModel.respiratoryRateData.collectAsState()
    // syncStatusMessage LiveData and its observer are removed

    // No LaunchedEffect needed here to call viewModel.fetchHealthData() on permission grant,
    // as the ViewModel and SyncWorker now handle initial data loading proactively.
    // The UI will update automatically when the database changes.

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Health Connect Data (from Local DB)", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Start)

            if (!healthConnectAvailable) {
                Text("Health Connect SDK is not available on this device.", textAlign = TextAlign.Start)
            } else if (permissionsGranted) {
                Text(text = steps)
                Text(text = heartRate)
                Text(text = sleep)
                Text(text = bloodGlucose)
                Text(text = weight)
                Text(text = activeCaloriesBurned)
                Text(text = basalBodyTemperature)
                Text(text = basalMetabolicRate)
                Text(text = distance)
                Text(text = elevationGained)
                Text(text = exerciseSession)
                Text(text = floorsClimbed)
                Text(text = heartRateVariabilityRmssd)
                Text(text = power)
                Text(text = restingHeartRate)
                Text(text = speed)
                Text(text = totalCaloriesBurned)
                Text(text = vo2Max)
                Text(text = bodyFat)
                Text(text = bodyTemperature)
                Text(text = bodyWaterMass)
                Text(text = boneMass)
                Text(text = height)
                Text(text = leanBodyMass)
                Text(text = hydration)
                Text(text = nutrition)
                Text(text = bloodPressure)
                Text(text = oxygenSaturation)
                Text(text = respiratoryRate)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.triggerDataRefresh() }, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) { // Changed to triggerDataRefresh
                    Text("Refresh Data from Health Connect")
                }
                // "Process Unsynced Data" button and its status Text are removed
            } else {
                Text("Permissions not granted. Please grant permissions to see data.", textAlign = TextAlign.Start)
                Button(onClick = { viewModel.checkOrRequestPermissions() }) {
                    Text("Request Permissions")
                }
            }
        }
    }
}
