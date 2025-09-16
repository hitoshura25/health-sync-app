package io.github.hitoshura25.healthsyncapp.data.mapper

// Removed: import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseLevelUnit - No longer used
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class AvroToEntityMappersTest {

    @Test
    fun `AvroBloodGlucoseRecord toBloodGlucoseEntity correctly maps all fields`() {
        val nowEpochMillis = Instant.now().toEpochMilli()
        val avroRecord = AvroBloodGlucoseRecord(
            hcUid = "test-hcuid-bg-123",
            timeEpochMillis = nowEpochMillis - 10000L,
            zoneOffsetId = "Europe/London",
            levelInMilligramsPerDeciliter = 120.5, // Renamed field
            // levelUnit field removed
            specimenSource = AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD, 
            mealType = AvroBloodGlucoseMealType.LUNCH, 
            relationToMeal = AvroBloodGlucoseRelationToMeal.AFTER_MEAL, 
            dataOriginPackageName = "com.example.healthapp",
            hcLastModifiedTimeEpochMillis = nowEpochMillis - 5000L,
            clientRecordId = "client-bg-001",
            clientRecordVersion = 2L,
            appRecordFetchTimeEpochMillis = nowEpochMillis
        )

        val entity = avroRecord.toBloodGlucoseEntity()

        assertEquals("test-hcuid-bg-123", entity.hcUid)
        assertEquals(nowEpochMillis - 10000L, entity.timeEpochMillis)
        assertEquals("Europe/London", entity.zoneOffsetId)
        assertEquals(120.5, entity.levelInMilligramsPerDeciliter, 0.001) // Assert renamed field
        // assertEquals for levelUnit removed
        assertEquals(mapSpecimenSourceToInt(AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD), entity.specimenSource) 
        assertEquals(mapMealTypeToInt(AvroBloodGlucoseMealType.LUNCH), entity.mealType) 
        assertEquals(mapRelationToMealToInt(AvroBloodGlucoseRelationToMeal.AFTER_MEAL), entity.relationToMeal) 
        assertEquals("com.example.healthapp", entity.dataOriginPackageName)
        assertEquals(nowEpochMillis - 5000L, entity.hcLastModifiedTimeEpochMillis)
        assertEquals("client-bg-001", entity.clientRecordId)
        assertEquals(2L, entity.clientRecordVersion)
        assertEquals(nowEpochMillis, entity.appRecordFetchTimeEpochMillis)
        assertEquals(false, entity.isSynced) 
    }

    private fun mapSpecimenSourceToInt(avroSpecimenSource: AvroBloodGlucoseSpecimenSource): Int { 
        return when (avroSpecimenSource) {
            AvroBloodGlucoseSpecimenSource.INTERSTITIAL_FLUID -> 1
            AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD -> 2
            AvroBloodGlucoseSpecimenSource.PLASMA -> 3
            AvroBloodGlucoseSpecimenSource.SERUM -> 4
            AvroBloodGlucoseSpecimenSource.TEARS -> 5
            AvroBloodGlucoseSpecimenSource.WHOLE_BLOOD -> 6
            AvroBloodGlucoseSpecimenSource.UNKNOWN -> 0
        }
    }

    private fun mapMealTypeToInt(avroMealType: AvroBloodGlucoseMealType): Int { 
        return when (avroMealType) {
            AvroBloodGlucoseMealType.BREAKFAST -> 1
            AvroBloodGlucoseMealType.LUNCH -> 2
            AvroBloodGlucoseMealType.DINNER -> 3
            AvroBloodGlucoseMealType.SNACK -> 4
            AvroBloodGlucoseMealType.UNKNOWN -> 0
        }
    }

    private fun mapRelationToMealToInt(avroRelationToMeal: AvroBloodGlucoseRelationToMeal): Int { 
        return when (avroRelationToMeal) {
            AvroBloodGlucoseRelationToMeal.GENERAL -> 1
            AvroBloodGlucoseRelationToMeal.FASTING -> 2
            AvroBloodGlucoseRelationToMeal.BEFORE_MEAL -> 3
            AvroBloodGlucoseRelationToMeal.AFTER_MEAL -> 4
            AvroBloodGlucoseRelationToMeal.UNKNOWN -> 0
        }
    }
}
