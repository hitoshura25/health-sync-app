package io.github.hitoshura25.healthsyncapp.worker.fetcher

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapPowerRecord
import javax.inject.Inject

class PowerRecordFetcher @Inject constructor(
    private val healthConnectClient: HealthConnectClient
) : RecordFetcher {

    override suspend fun fetchAndMap(timeRangeFilter: TimeRangeFilter, timestamp: Long): List<Any> {
        val request = ReadRecordsRequest(PowerRecord::class, timeRangeFilter)
        val response = healthConnectClient.readRecords(request)
        return response.records.map { mapPowerRecord(it, timestamp) }
    }
}
