package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.Record

object HealthConnectConstants {
    val RECORD_TYPES_SUPPORTED: List<SupportedHealthRecordType<out Record>> = listOf(
        SupportedHealthRecordType.Steps,
        SupportedHealthRecordType.HeartRate,
        SupportedHealthRecordType.SleepSession,
        SupportedHealthRecordType.BloodGlucose,
        SupportedHealthRecordType.Weight,
        SupportedHealthRecordType.ActiveCaloriesBurned,
        SupportedHealthRecordType.BasalBodyTemperature,
        SupportedHealthRecordType.BasalMetabolicRate,
        SupportedHealthRecordType.CyclingPedalingCadence,
        SupportedHealthRecordType.Distance,
        SupportedHealthRecordType.ElevationGained,
        SupportedHealthRecordType.ExerciseSession,
        SupportedHealthRecordType.FloorsClimbed,
        SupportedHealthRecordType.HeartRateVariabilityRmssd,
        SupportedHealthRecordType.Power,
        SupportedHealthRecordType.RestingHeartRate,
        SupportedHealthRecordType.Speed,
        SupportedHealthRecordType.StepsCadence,
        SupportedHealthRecordType.TotalCaloriesBurned,
        SupportedHealthRecordType.Vo2Max,
        SupportedHealthRecordType.BodyFat,
        SupportedHealthRecordType.BodyTemperature,
        SupportedHealthRecordType.BodyWaterMass,
        SupportedHealthRecordType.BoneMass,
        SupportedHealthRecordType.Height,
        SupportedHealthRecordType.Hydration,
        SupportedHealthRecordType.LeanBodyMass,
        SupportedHealthRecordType.Nutrition,
        SupportedHealthRecordType.BloodPressure,
        SupportedHealthRecordType.OxygenSaturation,
        SupportedHealthRecordType.RespiratoryRate,
    )

    val RECORD_PERMISSIONS = RECORD_TYPES_SUPPORTED.map {
        HealthPermission.getReadPermission(it.recordKClass)
    }.toSet()

    val ALL_PERMISSIONS =  RECORD_PERMISSIONS + HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND

}