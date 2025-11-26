package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.ExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroExerciseType

private fun mapHcExerciseTypeToAvro(hcExerciseType: Int): AvroExerciseType {
    return when (hcExerciseType) {
        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> AvroExerciseType.OTHER_WORKOUT
        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> AvroExerciseType.BADMINTON
        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> AvroExerciseType.BASEBALL
        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> AvroExerciseType.BASKETBALL
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> AvroExerciseType.BIKING
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> AvroExerciseType.BIKING_STATIONARY
        ExerciseSessionRecord.EXERCISE_TYPE_BOOT_CAMP -> AvroExerciseType.BOOT_CAMP
        ExerciseSessionRecord.EXERCISE_TYPE_BOXING -> AvroExerciseType.BOXING
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> AvroExerciseType.CALISTHENICS
        ExerciseSessionRecord.EXERCISE_TYPE_CRICKET -> AvroExerciseType.CRICKET
        ExerciseSessionRecord.EXERCISE_TYPE_DANCING -> AvroExerciseType.DANCING
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> AvroExerciseType.ELLIPTICAL
        ExerciseSessionRecord.EXERCISE_TYPE_EXERCISE_CLASS -> AvroExerciseType.EXERCISE_CLASS
        ExerciseSessionRecord.EXERCISE_TYPE_FENCING -> AvroExerciseType.FENCING
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AMERICAN -> AvroExerciseType.FOOTBALL_AMERICAN
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AUSTRALIAN -> AvroExerciseType.FOOTBALL_AUSTRALIAN
        ExerciseSessionRecord.EXERCISE_TYPE_FRISBEE_DISC -> AvroExerciseType.FRISBEE_DISC
        ExerciseSessionRecord.EXERCISE_TYPE_GOLF -> AvroExerciseType.GOLF
        ExerciseSessionRecord.EXERCISE_TYPE_GUIDED_BREATHING -> AvroExerciseType.GUIDED_BREATHING
        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> AvroExerciseType.GYMNASTICS
        ExerciseSessionRecord.EXERCISE_TYPE_HANDBALL -> AvroExerciseType.HANDBALL
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> AvroExerciseType.HIGH_INTENSITY_INTERVAL_TRAINING
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> AvroExerciseType.HIKING
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_HOCKEY -> AvroExerciseType.ICE_HOCKEY
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_SKATING -> AvroExerciseType.ICE_SKATING
        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS -> AvroExerciseType.MARTIAL_ARTS
        ExerciseSessionRecord.EXERCISE_TYPE_PADDLING -> AvroExerciseType.PADDLING
        ExerciseSessionRecord.EXERCISE_TYPE_PARAGLIDING -> AvroExerciseType.PARAGLIDING
        ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> AvroExerciseType.PILATES
        ExerciseSessionRecord.EXERCISE_TYPE_RACQUETBALL -> AvroExerciseType.RACQUETBALL
        ExerciseSessionRecord.EXERCISE_TYPE_ROCK_CLIMBING -> AvroExerciseType.ROCK_CLIMBING
        ExerciseSessionRecord.EXERCISE_TYPE_ROLLER_HOCKEY -> AvroExerciseType.ROLLER_HOCKEY
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING -> AvroExerciseType.ROWING
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> AvroExerciseType.ROWING_MACHINE
        ExerciseSessionRecord.EXERCISE_TYPE_RUGBY -> AvroExerciseType.RUGBY
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> AvroExerciseType.RUNNING
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL -> AvroExerciseType.RUNNING_TREADMILL
        ExerciseSessionRecord.EXERCISE_TYPE_SAILING -> AvroExerciseType.SAILING
        ExerciseSessionRecord.EXERCISE_TYPE_SCUBA_DIVING -> AvroExerciseType.SCUBA_DIVING
        ExerciseSessionRecord.EXERCISE_TYPE_SKATING -> AvroExerciseType.SKATING
        ExerciseSessionRecord.EXERCISE_TYPE_SKIING -> AvroExerciseType.SKIING
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWBOARDING -> AvroExerciseType.SNOWBOARDING
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWSHOEING -> AvroExerciseType.SNOWSHOEING
        ExerciseSessionRecord.EXERCISE_TYPE_SOCCER -> AvroExerciseType.SOCCER
        ExerciseSessionRecord.EXERCISE_TYPE_SOFTBALL -> AvroExerciseType.SOFTBALL
        ExerciseSessionRecord.EXERCISE_TYPE_SQUASH -> AvroExerciseType.SQUASH
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING -> AvroExerciseType.STAIR_CLIMBING
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING_MACHINE -> AvroExerciseType.STAIR_CLIMBING_MACHINE
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> AvroExerciseType.STRENGTH_TRAINING
        ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING -> AvroExerciseType.STRETCHING
        ExerciseSessionRecord.EXERCISE_TYPE_SURFING -> AvroExerciseType.SURFING
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> AvroExerciseType.SWIMMING_OPEN_WATER
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> AvroExerciseType.SWIMMING_POOL
        ExerciseSessionRecord.EXERCISE_TYPE_TABLE_TENNIS -> AvroExerciseType.TABLE_TENNIS
        ExerciseSessionRecord.EXERCISE_TYPE_TENNIS -> AvroExerciseType.TENNIS
        ExerciseSessionRecord.EXERCISE_TYPE_VOLLEYBALL -> AvroExerciseType.VOLLEYBALL
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> AvroExerciseType.WALKING
        ExerciseSessionRecord.EXERCISE_TYPE_WATER_POLO -> AvroExerciseType.WATER_POLO
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> AvroExerciseType.WEIGHTLIFTING
        ExerciseSessionRecord.EXERCISE_TYPE_WHEELCHAIR -> AvroExerciseType.WHEELCHAIR
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> AvroExerciseType.YOGA
        else -> AvroExerciseType.UNKNOWN
    }
}

fun mapExerciseSessionRecord(record: ExerciseSessionRecord, fetchedTimeEpochMillis: Long): AvroExerciseSessionRecord {
    return AvroExerciseSessionRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        exerciseType = mapHcExerciseTypeToAvro(record.exerciseType),
        title = record.title,
        notes = record.notes,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}