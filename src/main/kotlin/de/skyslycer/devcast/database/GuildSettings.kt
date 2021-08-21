package de.skyslycer.devcast.database

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class GuildSettings(
    val id: Snowflake,
    var modRole: Snowflake,
    var organizerRole: Snowflake,
    var pingRole: Snowflake,
    var announcementChannel: Snowflake,
    var applicationChannel: Snowflake
)