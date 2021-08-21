package de.skyslycer.devcast.command.commands

import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.CommandInteraction

interface Command {

    @OptIn(KordPreview::class)
    suspend fun execute(context: CommandInteraction)

}