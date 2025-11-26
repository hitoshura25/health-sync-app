package io.github.hitoshura25.healthsyncapp.service.processing

import androidx.health.connect.client.records.Record
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class RecordProcessorFactory @Inject constructor(
    private val processors: Map<KClass<out Record>, Provider<out RecordProcessor>>
) {
    fun create(type: SupportedHealthRecordType<*>): RecordProcessor {
        val processorProvider = processors[type.recordKClass]
            ?: throw IllegalArgumentException("No processor found for type ${type.recordKClass.simpleName}")
        return processorProvider.get()
    }
}
