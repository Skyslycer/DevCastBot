package de.skyslycer.devcast.database

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class Application(
    val id: String,
    var applicants: ArrayList<AlternativeUser>,
    var user: Snowflake,
    var topic: String,
    var description: String,
    var message: Snowflake,
    var channel: Snowflake,
    var guildId: Snowflake
)