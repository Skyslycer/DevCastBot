package de.skyslycer.devcast.database

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Event(
    val id: String,
    var organizers: MutableList<AlternativeUser>,
    var topic: String,
    var description: String,
    @Contextual var time: LocalDateTime,
    var message: Snowflake,
    var channel: Snowflake,
    var guildId: Snowflake
)