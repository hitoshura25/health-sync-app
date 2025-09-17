package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import io.github.hitoshura25.healthsyncapp.avro.*
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
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.openWriter
import androidx.test.core.app.ApplicationProvider
import androidx.hilt.work.HiltWorkerFactory

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

    private lateinit var context: Context
    private lateinit var stagingDir: File
    private lateinit var completedDir: File

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext<HiltTestApplication>()
        stagingDir = fileHandler.getStagingDirectory()
        completedDir = fileHandler.getCompletedDirectory()
        stagingDir.mkdirs()
        completedDir.mkdirs()
    }

    @After
    fun tearDown() {
        database.close()
        stagingDir.deleteRecursively()
        completedDir.deleteRecursively()
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

        val metadata = AvroMetadata(
            id = "test-id",
            dataOriginPackageName = "test.package",
            lastModifiedTimeEpochMillis = currentTime - 1000L,
            clientRecordId = "client-id",
            clientRecordVersion = 1L,
            device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")
        )

        // Create test files
        createAvroFileInDir(stagingDir, "StepsRecord_test1.avro", listOf(AvroStepsRecord(metadata, 1L, 2L, null, null, 100L, fetchTime)))
        createAvroFileInDir(stagingDir, "HeartRateRecord_test1.avro", listOf(AvroHeartRateRecord(metadata, 1L, 2L, null, null, fetchTime, listOf(AvroHeartRateSample(1L, 75L)))))
        createAvroFileInDir(stagingDir, "SleepSessionRecord_test1.avro", listOf(AvroSleepSessionRecord(metadata, "title", "notes", 1L, 2L, null, null, 1L, fetchTime, emptyList())))
        createAvroFileInDir(stagingDir, "BloodGlucoseRecord_test1.avro", listOf(AvroBloodGlucoseRecord(metadata, 1L, null, 120.0, AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD, AvroBloodGlucoseMealType.BREAKFAST, AvroBloodGlucoseRelationToMeal.BEFORE_MEAL, fetchTime)))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(4)

        // Verify data in database
        val steps = database.stepsRecordDao().getRecordByHcUid("test-id")
        assertThat(steps).isNotNull()
        assertThat(steps?.count).isEqualTo(100L)

        val hr = database.heartRateSampleDao().getSamplesByRecordHcUid("test-id")
        assertThat(hr).isNotEmpty()
        assertThat(hr[0].beatsPerMinute).isEqualTo(75L)

        val sleep = database.sleepSessionDao().getRecordByHcUid("test-id")
        assertThat(sleep).isNotNull()
        assertThat(sleep?.title).isEqualTo("title")

        val glucose = database.bloodGlucoseDao().getRecordByHcUid("test-id")
        assertThat(glucose).isNotNull()
        assertThat(glucose?.levelInMilligramsPerDeciliter).isEqualTo(120.0)
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