package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import io.github.hitoshura25.healthsyncapp.avro.AvroDevice
import io.github.hitoshura25.healthsyncapp.avro.AvroMetadata

/**
 * Maps Health Connect SDK Metadata object to Avro DTO AvroMetadata.
 */
fun mapHealthConnectMetadataToAvroMetadata(metadata: Metadata): AvroMetadata {
    val device = metadata.device
    val avroDevice = device?.let {
        AvroDevice(
            manufacturer = it.manufacturer,
            model = it.model,
            type = mapDeviceType(it.type)
        )
    }
    return AvroMetadata(
        id = metadata.id,
        dataOriginPackageName = metadata.dataOrigin.packageName,
        lastModifiedTimeEpochMillis = metadata.lastModifiedTime.toEpochMilli(),
        clientRecordId = metadata.clientRecordId,
        clientRecordVersion = metadata.clientRecordVersion,
        device = avroDevice
    )
}

/**
 * Maps Health Connect SDK Device type to a String representation for Avro.
 */
private fun mapDeviceType(deviceType: Int): String {
    return when (deviceType) {
        Device.TYPE_PHONE -> "PHONE"
        Device.TYPE_WATCH -> "WATCH"
        // Device.TYPE_TABLET -> "TABLET"
        Device.TYPE_HEAD_MOUNTED -> "HEAD_MOUNTED"
        Device.TYPE_RING -> "RING"
        Device.TYPE_SCALE -> "SCALE"
        Device.TYPE_FITNESS_BAND -> "FITNESS_BAND"
        Device.TYPE_CHEST_STRAP -> "CHEST_STRAP"
        Device.TYPE_SMART_DISPLAY -> "SMART_DISPLAY"
        else -> "UNKNOWN"
    }
}
