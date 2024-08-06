package org.smarthata.alice.model.smarthome

import com.fasterxml.jackson.annotation.JsonProperty

interface Parameter {
    val instance: String
    val unit: String
}

data class TempParameter(
    override val instance: String = "temperature",
    override val unit: String = "unit.temperature.celsius",
) : Parameter

data class RangeParameter(
    override val instance: String = "temperature",
    override val unit: String = "unit.temperature.celsius",
    @JsonProperty("random_access")
    val randomAccess: Boolean = true,
    val range: Range,
) : Parameter {
    data class Range(
        val min: Int,
        val max: Int,
        val precision: Double,
    )
}
