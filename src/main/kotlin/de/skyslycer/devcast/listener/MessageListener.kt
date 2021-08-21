package de.skyslycer.devcast.listener

import dev.kord.core.event.message.MessageCreateEvent
import java.util.function.Predicate

class MessageListener : Listener<MessageCreateEvent>, EventWaiter<MessageCreateEvent> {

    override val waiters: MutableSet<Pair<Predicate<MessageCreateEvent>, suspend (MessageCreateEvent) -> Unit>> = mutableSetOf()

    override fun waitForEvent(condition: Predicate<MessageCreateEvent>, consumer: (suspend (MessageCreateEvent) -> Unit)) {
        waiters.add(Pair(condition, consumer))
    }

    override suspend fun supply(event: MessageCreateEvent) {
        for (waiter in waiters) {
            if (waiter.first.test(event)) {
                waiter.second.invoke(event)
                waiters.remove(waiter)
            }
        }
    }

}