package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBloodGlucoseSpecimenSource {
    INTERSTITIAL_FLUID,
    CAPILLARY_BLOOD,
    PLASMA,
    SERUM,
    TEARS,
    WHOLE_BLOOD,
    UNKNOWN
}