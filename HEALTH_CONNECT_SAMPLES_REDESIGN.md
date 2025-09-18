# Health Connect Samples Data Redesign

## 1. Overview

**Problem:** The data model for several Health Connect record types is incorrect, leading to significant data loss. The affected records all contain time-series sample data.

**Goal:** Refactor the entire data pipeline for these records—from Avro serialization to Room database persistence—to correctly handle and preserve the time-series sample data.

## 2. Current Progress (Phase 1 Complete)

The first phase of the refactoring is complete. This involved:
1.  **Creating New Avro Sample Records:** `Avro...Sample.kt` files were created.
2.  **Updating Main Avro Records:** The main `Avro...Record.kt` files were updated to use a `List` of the new sample records.
3.  **Updating HealthConnect-to-Avro Mappers:** The `Map...Record.kt` files were updated to correctly populate the new Avro structures.

## 3. Phase 2: Database Layer Refactoring (Relational Approach)

**Problem:** The Room database entities and DAOs are still based on the old, flattened data structure. They must be updated to handle the one-to-many relationship between a record and its samples.

**Proposed Solution:** We will implement a proper relational model in Room. For each record type, we will have a parent entity (the record) and a child entity (the sample). A new data class will be used to hold the parent and its related list of children for querying.

### Detailed Plan for Phase 2

**Step 1: Create New "Sample" Entity Classes**

For each of the four record types, a new entity class will be created to store its samples. Each sample entity will contain a foreign key referencing the parent record's UID.

- **Files to Create:**
  - `CyclingPedalingCadenceSampleEntity.kt`
  - `PowerSampleEntity.kt`
  - `SpeedSampleEntity.kt`
  - `StepsCadenceSampleEntity.kt`

- **Example Structure (`PowerSampleEntity.kt`):**
  ```kotlin
  @Entity(
      tableName = "power_samples",
      foreignKeys = [
          ForeignKey(
              entity = PowerRecordEntity::class,
              parentColumns = ["hcUid"],
              childColumns = ["parentRecordUid"],
              onDelete = ForeignKey.CASCADE
          )
      ]
  )
  data class PowerSampleEntity(
      @PrimaryKey(autoGenerate = true) val id: Long = 0,
      val parentRecordUid: String,
      val timeEpochMillis: Long,
      val powerInWatts: Double
  )
  ```

**Step 2: Update Main Record Entity Classes**

The existing `...RecordEntity.kt` files will be updated to remove the old single-value fields. The `hcUid` will serve as the primary key for the relationship.

- **`CyclingPedalingCadenceRecordEntity.kt`**: Remove `revolutions: Long`.
- **`PowerRecordEntity.kt`**: Remove `powerInWatts: Double`.
- **`SpeedRecordEntity.kt`**: Remove `speedInMetersPerSecond: Double`.
- **`StepsCadenceRecordEntity.kt`**: Remove `rateInStepsPerMinute: Double`.

**Step 3: Create Relational Data Classes**

To query a record *with* its samples, we need a new data class for each type that combines the parent and children.

- **Files to Create:**
  - `CyclingPedalingCadenceRecordWithSamples.kt`
  - `PowerRecordWithSamples.kt`
  - `SpeedRecordWithSamples.kt`
  - `StepsCadenceRecordWithSamples.kt`

- **Example Structure (`PowerRecordWithSamples.kt`):**
  ```kotlin
  data class PowerRecordWithSamples(
      @Embedded val record: PowerRecordEntity,
      @Relation(
          parentColumn = "hcUid",
          entityColumn = "parentRecordUid"
      )
      val samples: List<PowerSampleEntity>
  )
  ```

**Step 4: Update Data Access Objects (DAOs)**

The DAOs need to be updated to handle transactional inserts and relational queries.

- **Files to Modify:** `...Dao.kt` (x4)
- **Changes:**
  - The `insertAll` methods will be updated. For each record, it will become a `@Transaction` method that first inserts the parent record and then inserts the associated list of sample entities.
  - The `getAllObservable` (or similar) query methods will be updated to return a `Flow<List<...RecordWithSamples>>`.

- **Example (`PowerRecordDao.kt`):**
  ```kotlin
  // New transactional insert method
  @Transaction
  suspend fun insertRecordWithSamples(record: PowerRecordEntity, samples: List<PowerSampleEntity>) {
      insert(record)
      insertAllSamples(samples)
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllSamples(samples: List<PowerSampleEntity>)

  // Updated query method
  @Transaction
  @Query("SELECT * FROM power_records ORDER BY start_time_epoch_millis DESC")
  fun getAllObservable(): Flow<List<PowerRecordWithSamples>>
  ```

**Step 5: Update Avro-to-Entity Mappers**

The `To...Entity.kt` mappers will be changed to output both the parent entity and the list of child entities, which the DAO can then consume.

- **Files to Modify:** `To...Entity.kt` (x4)
- **Change:** The function signature will change to return a `Pair` containing the main entity and the list of sample entities.

- **Example (`ToPowerRecordEntity.kt`):**
  ```kotlin
  // fun AvroPowerRecord.toPowerRecordEntity(): PowerRecordEntity -> becomes:
  fun AvroPowerRecord.toPowerRecordEntity(): Pair<PowerRecordEntity, List<PowerSampleEntity>> {
      val recordEntity = PowerRecordEntity(/*...params...*/)
      val sampleEntities = this.samples.map {
          PowerSampleEntity(parentRecordUid = this.metadata.id, /*...more params...*/)
      }
      return Pair(recordEntity, sampleEntities)
  }
  ```

## 4. Summary of Next Steps

This constitutes a full-stack refactoring for the affected record types. Upon approval, I will execute this new relational database plan.
