package de.skyslycer.devcast.database.collection

import de.skyslycer.devcast.database.GuildSettings
import dev.kord.common.entity.Snowflake
import java.util.concurrent.ConcurrentHashMap

class Guilds : ConcurrentHashMap<Snowflake, GuildSettings>()