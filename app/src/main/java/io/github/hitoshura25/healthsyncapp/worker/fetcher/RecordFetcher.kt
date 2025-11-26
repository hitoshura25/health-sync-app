package io.github.hitoshura25.healthsyncapp.worker.fetcher

import androidx.health.connect.client.time.TimeRangeFilter

/**
 * Interface for fetching and mapping a specific type of health data from Health Connect.
 */
interface RecordFetcher {
    /**
     * Fetches and maps records of a specific type from Health Connect.
     *
     * @param timeRangeFilter The time range to fetch records for.
     * @param timestamp The timestamp to use for the mapping.
     * @return A list of mapped Avro records.
     */
    suspend fun fetchAndMap(timeRangeFilter: TimeRangeFilter, timestamp: Long): List<Any>
}