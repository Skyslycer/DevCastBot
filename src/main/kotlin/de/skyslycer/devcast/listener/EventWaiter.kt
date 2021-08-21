package de.skyslycer.devcast.listener

import dev.kord.core.event.Event
import java.util.function.Predicate

interface EventWaiter<T : Event> {

    val waiters: MutableSet<Pair<Predicate<T>, suspend (T) -> Unit>>

    fun waitForEvent(condition: Predicate<T>, consumer: suspend (T) -> Unit)

}