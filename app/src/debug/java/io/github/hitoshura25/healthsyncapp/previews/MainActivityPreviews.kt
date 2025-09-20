package io.github.hitoshura25.healthsyncapp.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hitoshura25.healthsyncapp.PermissionRationaleScreen

@Preview(showBackground = true, name = "Health Connect Available - Permissions Granted")
@Composable
fun HealthDataScreenPreview_Granted() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Health Connect Data (from Local DB)", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Steps (from DB): 10000")
            Text(text = "Latest HR (DB): 75 BPM at 2023-01-01 10:00:00")
            Text(text = "Last Sleep (DB): 480 mins (ends 2023-01-01 07:00:00)")
            Text(text = "Latest Glucose (DB): 90 mg/dL at 2023-01-01 08:00:00")
            Text(text = "Latest Weight (DB): 70 kg at 2023-01-01 09:00:00")
            Text(text = "Total Active Calories Burned (DB): 500 kcal")
            Text(text = "Latest Basal Body Temperature (DB): 36.5 °C at 2023-01-01 06:00:00")
            Text(text = "Latest Basal Metabolic Rate (DB): 1500 kcal/day at 2023-01-01 06:00:00")
            Text(text = "Latest Distance (DB): 5.20 km (ends 2023-01-01 11:00:00)")
            Text(text = "Latest Elevation Gained (DB): 150.00 m (ends 2023-01-01 11:00:00)")
            Text(text = "Last Exercise Session (DB): RUNNING for 60 mins (ends 2023-01-01 11:00:00)")
            Text(text = "Latest Floors Climbed (DB): 10 floors (ends 2023-01-01 11:00:00)")
            Text(text = "Latest HRV RMSSD (DB): 45.00 ms (at 2023-01-01 06:00:00)")
            Text(text = "Latest Power (DB): 200.00 W (ends 2023-01-01 11:00:00)")
            Text(text = "Latest Resting Heart Rate (DB): 60 BPM (at 2023-01-01 06:00:00)")
            Text(text = "Latest Speed (DB): 2.50 m/s (ends 2023-01-01 11:00:00)")
            Text(text = "Total Calories Burned (DB): 2000 kcal")
            Text(text = "Latest VO2 Max (DB): 40.00 ml/(min·kg) (at 2023-01-01 12:00:00)")
            Text(text = "Latest Body Fat (DB): 20.00% (at 2023-01-01 09:00:00)")
            Text(text = "Latest Body Temperature (DB): 37.00 °C (at 2023-01-01 09:00:00)")
            Text(text = "Latest Body Water Mass (DB): 45.00 kg (at 2023-01-01 09:00:00)")
            Text(text = "Latest Bone Mass (DB): 3.00 kg (at 2023-01-01 09:00:00)")
            Text(text = "Latest Height (DB): 175.00 cm (at 2023-01-01 09:00:00)")
            Text(text = "Latest Lean Body Mass (DB): 55.00 kg (at 2023-01-01 09:00:00)")
            Text(text = "Total Hydration (DB): 2.00 L")
            Text(text = "Total Nutrition Calories (DB): 1800.00 kcal")
            Text(text = "Latest Blood Pressure (DB): 120/80 mmHg (at 2023-01-01 10:00:00)")
            Text(text = "Latest Oxygen Saturation (DB): 98.00% (at 2023-01-.01 10:00:00)")
            Text(text = "Latest Respiratory Rate (DB): 16.00 breaths/min (at 2023-01-01 10:00:00)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { }) { Text("Refresh Data from Health Connect") }
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
            Text(text = "Health Connect Data (from Local DB)", style = MaterialTheme.typography.headlineSmall)
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
            Text(text = "Health Connect Data (from Local DB)", style = MaterialTheme.typography.headlineSmall)
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
