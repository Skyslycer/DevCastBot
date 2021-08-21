package de.skyslycer.devcast.listener

import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent

@OptIn(KordPreview::class)
class InteractionListener : Listener<InteractionCreateEvent> {

    override suspend fun supply(event: InteractionCreateEvent) {
        if (event.interaction is SelectMenuInteraction) {
            handleSelectionMenu(event)
        }
    }

    private fun handleSelectionMenu(event: InteractionCreateEvent) {
        val interaction = event.interaction as SelectMenuInteraction

        interaction.data
    }

}