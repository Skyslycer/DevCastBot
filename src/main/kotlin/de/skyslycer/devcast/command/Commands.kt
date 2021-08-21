package de.skyslycer.devcast.command

import de.skyslycer.devcast.command.commands.event.EventCreateCommand
import de.skyslycer.devcast.database.collection.DatabaseCollections
import de.skyslycer.devcast.listener.MessageListener
import de.skyslycer.skylocalizer.SkyLocalizer
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Commands(
    private val kord: Kord,
    private val collections: DatabaseCollections,
    private val localizer: SkyLocalizer,
    private val messageListener: MessageListener,
    private val scope: CoroutineScope
) {

    private val eventCreateCommand = EventCreateCommand(collections, localizer, messageListener)

    @OptIn(KordPreview::class)
    suspend fun initCommands() {
        kord.slashCommands {
            command("settings", "Ändere die Einstellungen für diesen Guild") {
                options {
                    string("new", "Die Änderung für die gewählte Einstellung") { required = false }
                }
                handler {
                    this.respondEphemeral { content = "yes" }
                }
            }
            command("event", "Kommandos jeglicher Aktionen für Events") {
                subcommands {
                    subCommand("create", "Erstelle ein neues Event") {
                        options {
                            string("organizers", "Die Veranstalter des Events")
                            string("topic", "Das Thema des Events")
                            string("description", "Eine grobe Beschreibung über das zu behandelnde Thema")
                            string("time", "Wann das Event stattfindet")
                        }
                        handler {
                            scope.launch {
                                eventCreateCommand.execute(this@handler)
                            }
                        }
                    }
                    subCommand("delete", "Entferne ein Event") {
                        options {
                            string("id", "Die Event-Id")
                        }
                        handler {  }
                    }
                    subCommand("info", "Zeigt Informationen über Events an") {
                        options {
                            string("id", "Die Event-Id des Events") { required = false }
                        }
                        handler {  }
                    }
                }
            }
            command("application", "Kommandos jeglicher Aktionen für die Event-Bewerbung") {
                subcommands {
                    subCommand("apply", "Bewirb dich als Event-Veranstalter") {
                        handler {  }
                    }
                    subCommand("deny", "Lehnt eine Bewerbung mit der gegebenen Berwerbungs-Id ab") {
                        options {
                            string("id", "Die Bewerbungs-Id der Bewerbung")
                        }
                        handler {  }
                    }
                    subCommand("accept", "Akzeptiert eine Berwerbung mit der gegebenen Berwerbungs-Id") {
                        options {
                            string("id", "Die Bewerbungs-Id der Bewerbung")
                        }
                        handler {  }
                    }
                    subCommand("info", "Zeigt Informationen über Bewerbungen an") {
                        options {
                            string("id", "Die Bewerbungs-Id der Bewerbung") { required = false }
                        }
                        handler {  }
                    }
                }
            }
        }
    }

}