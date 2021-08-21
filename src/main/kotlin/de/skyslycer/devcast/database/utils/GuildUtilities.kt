package de.skyslycer.devcast.database.utils

import com.mongodb.client.model.ReplaceOptions
import de.skyslycer.devcast.database.GuildSettings
import de.skyslycer.devcast.database.collection.Guilds
import dev.kord.common.entity.Snowflake
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

class GuildUtilities {

    companion object {
        suspend fun getOrCreateGuildSettings(
            collection: CoroutineCollection<GuildSettings>,
            guilds: Guilds,
            id: Snowflake
        ): GuildSettings {
            return getGuildSettings(collection, guilds, id) ?: createGuildSettings(collection, guilds, null, id)
        }

        suspend fun createGuildSettings(
            collection: CoroutineCollection<GuildSettings>,
            guilds: Guilds,
            guildSettings: GuildSettings?,
            id: Snowflake
        ): GuildSettings {
            val insertingSettings = guildSettings ?: newGuildSettings(id)

            collection.insertOne(insertingSettings)

            guilds[id] = insertingSettings

            return insertingSettings
        }

        fun newGuildSettings(id: Snowflake): GuildSettings {
            return GuildSettings(
                id,
                Snowflake(0),
                Snowflake(0),
                Snowflake(0),
                Snowflake(0),
                Snowflake(0)
            )
        }

        suspend fun getGuildSettings(
            collection: CoroutineCollection<GuildSettings>,
            guilds: Guilds,
            id: Snowflake
        ): GuildSettings? {
            return guilds[id] ?: collection.findOne(GuildSettings::id eq id)
        }

        suspend fun updateGuildSetting(
            collection: CoroutineCollection<GuildSettings>,
            guildSettings: GuildSettings
        ): GuildSettings {
            collection.replaceOne(
                GuildSettings::id eq guildSettings.id,
                guildSettings,
                ReplaceOptions().upsert(true)
            )

            return guildSettings
        }
    }

}