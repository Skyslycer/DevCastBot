package de.skyslycer.devcast.database.utils

import com.mongodb.client.model.ReplaceOptions
import de.skyslycer.devcast.database.Event
import de.skyslycer.devcast.database.collection.Events
import dev.kord.common.entity.Snowflake
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import java.time.LocalDateTime

class EventUtilities {

    companion object {
        suspend fun createEvent(collection: CoroutineCollection<Event>, event: Event?, events: Events): Event {
            val insertingEvent = event ?: newEvent()

            collection.insertOne(insertingEvent)

            events[insertingEvent.id] = insertingEvent

            return insertingEvent
        }

        suspend fun getEvent(collection: CoroutineCollection<Event>, events: Events, id: String): Event? {
            return events[id] ?: collection.findOne(Event::id eq id)
        }

        suspend fun updateEvent(collection: CoroutineCollection<Event>, event: Event): Event {
            collection.replaceOne(Event::id eq event.id, event, ReplaceOptions().upsert(true))

            return event
        }

        suspend fun deleteEvent(collection: CoroutineCollection<Event>, events: Events, event: Event) {
            events.remove(event.id)

            collection.deleteOne(Event::id eq event.id)
        }

        fun newEvent(): Event {
            return Event(
                newEventId(),
                arrayListOf(),
                "",
                "",
                LocalDateTime.MIN,
                Snowflake(0),
                Snowflake(0),
                Snowflake(0)
            )
        }

        private fun newEventId(): String {
            return RandomStringUtils.random(4, true, true)
        }
    }

}