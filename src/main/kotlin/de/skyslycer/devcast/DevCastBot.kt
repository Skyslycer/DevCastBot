package de.skyslycer.devcast

import de.skyslycer.devcast.command.Commands
import de.skyslycer.devcast.database.collection.Applications
import de.skyslycer.devcast.database.collection.DatabaseCollections
import de.skyslycer.devcast.database.collection.Events
import de.skyslycer.devcast.database.collection.Guilds
import de.skyslycer.devcast.listener.*
import de.skyslycer.skylocalizer.SkyLocalizer
import de.skyslycer.skylocalizer.reader.ResourceBundleReader
import dev.kord.core.Kord
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

suspend fun main() {

    DevCastBot().start()

}

class DevCastBot {

    private val token = System.getenv("DEVCAST_TOKEN")
    private val mongoDb = System.getenv("DEVCAST_MONGODB")
    private val sentry = System.getenv("DEVCAST_SENTRY")

    private val guilds = Guilds()
    private val events = Events()
    private val applications = Applications()

    private val languageFile = Paths.get("language/language.properties")
    private val localizer = SkyLocalizer(languageFile, ResourceBundleReader(), Locale.ROOT)

    private val logger = LoggerFactory.getLogger(DevCastBot::class.java)

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var client: CoroutineClient
    private lateinit var database: CoroutineDatabase
    private lateinit var collections: DatabaseCollections

    private val listeners = mutableListOf<Pair<Listener<*>, ListenerHook>>()

    private var startTime by Delegates.notNull<Long>()

    suspend fun start() {
        startTime = currentMillis()

        initSentry()
        initDatabase()
        initLocalizer()
        initKord()
    }

    private suspend fun initKord() {
        try {
            logger.info("Starting the Discord bot now. Loading took ${computeTakenTime(startTime)}ms in total")

            val kord = Kord(token)

            initKordUsages(kord)

            kord.login()
        } catch (exception: Exception) {
            logger.error(exception.localizedMessage)
            logger.error("Couldn't login to Discord with the given token! Stopping...")
            exitProcess(1)
        }
    }

    private fun initDatabase() {
        val startTime = currentMillis()

        client = KMongo.createClient(mongoDb).coroutine
        database = client.getDatabase("devcast")
        collections = DatabaseCollections(
            database.getCollection("guilds"),
            database.getCollection("events"),
            database.getCollection("applications"),
            guilds,
            events,
            applications
        )

        logger.info("Successfully loaded database and its collections. Took ${computeTakenTime(startTime)}ms")
    }

    private fun initSentry() {
        val startTime = currentMillis()

        Sentry.init { options ->
            options.dsn = sentry
            options.tracesSampleRate = 0.5
        }

        logger.info("Initiated Sentry error logging! Took ${computeTakenTime(startTime)}ms")
    }

    private fun initLocalizer() {
        val startTime = currentMillis()

        if (!Files.exists(languageFile)) {
            Files.createDirectories(languageFile.parent)
            Files.copy(javaClass.classLoader.getResourceAsStream(languageFile.fileName.toString())!!, languageFile)
        }

        localizer.load()

        logger.info("Loaded localization from file! Took ${computeTakenTime(startTime)}ms")
    }

    private suspend fun initKordUsages(kord: Kord) {
        val messageListener = MessageListener()
        val interactionListener = InteractionListener()

        messageListener.add(listeners)
        interactionListener.add(listeners)

        Commands(kord, collections, localizer, messageListener, scope).initCommands()
    }

    private fun currentMillis(): Long = System.currentTimeMillis()

    private fun computeTakenTime(start: Long): Long = currentMillis() - start

}

fun Any?.s(): String {
    return this.toString()
}