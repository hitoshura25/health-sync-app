# Avro Processing and Data Pipeline TDD Plan

This document outlines the plan for refactoring the Health Sync App to use Apache Avro for data serialization, create a file-based staging pipeline for fetched Health Connect data, and the Test-Driven Development (TDD) approach for implementing these changes.

## I. Goals

1.  Decouple data fetching from data processing/uploading.
2.  Use Apache Avro as the serialization format for data destined for a backend.
3.  Implement a resilient data pipeline using a local file-based staging area.
4.  Maintain current UI functionality by initially populating the Room DB from staged Avro files.
5.  Pave the way for future backend integration by having Avro files ready for upload.
6.  Develop this iteratively using a Test-Driven Development approach.

## II. Proposed Architecture Overview

1.  **`HealthDataFetcherWorker`**:
    *   Fetches data from Health Connect.
    *   Maps Health Connect SDK objects to Avro Data Transfer Objects (DTOs).
    *   Serializes Avro DTOs into `.avro` data files (with embedded schemas).
    *   Writes these files to a staging directory: `/avro_staging/`.
2.  **`/avro_staging/` Directory**:
    *   Located in app's internal storage (e.g., `context.getFilesDir() + "/avro_staging/"`).
    *   Acts as a persistent queue for fetched data awaiting processing.
3.  **`AvroFileProcessorWorker`**:
    *   **Phase 1 (UI Focus)**:
        *   Scans `/avro_staging/` for new `.avro` files.
        *   Reads and deserializes Avro records.
        *   Maps Avro DTOs to existing Room DB Entities.
        *   Inserts entities into Room DB (powering the current UI).
        *   Moves successfully processed files to `/avro_completed/`.
    *   **Phase 2 (Backend Focus - Future)**:
        *   Scans `/avro_staging/` for new `.avro` files.
        *   Uploads the entire `.avro` file to the backend.
        *   On successful upload, moves the file to `/avro_completed/`.
4.  **`/avro_completed/` Directory**:
    *   Located in app's internal storage (e.g., `context.getFilesDir() + "/avro_completed/"`).
    *   Archives successfully processed Avro files for data preservation, debugging, and potential future re-uploads.
    *   Requires a future strategy for cleanup/retention.
5.  **Room Database**:
    *   Initially, continues to serve as the data source for the UI.
    *   Populated by `AvroFileProcessorWorker` in Phase 1.
    *   Its role might change once direct backend integration for UI data is considered.

## III. Overall TDD Strategy

*   **Unit Tests First**: Focus on JVM unit tests for core logic (mapping, Avro serialization/deserialization, service class logic). Use mocking extensively.
*   **Robolectric for Android-Specific Unit Tests**: Utilize for tests requiring Android SDK stubs (e.g., `Context`) but runnable on the JVM.
*   **Instrumented Tests for Integration**: Employ for `WorkManager` integration, actual file I/O on device/emulator, and Room DB interactions. Use an in-memory Room DB for tests where appropriate.
*   **Isolate Logic**: Decouple business logic from `Worker` classes into testable service/utility classes. Workers become thin wrappers.
*   **Small, Focused Cycles**: Adhere to RED-GREEN-REFACTOR for each piece of functionality.

## IV. TDD Iterations

### Iteration 1: Avro Schemas, DTOs, and Basic Serialization/Deserialization

1.  **Objective**: Define Avro schemas for Health Connect data types and ensure basic Avro serialization/deserialization works.
2.  **TDD Steps**:
    *   **RED**: Write a unit test for a chosen Avro DTO (e.g., `AvroStepsRecord`). Test attempts to create, serialize to Avro binary, deserialize back, and assert equality.
    *   **GREEN**:
        *   Define the `.avsc` schema file (e.g., `AvroStepsRecord.avsc`).
        *   Configure Avro Gradle plugin for DTO class generation (if chosen, or use generic records).
        *   Implement test logic using Avro serialization/deserialization APIs (e.g., `SpecificDatumWriter/Reader`, `BinaryEncoder/Decoder`).
    *   **REFACTOR**: Clean up.
    *   Repeat for other core Health Connect data types (Heart Rate, Sleep, etc.).
3.  **Key Artifacts**: `.avsc` files, generated Avro DTO Kotlin/Java classes, unit tests.

### Iteration 2: Mapping Logic (Health Connect SDK Object -> Avro DTO)

1.  **Objective**: Create mappers to convert Health Connect SDK objects to Avro DTOs.
2.  **TDD Steps**:
    *   **RED**: Write a unit test for a `HealthConnectToAvroMapper` class/function. Input a mocked HC SDK `Record` object (e.g., `StepsRecord`), assert correct conversion to the corresponding Avro DTO.
    *   **GREEN**: Implement the mapping logic.
    *   **REFACTOR**.
    *   Repeat for other Health Connect data types.
3.  **Key Artifacts**: Mapper class(es)/functions, unit tests.

### Iteration 3: `HealthDataFetcherWorker` - Core Logic (Writing Avro Files to Staging)

1.  **Objective**: Implement the service responsible for writing Avro DTOs to `.avro` files in the `/avro_staging/` directory.
2.  **Service Class**: e.g., `AvroFileWriterService`.
3.  **TDD Steps**:
    *   **RED**: Unit test (Robolectric/mocked file system): Given a list of Avro DTOs, assert `AvroFileWriterService` creates a correctly named `.avro` file (with embedded schema in header) in a specified test staging directory.
    *   **GREEN**: Implement using Avro's `DataFileWriter`. Test file existence and content integrity (potentially read back). Use temporary file rules for testing.
    *   **REFACTOR**.
    *   Add tests for:
        *   File naming conventions (e.g., `type_[timestamp]_[uuid].avro`).
        *   Writing to a temporary name and renaming on success.
        *   Handling empty lists of DTOs.
        *   Error handling during file I/O.
4.  **Key Artifacts**: `AvroFileWriterService` class, unit tests.

### Iteration 4: `AvroFileProcessorWorker` - Core Logic (Phase 1: Reading Avro & Populating Room)

1.  **Objective**: Implement the service responsible for reading `.avro` files from staging, mapping to Room entities, inserting into Room, and moving processed files to `/avro_completed/`.
2.  **Service Class**: e.g., `AvroFileProcessingService`.
3.  **TDD Steps**:
    *   **Reading Avro**:
        *   **RED**: Unit test: Given a path to a test `.avro` file, assert `AvroFileProcessingService` reads and deserializes its content into a list of Avro DTOs.
        *   **GREEN**: Implement using Avro's `DataFileReader`.
        *   **REFACTOR**.
    *   **Mapping (Avro DTO -> Room Entity)**:
        *   **RED**: Unit test for `AvroToRoomEntityMapper`: Given Avro DTOs, assert correct conversion to existing Room Entities.
        *   **GREEN**: Implement the mapper.
        *   **REFACTOR**.
    *   **Room Interaction**:
        *   **RED**: Instrumented test (or unit test with mocked DAO): Assert `AvroFileProcessingService`, given Room entities, calls the correct DAO `insertAll` methods. Use in-memory Room DB for instrumented tests.
        *   **GREEN**: Implement DAO interaction.
        *   **REFACTOR**.
    *   **File Management**:
        *   **RED**: Unit/Instrumented test: Assert that on successful processing (all above steps for a file), the input `.avro` file is moved from the staging directory to the completed directory.
        *   **GREEN**: Implement file moving logic.
        *   **REFACTOR**.
    *   **Error Handling**:
        *   Add tests to ensure files are *not* moved if any step (deserialization, Room insertion) fails, and that appropriate error/retry signals are propagated.
4.  **Key Artifacts**: `AvroFileProcessingService`, `AvroToRoomEntityMapper`, unit tests, instrumented tests.

### Iteration 5: Worker Class Implementation & `WorkManager` Integration Tests

1.  **Objective**: Implement the `WorkManager` `ListenableWorker` classes and test their integration.
2.  **TDD Steps**:
    *   Implement `HealthDataFetcherWorker.kt` and `AvroFileProcessorWorker.kt`. These should be thin wrappers, delegating to the unit-tested service classes.
    *   **RED**: Instrumented test using `androidx.work.testing.TestListenableWorkerBuilder` for `HealthDataFetcherWorker`. Provide mocked dependencies (e.g., `HealthConnectClient`).
    *   **GREEN**: Execute worker, assert `Result.success()`, verify side effect (e.g., `.avro` file created in a test staging directory).
    *   **REFACTOR**.
    *   **RED**: Instrumented test for `AvroFileProcessorWorker`. Place a test `.avro` file in the test staging directory.
    *   **GREEN**: Execute worker, assert `Result.success()`, verify side effects (data in Room DB, file moved to test completed directory).
    *   **REFACTOR**.
    *   Test worker failure/retry scenarios.
    *   Test chaining of workers if implemented (e.g., `AvroFileProcessorWorker` runs after `HealthDataFetcherWorker`).
3.  **Key Artifacts**: `HealthDataFetcherWorker.kt`, `AvroFileProcessorWorker.kt`, instrumented tests.

### Iteration 6 (Future): `AvroFileProcessorWorker` - Phase 2 (Backend Upload Logic)

1.  **Objective**: Modify `AvroFileProcessingService` to upload `.avro` files to the backend.
2.  **TDD Steps**:
    *   **RED**: Unit test: Assert that the service prepares the correct HTTP request (mock network client) with the `.avro` file as the body.
    *   **GREEN**: Implement network call setup.
    *   **REFACTOR**.
    *   Test handling of successful backend responses (e.g., HTTP 200), leading to file moving to `/avro_completed/`.
    *   Test handling of backend error responses (HTTP 4xx, 5xx, network errors), leading to retry signals (worker returns `Result.retry()`) and *not* moving the file.
    *   Consider using `MockWebServer` in instrumented tests for more end-to-end style testing of uploads.
3.  **Key Artifacts**: Updated `AvroFileProcessingService`, unit tests, potentially instrumented tests with `MockWebServer`.

## V. Key Design Principles & Considerations for TDD

*   **Dependency Injection**: Use DI to provide dependencies (DAOs, service classes, `Context`, dispatchers) to workers and services, making them easier to test with mocks/fakes.
*   **Single Responsibility Principle**: Each class/method should have a clear, single responsibility.
*   **Immutability**: Favor immutable objects (especially for DTOs and entities) where possible.
*   **Error Handling**: Define clear error handling strategies for each step (file I/O, network, database). Workers should return appropriate `Result` values.
*   **File System Robustness**:
    *   Write to temporary filenames first, then rename to final names in staging to prevent partial file processing.
    *   Ensure exclusive access if needed, though separate workers might mitigate this.
*   **Configuration**: Make directory names, etc., configurable if necessary, though constants are fine initially.

This plan provides a structured approach to a significant but valuable refactoring effort.
