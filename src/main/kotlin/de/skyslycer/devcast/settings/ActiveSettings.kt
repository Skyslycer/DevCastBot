package de.skyslycer.devcast.settings

import dev.kord.common.entity.Snowflake
import java.util.concurrent.ConcurrentHashMap

class ActiveSettings : ConcurrentHashMap<Pair<Snowflake, Snowflake>, SettingType>()