package de.skyslycer.devcast.command.commands.event

import de.skyslycer.devcast.command.commands.Command
import de.skyslycer.devcast.database.AlternativeUser
import de.skyslycer.devcast.database.GuildSettings
import de.skyslycer.devcast.database.collection.DatabaseCollections
import de.skyslycer.devcast.database.utils.EventUtilities
import de.skyslycer.devcast.database.utils.GuildUtilities
import de.skyslycer.devcast.s
import de.skyslycer.devcast.utils.RoleUtilities
import de.skyslycer.devcast.utils.UserUtilities
import de.skyslycer.skylocalizer.Replacement
import de.skyslycer.skylocalizer.SkyLocalizer
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.OptionValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(KordPreview::class)
class EventCreateCommand(
    private val collections: DatabaseCollections,
    private val localizer: SkyLocalizer
) : Command {

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    override suspend fun execute(context: CommandInteraction) {
        val channel = context.getChannel() as? GuildMessageChannel ?: return
        val guild = channel.getGuildOrNull() ?: return
        val member = context.user.asMemberOrNull(guild.id) ?: return
        val settings = GuildUtilities.getOrCreateGuildSettings(
            collections.guilds,
            collections.cachedGuilds,
            guild.id
        )
        val organizerRole = guild.getRoleOrNull(settings.organizerRole)

        if (organizerRole == null || !RoleUtilities.hasRole(member, organizerRole)) {
            context.respondEphemeral {
                localizer.get("commands.event.insufficient-permission")
            }
            return
        }
    }

    suspend fun generateEvent(
        channel: GuildMessageChannel,
        guild: Guild,
        member: Member,
        settings: GuildSettings,
        context: CommandInteraction
    ) {
        val newEvent = EventUtilities.newEvent()

        context.command.options.forEach {
            when (it.key) {
                "topic" -> newEvent.topic = it.value.value.s()
                "description" -> newEvent.description = it.value.value.s()
                "organizers" -> {
                    val users = UserUtilities.usersFromString(it.value.value.s())

                    if (users.isEmpty()) users.add(AlternativeUser(member.id, member.mention))

                    newEvent.organizers = users
                }
                "time" -> {
                    val possibleTime = computeEventTime(it, context) ?: return

                    newEvent.time = possibleTime
                }
            }
        }
    }

    private suspend fun computeEventTime(entry: Map.Entry<String, OptionValue<*>>, context: CommandInteraction): LocalDateTime? {
        try {
            val time = LocalDateTime.parse(entry.value.s())

            if (time.isBefore(LocalDateTime.now()) || time.isAfter(LocalDateTime.now().plusYears(1))) {
                context.respondEphemeral {
                    content = localizer.get("commands.event.create.time-out-of-range").s()
                }
                return null
            }

            return time
        } catch (exception: DateTimeParseException) {
            context.respondEphemeral {
                content = localizer.get(
                    "commands.event.create.invalid-time",
                    Replacement(
                        "error", makeDateParsingErrorBold(exception.errorIndex, entry.value.s())
                    )
                ).s()
            }
            return null
        }
    }

    private fun makeDateParsingErrorBold(errorIndex: Int, string: String): String {
        return string.substring(0, errorIndex) + "**" +
                string.substring(errorIndex, errorIndex + 1) + "**" +
                string.substring(errorIndex + 1)
    }

}