package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Config.OLDEST_SDK]) // Using oldest SDK for broader compatibility in Robolectric if needed
class BloodGlucoseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var bloodGlucoseDao: BloodGlucoseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Allowing main thread queries for simplicity in tests
            .build()
        bloodGlucoseDao = db.bloodGlucoseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun `insert and getRecordByHcUid retrieves correct entity with all fields`() = runBlocking {
        val now = Instant.now().toEpochMilli()
        val entity = BloodGlucoseEntity(
            hcUid = "test-hcuid-123",
            timeEpochMillis = now - 1000L,
            zoneOffsetId = "Europe/Berlin",
            levelInMilligramsPerDeciliter = 125.5,
            specimenSource = 1, // INTERSTITIAL_FLUID
            mealType = 2, // LUNCH
            relationToMeal = 3, // BEFORE_MEAL
            dataOriginPackageName = "com.test.app",
            hcLastModifiedTimeEpochMillis = now - 500L,
            clientRecordId = "client-id-xyz",
            clientRecordVersion = 3L,
            appRecordFetchTimeEpochMillis = now,
            isSynced = false
        )

        bloodGlucoseDao.insert(entity)
        val retrievedEntity = bloodGlucoseDao.getRecordByHcUid("test-hcuid-123")

        assertNotNull(retrievedEntity)
        assertEquals(entity.hcUid, retrievedEntity?.hcUid)
        assertEquals(entity.timeEpochMillis, retrievedEntity?.timeEpochMillis)
        assertEquals(entity.zoneOffsetId, retrievedEntity?.zoneOffsetId)

        assertTrue(entity.levelInMilligramsPerDeciliter == retrievedEntity?.levelInMilligramsPerDeciliter)
        assertEquals(entity.specimenSource, retrievedEntity?.specimenSource)
        assertEquals(entity.mealType, retrievedEntity?.mealType)
        assertEquals(entity.relationToMeal, retrievedEntity?.relationToMeal)
        assertEquals(entity.dataOriginPackageName, retrievedEntity?.dataOriginPackageName)
        assertEquals(entity.hcLastModifiedTimeEpochMillis, retrievedEntity?.hcLastModifiedTimeEpochMillis)
        assertEquals(entity.clientRecordId, retrievedEntity?.clientRecordId)
        assertEquals(entity.clientRecordVersion, retrievedEntity?.clientRecordVersion)
        assertEquals(entity.appRecordFetchTimeEpochMillis, retrievedEntity?.appRecordFetchTimeEpochMillis)
        assertEquals(entity.isSynced, retrievedEntity?.isSynced)
        assertTrue((retrievedEntity?.id ?: 0L) > 0L)
    }

    @Test
    @Throws(Exception::class)
    fun `insertAll and getAllObservable retrieves all entities`() = runBlocking {
        val now = Instant.now().toEpochMilli()
        val entity1 = BloodGlucoseEntity(
            hcUid = "uid1", 
            timeEpochMillis = now, 
            zoneOffsetId = null, // Explicitly set to null
            levelInMilligramsPerDeciliter = 100.0, 
            specimenSource = 1, 
            mealType = 1, 
            relationToMeal = 1, 
            dataOriginPackageName = "app1", 
            hcLastModifiedTimeEpochMillis = now, 
            clientRecordId = null, // Explicitly set to null
            clientRecordVersion = 1L, 
            appRecordFetchTimeEpochMillis = now
            // isSynced will use its default value of false
        )
        val entity2 = BloodGlucoseEntity(
            hcUid = "uid2", 
            timeEpochMillis = now + 1000L, 
            zoneOffsetId = null, // Explicitly set to null
            levelInMilligramsPerDeciliter = 110.0, 
            specimenSource = 2, 
            mealType = 2, 
            relationToMeal = 2, 
            dataOriginPackageName = "app2", 
            hcLastModifiedTimeEpochMillis = now, 
            clientRecordId = null, // Explicitly set to null
            clientRecordVersion = 1L, 
            appRecordFetchTimeEpochMillis = now
            // isSynced will use its default value of false
        )

        bloodGlucoseDao.insertAll(listOf(entity1, entity2))

        val allEntities = bloodGlucoseDao.getAllObservable().first() // Get the first emitted list
        assertEquals(2, allEntities.size)
        assertTrue(allEntities.any { it.hcUid == "uid1" })
        assertTrue(allEntities.any { it.hcUid == "uid2" })
    }
}
