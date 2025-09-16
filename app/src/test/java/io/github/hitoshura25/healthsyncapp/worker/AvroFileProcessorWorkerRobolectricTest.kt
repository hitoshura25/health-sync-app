package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.hilt.work.HiltWorkerFactory
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.openWriter
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
// Explicit DAO imports
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.file.FileHandler
// Explicit Avro DTO and Enum imports
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class, manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class AvroFileProcessorWorkerRobolectricTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var fileHandler: FileHandler

    // DAOs
    @Inject
    lateinit var stepsRecordDao: StepsRecordDao
    @Inject
    lateinit var heartRateSampleDao: HeartRateSampleDao
    @Inject
    lateinit var sleepSessionDao: SleepSessionDao
    @Inject
    lateinit var sleepStageDao: SleepStageDao
    @Inject
    lateinit var bloodGlucoseDao: BloodGlucoseDao

    private lateinit var context: Context
    private lateinit var stagingDir: File
    private lateinit var completedDir: File

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext<HiltTestApplication>()

        stagingDir = fileHandler.getStagingDirectory()
        completedDir = fileHandler.getCompletedDirectory()

        stagingDir.deleteRecursively()
        completedDir.deleteRecursively()
        stagingDir.mkdirs()
        completedDir.mkdirs()
    }

    @After
    fun tearDown() {
        stagingDir.deleteRecursively()
        completedDir.deleteRecursively()
        database.close() // Close the database after tests
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private inline fun <reified T : Any> createAvroFileInDir(directory: File, fileName: String, records: List<T>) {
        val file = File(directory, fileName)
        file.outputStream().buffered().use { outputStream ->
            AvroObjectContainer.openWriter<T>(outputStream).use { writer ->
                records.forEach { record -> writer.writeValue(record) }
            }
        }
    }

    @Test
    fun testWorker_processesAllFileTypes_movesToCompleted_andSavesData() = runBlocking {
        val currentTime = System.currentTimeMillis()
        val fetchTime = currentTime - 100

        // Steps
        val avroStepsRecord = AvroStepsRecord(
            hcUid = "steps-uid-1",
            startTimeEpochMillis = currentTime - 10000L,
            endTimeEpochMillis = currentTime - 5000L,
            count = 500L,
            dataOriginPackageName = "test.pkg.steps",
            hcLastModifiedTimeEpochMillis = currentTime - 2000L,
            appRecordFetchTimeEpochMillis = fetchTime,
            clientRecordId = "client-steps-1",
            clientRecordVersion = 1L,
            startZoneOffsetId = "Europe/London",
            endZoneOffsetId = "Europe/London"
        )
        createAvroFileInDir(stagingDir, "StepsRecord_test1.avro", listOf(avroStepsRecord))

        // Heart Rate
        val avroHeartRateSamples = listOf(AvroHeartRateSample(timeEpochMillis = currentTime - 8000L, beatsPerMinute = 75L))
        val avroHeartRateRecord = AvroHeartRateRecord(
            hcUid = "hr-uid-1",
            startTimeEpochMillis = currentTime - 9000L,
            endTimeEpochMillis = currentTime - 7000L,
            samples = avroHeartRateSamples,
            dataOriginPackageName = "test.pkg.hr",
            hcLastModifiedTimeEpochMillis = currentTime - 1000L,
            appRecordFetchTimeEpochMillis = fetchTime,
            clientRecordId = "client-hr-1",
            clientRecordVersion = 1L,
            startZoneOffsetId = "Europe/London",
            endZoneOffsetId = "Europe/London"
        )
        createAvroFileInDir(stagingDir, "HeartRateRecord_test1.avro", listOf(avroHeartRateRecord))

        // Sleep Session with Stages
        val avroSleepStages = listOf(
            AvroSleepStageRecord(startTimeEpochMillis = currentTime - 60000L, endTimeEpochMillis = currentTime - 50000L, stage = AvroSleepStageType.LIGHT),
            AvroSleepStageRecord(startTimeEpochMillis = currentTime - 50000L, endTimeEpochMillis = currentTime - 40000L, stage = AvroSleepStageType.DEEP)
        )
        val avroSleepSessionRecord = AvroSleepSessionRecord(
            hcUid = "sleep-uid-1",
            title = "Night Sleep",
            notes = "Good rest",
            startTimeEpochMillis = currentTime - 70000L,
            endTimeEpochMillis = currentTime - 30000L,
            stages = avroSleepStages,
            dataOriginPackageName = "test.pkg.sleep",
            hcLastModifiedTimeEpochMillis = currentTime - 500L,
            appRecordFetchTimeEpochMillis = fetchTime,
            clientRecordId = "client-sleep-1",
            clientRecordVersion = 1L,
            startZoneOffsetId = "Europe/London",
            endZoneOffsetId = "Europe/London",
            durationMillis = 40000L
        )
        createAvroFileInDir(stagingDir, "SleepSessionRecord_test1.avro", listOf(avroSleepSessionRecord))

        // Blood Glucose
        val originalAvroBgRecord = AvroBloodGlucoseRecord(
            hcUid = "bg-uid-1",
            timeEpochMillis = currentTime - 15000L,
            levelInMilligramsPerDeciliter = 100.0,
            specimenSource = AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD,
            mealType = AvroBloodGlucoseMealType.BREAKFAST,
            relationToMeal = AvroBloodGlucoseRelationToMeal.BEFORE_MEAL,
            dataOriginPackageName = "test.pkg.bg",
            hcLastModifiedTimeEpochMillis = currentTime - 300L,
            appRecordFetchTimeEpochMillis = fetchTime,
            clientRecordId = "client-bg-1",
            clientRecordVersion = 1L,
            zoneOffsetId = "Europe/London"
        )
        createAvroFileInDir(stagingDir, "BloodGlucoseRecord_test1.avro", listOf(originalAvroBgRecord))

        val unknownFile = File(stagingDir, "UnknownRecord_test.avro")
        unknownFile.writeText("some dummy data")

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        // File movement assertions
        assertThat(File(stagingDir, "StepsRecord_test1.avro").exists()).isFalse()
        assertThat(File(completedDir, "StepsRecord_test1.avro").exists()).isTrue()
        assertThat(File(stagingDir, "HeartRateRecord_test1.avro").exists()).isFalse()
        assertThat(File(completedDir, "HeartRateRecord_test1.avro").exists()).isTrue()
        assertThat(File(stagingDir, "SleepSessionRecord_test1.avro").exists()).isFalse()
        assertThat(File(completedDir, "SleepSessionRecord_test1.avro").exists()).isTrue()
        assertThat(File(stagingDir, "BloodGlucoseRecord_test1.avro").exists()).isFalse()
        assertThat(File(completedDir, "BloodGlucoseRecord_test1.avro").exists()).isTrue()
        assertThat(stagingDir.resolve("UnknownRecord_test.avro").exists()).isTrue()

        // Assertions for DAO interactions
        val savedSteps = stepsRecordDao.getRecordByHcUid("steps-uid-1")
        assertThat(savedSteps).isNotNull()
        assertThat(savedSteps?.count).isEqualTo(avroStepsRecord.count)
        assertThat(savedSteps?.hcUid).isEqualTo(avroStepsRecord.hcUid)
        // Add more assertions for steps if needed

        val savedHrSamples = heartRateSampleDao.getSamplesByRecordHcUid("hr-uid-1")
        assertThat(savedHrSamples).isNotEmpty()
        assertThat(savedHrSamples.size).isEqualTo(1)
        assertThat(savedHrSamples[0].beatsPerMinute).isEqualTo(avroHeartRateSamples[0].beatsPerMinute)
        assertThat(savedHrSamples[0].hcRecordUid).isEqualTo(avroHeartRateRecord.hcUid)
        // Add more assertions for heart rate if needed

        val savedSleepSession = sleepSessionDao.getRecordByHcUid("sleep-uid-1")
        assertThat(savedSleepSession).isNotNull()
        assertThat(savedSleepSession?.title).isEqualTo(avroSleepSessionRecord.title)
        assertThat(savedSleepSession?.hcUid).isEqualTo(avroSleepSessionRecord.hcUid)
        // Add more assertions for sleep session if needed

        val savedSleepStages = sleepStageDao.getStagesBySessionHcUid("sleep-uid-1")
        assertThat(savedSleepStages).isNotEmpty()
        assertThat(savedSleepStages.size).isEqualTo(avroSleepStages.size)
        // Add more detailed assertions for sleep stages if needed

        val savedBg = bloodGlucoseDao.getRecordByHcUid("bg-uid-1")
        assertThat(savedBg).isNotNull()
        assertThat(savedBg?.hcUid).isEqualTo(originalAvroBgRecord.hcUid)
        assertThat(savedBg?.timeEpochMillis).isEqualTo(originalAvroBgRecord.timeEpochMillis)
        assertThat(savedBg?.zoneOffsetId).isEqualTo(originalAvroBgRecord.zoneOffsetId)
        assertThat(savedBg?.levelInMilligramsPerDeciliter).isEqualTo(originalAvroBgRecord.levelInMilligramsPerDeciliter)
        assertThat(savedBg?.specimenSource).isEqualTo(2) // CAPILLARY_BLOOD -> 2
        assertThat(savedBg?.mealType).isEqualTo(1) // BREAKFAST -> 1
        assertThat(savedBg?.relationToMeal).isEqualTo(3) // BEFORE_MEAL -> 3
        assertThat(savedBg?.dataOriginPackageName).isEqualTo(originalAvroBgRecord.dataOriginPackageName)
        assertThat(savedBg?.hcLastModifiedTimeEpochMillis).isEqualTo(originalAvroBgRecord.hcLastModifiedTimeEpochMillis)
        assertThat(savedBg?.clientRecordId).isEqualTo(originalAvroBgRecord.clientRecordId)
        assertThat(savedBg?.clientRecordVersion).isEqualTo(originalAvroBgRecord.clientRecordVersion)
        assertThat(savedBg?.appRecordFetchTimeEpochMillis).isEqualTo(originalAvroBgRecord.appRecordFetchTimeEpochMillis)
    }

    @Test
    fun testWorker_emptyStagingDirectory_succeeds() = runBlocking {
        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()
        val result = worker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}
