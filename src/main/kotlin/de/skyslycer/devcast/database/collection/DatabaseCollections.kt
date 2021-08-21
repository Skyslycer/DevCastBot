package de.skyslycer.devcast.database.collection

import de.skyslycer.devcast.database.Application
import de.skyslycer.devcast.database.Event
import de.skyslycer.devcast.database.GuildSettings
import org.litote.kmongo.coroutine.CoroutineCollection

data class DatabaseCollections(
    val guilds: CoroutineCollection<GuildSettings>,
    val events: CoroutineCollection<Event>,
    val applications: CoroutineCollection<Application>,
    val cachedGuilds: Guilds,
    val cachedEvents: Events,
    val cachedApplications: Applications
)