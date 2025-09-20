package io.github.hitoshura25.healthsyncapp.worker.fetcher

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.CyclingPedalingCadenceRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapCyclingPedalingCadenceRecord
import javax.inject.Inject

class CyclingPedalingCadenceRecordFetcher @Inject constructor(
    private val healthConnectClient: HealthConnectClient
) : RecordFetcher {

    override suspend fun fetchAndMap(timeRangeFilter: TimeRangeFilter, timestamp: Long): List<Any> {
        val request = ReadRecordsRequest(CyclingPedalingCadenceRecord::class, timeRangeFilter)
        val response = healthConnectClient.readRecords(request)
        return response.records.map { mapCyclingPedalingCadenceRecord(it, timestamp) }
    }
}
