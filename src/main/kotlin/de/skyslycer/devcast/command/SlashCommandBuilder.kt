package de.skyslycer.devcast.command

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ApplicationCommand
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder
import dev.kord.rest.builder.interaction.ApplicationCommandPermissionsModifyBuilder
import dev.kord.rest.builder.interaction.ApplicationCommandsCreateBuilder
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory
import dev.kord.rest.builder.interaction.SubCommandBuilder as KordSubCommandBuilder

private val logger = LoggerFactory.getLogger("SlashCommands")

@DslMarker
annotation class SlashCommandDsl

typealias SlashCommandHandler = suspend CommandInteraction.() -> Unit
typealias SlashCommandPrecondition = suspend CommandInteraction.() -> Boolean
typealias KordCommandPermModifier = ApplicationCommandPermissionsModifyBuilder.() -> Unit

@OptIn(KordPreview::class)
@SlashCommandDsl
suspend fun Kord.slashCommands(builder: SlashCommandsBuilder.() -> Unit) {
    lateinit var commandBuilder: SlashCommandsBuilder
    val flow = createGlobalApplicationCommands {
        commandBuilder = SlashCommandsBuilder(this@slashCommands, this).apply(builder)
    }
    commandBuilder.build(flow)
}

@OptIn(KordPreview::class)
@SlashCommandDsl
suspend fun Kord.guildCommands(guildId: Snowflake, builder: SlashCommandsBuilder.() -> Unit) {
    lateinit var commandBuilder: SlashCommandsBuilder
    val flow = slashCommands.createGuildApplicationCommands(guildId) {
        commandBuilder = SlashCommandsBuilder(this@guildCommands, this).apply(builder)
    }
    commandBuilder.build(flow)
}

@SlashCommandDsl
class SlashCommandsBuilder(private val kord: Kord, private val kordBuilder: ApplicationCommandsCreateBuilder) {

    private val commandsByName: MutableMap<String, BuiltCommand> = mutableMapOf()
    private val commandsBySnowflake: MutableMap<Snowflake, BuiltCommand> = mutableMapOf()

    @SlashCommandDsl
    fun command(name: String, description: String, builder: SlashCommandBuilder.() -> Unit) {
        kordBuilder.command(name, description) {
            commandsByName[name] = SlashCommandBuilder(this).apply(builder).build()
        }
    }

    @OptIn(InternalCoroutinesApi::class, KordPreview::class)
    suspend fun build(flow: Flow<ApplicationCommand>) {
        val guildsAndCommandsNeedingPermission =
            mutableMapOf</*guild*/Snowflake, MutableMap</*command*/Snowflake, KordCommandPermModifier>>()
        flow.collect {
            val handler = commandsByName[it.name] ?: error("Command ${it.name} not found?!")
            commandsBySnowflake[it.id] = handler
            logger.info("Registered handler for {} with id {}", it.name, it.id)
            if (handler.permissions != null) {
                handler.permissions.forEach { (guild, perms) ->
                    guildsAndCommandsNeedingPermission.computeIfAbsent(guild) { mutableMapOf() }[it.id] = perms
                }
            }
        }

        if (guildsAndCommandsNeedingPermission.isNotEmpty()) {
            logger.info("Registering permissions for {} guilds", guildsAndCommandsNeedingPermission.size)
            guildsAndCommandsNeedingPermission.forEach { (guild, commandPerms) ->
                kord.slashCommands.bulkEditApplicationCommandPermissions(kord.slashCommands.applicationId, guild) {
                    commandPerms.forEach { (cmd, permBuilder) ->
                        command(cmd, permBuilder)
                    }
                }
            }

        }
        kord.on<InteractionCreateEvent> {
            val myInteraction = this.interaction
            if (myInteraction !is CommandInteraction) return@on
            logger.debug("Got a command: {} / {}", myInteraction.command.rootName, myInteraction.command.rootId)
            val builtCommand =
                commandsBySnowflake[myInteraction.command.rootId] ?: return@on logger.debug("Didn't recognise command")
            if (builtCommand.precondition?.invoke(myInteraction) != false)// null means unrestricted
                builtCommand.handler(myInteraction)
            else
                myInteraction.respondEphemeral {
                    content = "**You don't have permission for that!**"
                }
        }
    }
}

@SlashCommandDsl
class SlashCommandBuilder @OptIn(KordPreview::class) constructor(private val kordBuilder: ApplicationCommandCreateBuilder) {
    private lateinit var _handler: SlashCommandHandler
    private var _precondition: SlashCommandPrecondition? = null
    private var guildPermissions: Map<Snowflake, KordCommandPermModifier>? = null

    @OptIn(KordPreview::class)
    @SlashCommandDsl
    fun options(builder: ApplicationCommandCreateBuilder.() -> Unit) {
        kordBuilder.apply(builder)
    }

    @SlashCommandDsl
    fun handler(theHandler: SlashCommandHandler) {
        require(!this::_handler.isInitialized) { "Handler was already initialised" }
        _handler = theHandler
    }

    @SlashCommandDsl
    fun subcommands(builder: SubCommandsBuilder.() -> Unit) {
        SubCommandsBuilder(kordBuilder).apply(builder).build(this)
    }

    @SlashCommandDsl
    fun precondition(precondition: SlashCommandPrecondition) {
        when (_precondition) {
            null -> this._precondition = precondition
            else -> {
                val existingPrecondition = this._precondition!!
                this._precondition = {
                    existingPrecondition() && precondition()
                }
            }
        }
    }

    @SlashCommandDsl
    fun permissions(builder: CommandPermissionsBuilder.() -> Unit) {
        this.guildPermissions = CommandPermissionsBuilder().apply(builder).build()
    }

    fun build(): BuiltCommand {
        return BuiltCommand(this._handler, this._precondition, this.guildPermissions)
    }
}

@SlashCommandDsl
class CommandPermissionsBuilder {
    private val guildToPermissions = mutableMapOf<Snowflake, KordCommandPermModifier>()

    operator fun Snowflake.invoke(builder: KordCommandPermModifier) {
        guildToPermissions[this] = builder
    }

    fun build(): Map<Snowflake, KordCommandPermModifier> = guildToPermissions
}

data class BuiltCommand(
    val handler: SlashCommandHandler,
    val precondition: SlashCommandPrecondition?,
    val permissions: Map<Snowflake, KordCommandPermModifier>?
)

@SlashCommandDsl
class SubCommandsBuilder @OptIn(KordPreview::class) constructor(private val kordBuilder: ApplicationCommandCreateBuilder) {
    private val subHandlers = mutableMapOf<String, SlashCommandHandler>()

    @OptIn(KordPreview::class)
    fun build(rootBuilder: SlashCommandBuilder) {
        require(this.subHandlers.isNotEmpty()) { "No subcommands defined" }
        rootBuilder.handler {
            val interactionCommand = this.command
            if (interactionCommand !is SubCommand)
                return@handler
            val handler = subHandlers[interactionCommand.name] ?: return@handler
            handler(this)
        }
    }

    @OptIn(KordPreview::class)
    @SlashCommandDsl
    fun subCommand(name: String, description: String, builder: SubCommandBuilder.() -> Unit) {
        kordBuilder.subCommand(name, description) {
            subHandlers[name] = SubCommandBuilder(this).apply(builder).build()
        }
    }
}

class SubCommandBuilder @OptIn(KordPreview::class) constructor(private val kordBuilder: KordSubCommandBuilder) {
    private lateinit var _handler: SlashCommandHandler

    @SlashCommandDsl
    fun handler(theHandler: SlashCommandHandler) {
        require(!this::_handler.isInitialized) { "Handler was already initialised" }
        _handler = theHandler
    }

    fun build(): SlashCommandHandler {
        return _handler
    }

    @OptIn(KordPreview::class)
    @SlashCommandDsl
    fun options(builder: KordSubCommandBuilder.() -> Unit) {
        kordBuilder.apply(builder)
    }
}
