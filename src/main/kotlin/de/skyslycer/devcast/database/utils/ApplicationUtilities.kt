package de.skyslycer.devcast.database.utils

import com.mongodb.client.model.ReplaceOptions
import de.skyslycer.devcast.database.Application
import de.skyslycer.devcast.database.collection.Applications
import dev.kord.common.entity.Snowflake
import org.apache.commons.lang3.RandomStringUtils
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

class ApplicationUtilities {

    companion object {
        suspend fun createApplication(
            collection: CoroutineCollection<Application>,
            application: Application?,
            applications: Applications
        ): Application {
            val insertingApplication = application ?: newApplication()

            collection.insertOne(insertingApplication)

            applications[insertingApplication.id] = insertingApplication

            return insertingApplication
        }

        suspend fun getApplication(
            collection: CoroutineCollection<Application>,
            applications: Applications,
            id: String
        ): Application? {
            return applications[id] ?: collection.findOne(Application::id eq id)
        }

        suspend fun updateApplication(
            collection: CoroutineCollection<Application>,
            application: Application
        ): Application {
            collection.replaceOne(Application::id eq application.id, application, ReplaceOptions().upsert(true))

            return application
        }

        suspend fun deleteEvent(
            collection: CoroutineCollection<Application>,
            applications: Applications,
            application: Application
        ) {
            applications.remove(application.id)

            collection.deleteOne(Application::id eq application.id)
        }

        private fun newApplication(): Application {
            return Application(
                newApplicationId(),
                arrayListOf(),
                Snowflake(0),
                "",
                "",
                Snowflake(0),
                Snowflake(0),
                Snowflake(0)
            )
        }

        private fun newApplicationId(): String {
            return RandomStringUtils.random(4, true, true)
        }
    }

}